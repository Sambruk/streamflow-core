package se.streamsource.streamflow.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author henrikreinhold
 *
 */
public class Translator
{
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
   
}
