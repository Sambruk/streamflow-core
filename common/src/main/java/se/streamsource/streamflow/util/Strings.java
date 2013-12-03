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

import org.qi4j.api.property.Property;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility methods for strings
 */
public class Strings
{
    /**
     * Check if a string is null or equal to ""
     *
     * @param value string to be tested
     * @return true if value is null or equal to ""
     */
    public static boolean empty( String value )
    {
        return value == null || value.trim().equals( "" );
    }

    /**
     * Transform a Java name to a human readable string by replacing uppercase characters
     * with space+toLowerCase(char)
     * Example:
     * changeDescription -> Change description
     * doStuffNow -> Do stuff now
     * ON_HOLD -> On hold
     *
     * @param name
     * @return
     */
    public static String humanReadable( String name )
    {
        if (name.toUpperCase().equals(name))
            name = name.toLowerCase();

        StringBuilder humanReadableString = new StringBuilder();

        boolean previousWasUppercase = false;
        boolean hasUppercaseWord = false;
        for (int i = 0; i < name.length(); i++)
        {
            char character = name.charAt( i );
            if (i == 0)
            {
                // Capitalize first character
                humanReadableString.append( Character.toUpperCase( character ) );
                previousWasUppercase = true;
            } else if (character == '_')
            {
                humanReadableString.append( ' ' );
            } else if (Character.isLowerCase( character ))
            {
                if (hasUppercaseWord)
                {
                    humanReadableString.append( ' ' );
                    hasUppercaseWord = false;
                }
                humanReadableString.append( character );
                previousWasUppercase = false;
            } else if (previousWasUppercase)
            {
                previousWasUppercase = true;
                hasUppercaseWord = true;
                humanReadableString.append( character );
            } else
            {
                previousWasUppercase = true;
                humanReadableString.append( ' ' ).append( Character.toLowerCase( character ) );
            }
        }

        return humanReadableString.toString();
    }

    public static String capitalize(final String string)
    {
       if (string == null)
          throw new NullPointerException();
       if (string.equals(""))
          throw new NullPointerException();

       return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    public static String hashString( String string )
    {
       try
       {
          MessageDigest md = MessageDigest.getInstance( "SHA" );
          md.update( string.getBytes( "UTF-8" ) );
          byte raw[] = md.digest();
          String hash = (new BASE64Encoder()).encode( raw );
          return hash;
       }
       catch (NoSuchAlgorithmException e)
       {
          throw new IllegalStateException( "No SHA algorithm founde", e );
       }
       catch (UnsupportedEncodingException e)
       {
          throw new IllegalStateException( e.getMessage(), e );
       }
    }

   /**
    * Test if the contents of a Property equals the new value regardless if the property is optional, empty or has value.
    * @param stringProperty The property to test
    * @param newValue The new value
    * @return A boolean
    */
   public static boolean propertyEquals( Property<String> stringProperty, String newValue )
   {
      return stringProperty.get() == null ? newValue == null : stringProperty.get().equals( newValue );
   }
}
