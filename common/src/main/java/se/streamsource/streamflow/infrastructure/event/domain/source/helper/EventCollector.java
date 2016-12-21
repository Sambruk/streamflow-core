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
package se.streamsource.streamflow.infrastructure.event.domain.source.helper;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class EventCollector
      implements EventVisitor
{
   List<DomainEvent> events = new ArrayList<DomainEvent>();

   public boolean visit( DomainEvent event )
   {
      events.add( event );
      return true;
   }

   public List<DomainEvent> events()
   {
      return events;
   }
}
