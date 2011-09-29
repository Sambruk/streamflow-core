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
