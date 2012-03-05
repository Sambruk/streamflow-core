/**
 *
 * Copyright 2009-2012 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles converting message templates to strings by replacing {foo} with values retrieved from a Map<String,String>
 */
public class MessageTemplate
{
   public static String text(String template, Map<String, String> variables)
   {
      Pattern pattern = Pattern.compile("\\{[^\\}]*\\}");

      String result = "";
      Matcher matcher = pattern.matcher(template);
      int start = 0;
      while (matcher.find())
      {
         String match = matcher.group();
         match = match.substring(1, match.length() - 1);
         result += template.substring(start, matcher.start());

         String var = variables.get(match);
         if (var != null)
            result += var;

         start = matcher.end();
      }

      result += template.substring(start);

      return result;
   }

   public static TemplateBuilder text(String template)
   {
      return new TemplateBuilder(template);
   }

   public static class TemplateBuilder
   {
      String template;
      Map<String, String> variables = new HashMap<String, String>();

      private TemplateBuilder(String template)
      {
         this.template = template;
      }

      public TemplateBuilder bind(String name, String value)
      {
         variables.put(name, value);
         return this;
      }

      public String eval()
      {
         return text(template, variables);
      }
   }
}
