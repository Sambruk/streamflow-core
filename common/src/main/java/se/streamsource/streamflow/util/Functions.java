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

package se.streamsource.streamflow.util;

import org.qi4j.api.util.Function;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for Function
 */
public class Functions
{
   public static <T> Comparator<T> comparator( final Function<T, Comparable> comparableFunction)
   {
       return new Comparator<T>()
       {
           Map<T, Comparable> compareKeys = new HashMap<T, Comparable>();

           public int compare( T o1, T o2 )
           {
               Comparable key1 = compareKeys.get( o1 );
               if (key1 == null)
               {
                   key1 = comparableFunction.map( o1 );
                   compareKeys.put(o1, key1);
               }

               Comparable key2 = compareKeys.get( o2 );
               if (key2 == null)
               {
                   key2 = comparableFunction.map( o2 );
                   compareKeys.put(o2, key2);
               }

               return key1.compareTo( key2 );
           }
       };
   }
}
