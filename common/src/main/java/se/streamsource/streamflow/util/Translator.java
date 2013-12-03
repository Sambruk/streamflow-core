/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.mozilla.universalchardet.UniversalDetector;
import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author henrikreinhold
 *
 */
public class Translator
{
   public static String HTML = "text/html";
   public static String PLAIN = "text/plain";

   public static String translate(String text, Map<String, String> translations)
   {
      return translate(text, translations, null);
   }
   
   public static String translate(String text, Map<String, String> translations, Map<String,String> variables)
   {
      if( text.startsWith("{") && text.endsWith("}") )
      {
         String[] tokens = text.substring(1,text.length()-1).split( "," );
         String key = tokens[0];
         String translation = translations.get(key);
         if (Strings.empty(translation))
            return "";
         String[] args = new String[tokens.length-1];
         System.arraycopy(tokens, 1, args, 0, args.length);

         if (variables == null)
         {
            variables = new HashMap<String,String>();
         }

         for (String arg : args)
         {
            String[] variable = arg.split("=", 2);
            if (variable.length == 2)
               variables.put(variable[0],variable[1]);
         }

         return MessageTemplate.text(translation, variables);
      } else
         return text;
   }

   public static String htmlToText( String html )
   {
      String result = html;

         ContentHandler handler = new BodyContentHandler( );
         Metadata metadata = new Metadata();
         try
         {
            new HtmlParser().parse( IOUtils.toInputStream( result, guessEncoding( result ) ), handler, metadata, new ParseContext());
            result = handler.toString();
         } catch (Exception e)
         {
            //do nothing
         }
      return result;
   }

   public static String cleanHtml( String html ) throws IOException
   {

      /*Tidy tidy = new Tidy();
      tidy.setWord2000( true );
      tidy.setQuiet( true );
      tidy.setShowWarnings( false );

      tidy.getConfiguration().printConfigOptions( new OutputStreamWriter( System.out ), true );
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StringReader reader = new StringReader( html );

      tidy.parse( reader, baos );

      String result = baos.toString("UTF-8");
      baos.close();
      reader.close();

      return result;*/
      return html.replaceAll("<o:p>(\\s|&nbsp;)*</o:p>", "" );

   }

   public static String guessEncoding( String input) {
      byte [] bytes = input.getBytes();
      String DEFAULT_ENCODING = "UTF-8";
      UniversalDetector detector = new UniversalDetector(null);
      detector.handleData(bytes, 0, bytes.length);
      detector.dataEnd();
      String encoding = detector.getDetectedCharset();
      detector.reset();
      if (encoding == null) {
         encoding = DEFAULT_ENCODING;
      }
      return encoding;
   }
}
