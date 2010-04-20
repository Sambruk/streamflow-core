/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.administration.tasktypes;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.form.SelectedFormsModel;
import se.streamsource.streamflow.client.ui.administration.label.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.tasktypes.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

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
         LabelsModel labelsModel = obf.newObjectBuilder( LabelsModel.class ).use( taskTypeClient.getSubClient( "labels" ) ).newInstance();
         SelectedLabelsModel selectedLabelsModel = obf.newObjectBuilder( SelectedLabelsModel.class ).use( taskTypeClient.getSubClient( "selectedlabels" ) ).newInstance();
         FormsModel formsModel = obf.newObjectBuilder( FormsModel.class ).use( taskTypeClient.getSubClient( "forms" ) ).newInstance();
         SelectedFormsModel selectedFormsModel = obf.newObjectBuilder( SelectedFormsModel.class ).use( taskTypeClient.getSubClient( "selectedforms" ) ).newInstance();

         return obf.newObjectBuilder( TaskTypeModel.class ).use( labelsModel,
               selectedLabelsModel,
               formsModel,
               selectedFormsModel,
               taskTypeClient ).newInstance();
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
         EventListSynch.synchronize( client.query( "index", LinksValue.class ).links().get(), eventList );
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
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( taskTypeName );
         client.postCommand( "createtasktype", builder.newInstance() );
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.description_cannot_be_more_than_50, e );
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