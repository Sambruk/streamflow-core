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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.search.SearchTerms;
import se.streamsource.streamflow.client.ui.task.conversations.TaskConversationsModel;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.infrastructure.event.source.EventParameters;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * Model that keeps track of all task models
 */
public class TasksModel
      implements EventListener, EventVisitor
{
   @Uses
   CommandQueryClient client;

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   WeakModelMap<String, TaskModel> models = new WeakModelMap<String, TaskModel>()
   {
      protected TaskModel newModel( String key )
      {
         CommandQueryClient taskClient = client.getSubClient( key );

         CommandQueryClient generalClient = taskClient.getSubClient( "general" );
         CommandQueryClient converationsClient = taskClient.getSubClient( "conversations" );
         CommandQueryClient contactsClient = taskClient.getSubClient( "contacts" );
         CommandQueryClient formsClient = taskClient.getSubClient( "forms" );
         CommandQueryClient actionsClient = taskClient;

         PossibleFormsModel possibleFormsModel = obf.newObjectBuilder( PossibleFormsModel.class ).use( formsClient ).newInstance();
         TaskLabelsModel labelsModel = obf.newObjectBuilder( TaskLabelsModel.class ).use( generalClient.getSubClient( "labels" ) ).newInstance();
         TaskGeneralModel generalModel = obf.newObjectBuilder( TaskGeneralModel.class ).use( generalClient, possibleFormsModel, labelsModel ).newInstance();
         TaskConversationsModel conversationsModel = obf.newObjectBuilder( TaskConversationsModel.class ).use( converationsClient ).newInstance();
         TaskContactsModel contactsModel = obf.newObjectBuilder( TaskContactsModel.class ).use( contactsClient ).newInstance();
         TaskFormsModel formsModel = obf.newObjectBuilder( TaskFormsModel.class ).use( formsClient ).newInstance();

         TaskActionsModel actionsModel = obf.newObjectBuilder( TaskActionsModel.class ).use( actionsClient).newInstance();

         return obf.newObjectBuilder( TaskModel.class ).
               use( taskClient,
                     generalModel,
                     conversationsModel,
                     contactsModel,
                     formsModel,
                     actionsModel ).newInstance();
      }
   };

   private EventVisitorFilter eventFilter = new EventVisitorFilter( this, "deletedTask", "deletedAssignedTask", "deletedWaitingForTask" );

   public LinksValue search(String query) throws ResourceException
   {
      query = SearchTerms.translate( query );

      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( query );

      return client.query( "search", builder.newInstance(), LinksValue.class );
   }

   public TaskModel task( String id )
   {
      return models.get( id );
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );

      for (TaskModel model : models)
      {
         model.notifyEvent( event );
      }
   }

   public boolean visit( DomainEvent event )
   {
      String key = EventParameters.getParameter( event, "param1" );
      models.remove( key );

      return false;
   }
}
