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

package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.dci.resource.CommandQueryClient;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.task.SubmittedFormListDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;

import java.util.logging.Logger;

/**
 * List of contacts for a task
 */
public class TaskSubmittedFormsModel
      implements Refreshable, EventListener, EventVisitor

{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CommandQueryClient client;

   BasicEventList<SubmittedFormListDTO> submittedForms = new BasicEventList<SubmittedFormListDTO>( );

   EventVisitorFilter eventFilter = new EventVisitorFilter( this, "submittedForm" );

   public void refresh()
   {
      try
      {
         submittedForms.clear();
         submittedForms.addAll( client.query( "listsubmittedforms", SubmittedFormsListDTO.class ).forms().get() );
      } catch (Exception e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }

   public EventList<SubmittedFormListDTO> getSubmittedForms()
   {
      return submittedForms;
   }

   public TaskSubmittedFormModel getSubmittedFormModel(int index)
   {
      ValueBuilder<IntegerDTO> builder = vbf.newValueBuilder( IntegerDTO.class );
      builder.prototype().integer().set( index );
      return obf.newObjectBuilder(
            TaskSubmittedFormModel.class ).use( client, builder.newInstance() ).newInstance();
   }


   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      if (client.getReference().getParentRef().getLastSegment().equals( event.entity().get() ))
      {
         Logger.getLogger( "workspace" ).info( "Refresh submitted forms" );
         refresh();
      }

      return false;
   }

   public CommandQueryClient getClient()
   {
      return client;
   }
}