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
package se.streamsource.streamflow.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiFieldHelper
{

   public static List<String> options( String rawSelectionValues )
   {
      List<String> selectionValues = new ArrayList<String>(  );
      Pattern p = Pattern.compile( "(\\[)(\\w.+?)(\\])" );
      Matcher m = p.matcher( rawSelectionValues );
      while(m.find()){
         selectionValues.add( m.group( 2 ) );
      }
      String clean = rawSelectionValues.replaceAll( "\\[\\w.+?\\]", "" );
      for (String value : clean.split( ", " ))
      {
         if ( !value.isEmpty() )
            selectionValues.add( value );
      }

      return selectionValues;
   }
}
