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

import ca.odell.glazedlists.BasicEventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.dci.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.tasktypes.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * List of tasktypes in an Organization
 */
public class TaskTypesModel
      implements Refreshable, EventListener
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CommandQueryClient client;

   BasicEventList<LinkValue> eventList = new BasicEventList<LinkValue>();

   WeakModelMap<String, TaskTypeModel> taskTypeModels = new WeakModelMap<String, TaskTypeModel>()
   {
      protected TaskTypeModel newModel( String key )
      {
         CommandQueryClient taskTypeClient = client.getSubClient( key );
         SelectedLabelsModel selectedLabelsModel = obf.newObjectBuilder( SelectedLabelsModel.class ).use( taskTypeClient.getSubClient( "selectedlabels" ) ).newInstance();
         FormsModel formsModel = obf.newObjectBuilder( FormsModel.class ).use( taskTypeClient.getSubClient( "forms" ) ).newInstance();

         return obf.newObjectBuilder( TaskTypeModel.class ).use( selectedLabelsModel, formsModel, taskTypeClient ).newInstance();
      }
   };

   public BasicEventList<LinkValue> getTaskTypeList()
   {
      return eventList;
   }

   public void refresh()
   {
      try
      {
         // Get TaskType list
         eventList.clear();
         eventList.addAll( client.query( "index", LinksValue.class ).links().get() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void removeTaskType( String id )
   {
      getTaskTypeModel( id ).remove();
   }

   public void newTaskType( String taskTypeName )
   {
      try
      {
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( taskTypeName );
         client.postCommand( "createtasktype", builder.newInstance() );
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_create_project_name_already_exists, e );
         }
         throw new OperationException( AdministrationResources.could_not_create_project, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      for (TaskTypeModel taskTypeModel : taskTypeModels)
      {
         taskTypeModel.notifyEvent( event );
      }
   }

   public TaskTypeModel getTaskTypeModel( String id )
   {
      return taskTypeModels.get( id );
   }
}