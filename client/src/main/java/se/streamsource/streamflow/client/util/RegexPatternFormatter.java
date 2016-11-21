/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.util;

import javax.swing.text.DefaultFormatter;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A regular expression based implementation of AbstractFormatter.
 */
public class RegexPatternFormatter extends DefaultFormatter
{
   private Pattern pattern;

   private Matcher matcher;

   public RegexPatternFormatter()
   {
      super();
   }

   /**
    * Creates a regular expression based AbstractFormatter.
    * pattern specifies the regular expression that will be used
    * to determine if a value is legal.
    */
   public RegexPatternFormatter( String pattern ) throws PatternSyntaxException
   {
      this();
      setPattern( Pattern.compile( pattern ) );
   }

   /**
    * Creates a regular expression based AbstractFormatter.
    * pattern specifies the regular expression that will be used
    * to determine if a value is legal.
    */
   public RegexPatternFormatter( Pattern pattern )
   {
      this();
      setPattern( pattern );
   }

   /**
    * Sets the pattern that will be used to determine if a value is legal.
    */
   public void setPattern( Pattern pattern )
   {
      this.pattern = pattern;
   }

   /**
    * Returns the Pattern used to determine if a value is legal.
    */
   public Pattern getPattern()
   {
      return pattern;
   }

   /**
    * Sets the Matcher used in the most recent test if a value is
    * legal.
    */
   protected void setMatcher( Matcher matcher )
   {
      this.matcher = matcher;
   }

   /**
    * Returns the Matcher from the most test.
    */
   protected Matcher getMatcher()
   {
      return matcher;
   }

   /**
    * Parses text returning an arbitrary Object. Some formatters
    * may return null.
    * <p/>
    * If a Pattern has been specified and the text completely
    * matches the regular expression this will invoke setMatcher.
    *
    * @param text String to convert
    * @return Object representation of text
    * @throws ParseException if there is an error in the conversion
    */
   public Object stringToValue( String text ) throws ParseException
   {
      Pattern pattern = getPattern();

      if (pattern != null)
      {
         Matcher matcher = pattern.matcher( text );

         if (matcher.matches())
         {
            setMatcher( matcher );
            return super.stringToValue( text );
         }
         throw new ParseException( "Pattern did not match", 0 );
      }
      return text;
   }
}