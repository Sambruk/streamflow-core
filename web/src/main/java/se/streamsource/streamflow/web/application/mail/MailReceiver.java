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
package se.streamsource.streamflow.web.application.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.mixin.NoopMixin;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Iterables;

import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.factory.ApplicationEventCreationConcern;
import se.streamsource.streamflow.infrastructure.event.application.factory.ApplicationEventCreator;

/**
 * Factory for receiving emails. The events will be created by the ReceiveMailService, and services
 * can then listen for them.
 */
@Mixins({NoopMixin.class, MailReceiver.Mixin.class})
@Concerns(ApplicationEventCreationConcern.class)
public interface MailReceiver
   extends ApplicationEventCreator
{
   void receivedEmail( @Optional ApplicationEvent event, EmailValue email);

   boolean hasStreamflowReference( @Optional String referenceHeader );

   abstract class Mixin
      implements MailReceiver
   {
      public boolean hasStreamflowReference( String referenceHeader )
      {
         if ( referenceHeader == null )
            return false;

         List<String> refs = (List<String>) Iterables.addAll( (Collection<String>) new ArrayList<String>(), Iterables.iterable( referenceHeader.split( "[ \r\n\t]" ) ) );

         // check for Streamflow reference
         String sfRefs = Iterables.first( Iterables.filter(new Specification<String>()
         {
            public boolean satisfiedBy(String item)
            {
               return item.endsWith("@Streamflow>");
            }
         }, refs));

         return sfRefs != null;
      }
   }
}
