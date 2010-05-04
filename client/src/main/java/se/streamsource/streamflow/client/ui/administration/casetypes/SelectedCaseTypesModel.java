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

package se.streamsource.streamflow.client.ui.administration.casetypes;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

import java.util.Collection;
import java.util.List;

/**
 * Management of selected casetypes on a project
 */
public class SelectedCaseTypesModel
      implements EventListener, Refreshable
{
   @Uses
   CommandQueryClient client;

   BasicEventList<LinkValue> eventList = new BasicEventList<LinkValue>();

   @Structure
   ValueBuilderFactory vbf;

   public EventList<LinkValue> getCaseTypeList()
   {
      return eventList;
   }

   public void refresh()
   {
      try
      {
         // Get casetype list
         LinksValue newList = client.query( "index", LinksValue.class );

         EventListSynch.synchronize( newList.links().get(), eventList );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_casetypes, e );
      }
   }

   public EventList<TitledLinkValue> getPossibleCaseTypes()
   {
      try
      {
         BasicEventList<TitledLinkValue> possibleCaseTypes = new BasicEventList<TitledLinkValue>();
         List<LinkValue> valueList = client.query( "possiblecasetypes", LinksValue.class ).links().get();
         for (LinkValue linkValue : valueList)
         {
            possibleCaseTypes.add( (TitledLinkValue) linkValue );
         }
         return possibleCaseTypes;
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void addCaseType( LinkValue identity )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( EntityReference.parseEntityReference( identity.id().get()) );
         client.postCommand( "addcasetype", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_casetype, e );
      }
   }

   public void removeCaseType( EntityReference identity )
   {
      try
      {
         client.getSubClient( identity.identity() ).delete();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_casetype, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
   }
}