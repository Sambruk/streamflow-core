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

package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.BasicEventList;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;

/**
 * A general super class for models that use LinkValue lists shown in a JList.
 * This class simplifies the list update for changedDescription events.
 */
public class LinkValueListModel
      implements EventVisitor
{

   protected EventVisitorFilter eventFilter = new EventVisitorFilter( this, "changedDescription" );
   protected BasicEventList<LinkValue> linkValues = new BasicEventList<LinkValue>();

   public boolean visit( DomainEvent event )
   {
      boolean success = false;
      LinkValue updated = getLinkValue( event );
      if (updated != null)
      {
         int idx = linkValues.indexOf( updated );
         ValueBuilder<LinkValue> valueBuilder = updated.buildWith();
         updated = valueBuilder.prototype();

         String eventName = event.name().get();
         if (eventName.equals( "changedDescription" ))
         {
            try
            {
               String newDesc = EventParameters.getParameter( event, "param1" );
               updated.text().set( newDesc );
               linkValues.set( idx, valueBuilder.newInstance() );
               success = true;
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
      return success;
   }

   private LinkValue getLinkValue( DomainEvent event )
   {
      if (linkValues == null)
         return null;

      for (LinkValue link : linkValues)
      {
         if (link.id().get().equals( event.entity().get() ))
         {
            return link;
         }

      }
      return null;
   }
}
