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
package se.streamsource.streamflow.client.util;

import java.util.List;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import ca.odell.glazedlists.TextFilterator;

/**
 * Filter for LinkValue. Will filter on texts, and titles if available
 */
public class LinkFilterator
      implements TextFilterator<LinkValue>
{
   public void getFilterStrings( List<String> strings, LinkValue linkValue )
   {
      strings.add( linkValue.text().get() );

      if (linkValue.classes().get() != null)
         strings.add(linkValue.classes().get());

      if (linkValue instanceof TitledLinkValue)
         strings.add( ((TitledLinkValue) linkValue).title().get() );

   }
}