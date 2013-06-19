/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import org.w3c.tidy.Tidy;
import org.xml.sax.ContentHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
            new HtmlParser().parse( IOUtils.toInputStream( result, "UTF-8" ), handler, metadata, new ParseContext());
            result = handler.toString();
         } catch (Exception e)
         {
            //do nothing
         }
      return result;
   }

   public static String cleanHtml( String html ) throws IOException
   {

      Properties p = new Properties(  );
      p.setProperty( "new-blocklevel-tags", "st1:date, st1:city, st1:country-region, st1:place, " +
            "st1:time, o:p, o:smarttagtype, st1:placename, st1:placetype, st1:street, " +
            "st1:address, st1:state, st2:place, st2:placename, st2:placetype, " +
            "st2:city, st2:street, st2:address, st2:time, st2:state, " +
            "st2:country-region, quote, dt, dd" );

      Tidy tidy = new Tidy();
      tidy.getConfiguration().addProps( p );
      tidy.setMakeBare( true );
      tidy.setWord2000( true );
      tidy.setMakeClean( true );
      tidy.setQuiet( true );
      tidy.setShowWarnings( false );

      //tidy.getConfiguration().printConfigOptions( new OutputStreamWriter( System.out ), true );
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StringReader reader = new StringReader( html );

      tidy.parse( reader, baos );

      String result = baos.toString("UTF-8");
      baos.close();
      reader.close();

      return result;

   }
}
