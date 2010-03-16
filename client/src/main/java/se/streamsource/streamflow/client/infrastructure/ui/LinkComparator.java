/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.infrastructure.ui;

import se.streamsource.dci.value.LinkValue;

import java.util.Comparator;

/**
 * JAVADOC
 */
public class LinkComparator
      implements Comparator<LinkValue>
{
   public int compare( LinkValue o1, LinkValue o2 )
   {
      String s1 = o1.text().get();
      String s2 = o2.text().get();

      return s1.compareToIgnoreCase( s2 );
   }
}