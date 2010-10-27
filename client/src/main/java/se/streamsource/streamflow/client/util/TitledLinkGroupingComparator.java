/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import se.streamsource.dci.value.TitledLinkValue;

import java.util.Comparator;

/**
 * JAVADOC
 */
public class TitledLinkGroupingComparator
      implements Comparator<TitledLinkValue>
{
   public int compare( TitledLinkValue o1, TitledLinkValue o2 )
   {
      String s1 = o1.title().get();
      String s2 = o2.title().get();

      return s1.compareTo( s2 );
   }
}