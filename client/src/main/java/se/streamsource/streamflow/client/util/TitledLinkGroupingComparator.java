/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.util;

import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.util.Strings;

import java.util.Comparator;

/**
 * JAVADOC
 */
public class TitledLinkGroupingComparator
      implements Comparator<TitledLinkValue>
{
   private String topValue = "";

   public TitledLinkGroupingComparator(){}
   
   public TitledLinkGroupingComparator( String topValue )
   {
      this.topValue = topValue;
   }
   public int compare( TitledLinkValue o1, TitledLinkValue o2 )
   {
      int value = -1;
      String s1 = o1.title().get();
      String s2 = o2.title().get();

      if( !Strings.empty( topValue ) )
      {
        if( topValue.equals( s1 ) && !topValue.equals( s2 ) )
        {
            value = -1;
        } else if ( !topValue.equals( s1 ) && topValue.equals( s2 ) )
        {
           value = 1;
        } else
           value = s1.compareTo( s2 );

      } else
      {
         value = s1.compareTo( s2 );
      }

      return value;
   }
}