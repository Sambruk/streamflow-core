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
}
