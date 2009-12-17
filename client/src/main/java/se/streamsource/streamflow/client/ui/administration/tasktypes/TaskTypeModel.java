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

package se.streamsource.streamflow.client.ui.administration.tasktypes;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.tasktypes.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * JAVADOC
 */
public class TaskTypeModel
      implements EventListener
{
   private
   @Structure
   ValueBuilderFactory vbf;

   private
   @Uses
   CommandQueryClient client;

   private
   @Uses
   SelectedLabelsModel selectedLabelsModel;

   private
   @Uses
   FormsModel formsModel;

   public void changeDescription( String newName )
   {
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( newName );

      try
      {
         client.putCommand( "changedescription", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_rename_project, e );
      }
   }

   public SelectedLabelsModel getSelectedLabelsModel()
   {
      return selectedLabelsModel;
   }

   public FormsModel getFormsModel()
   {
      return formsModel;
   }

   public void notifyEvent( DomainEvent event )
   {
      selectedLabelsModel.notifyEvent( event );
      formsModel.notifyEvent( event );
   }

   public void remove()
   {
      try
      {
         client.deleteCommand();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_tasktype, e );
      }
   }
}
