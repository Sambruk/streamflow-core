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

package se.streamsource.streamflow.client.ui.caze;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.caze.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationsModel;
import se.streamsource.streamflow.client.ui.search.SearchTerms;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;

/**
 * Model that keeps track of all case models
 */
public class CasesModel
      implements EventListener, EventVisitor
{
   @Uses
   CommandQueryClient client;

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   WeakModelMap<String, CaseModel> models = new WeakModelMap<String, CaseModel>()
   {
      protected CaseModel newModel( String key )
      {
         CommandQueryClient caseClient = client.getSubClient( key );

         CommandQueryClient generalClient = caseClient.getSubClient( "general" );
         CommandQueryClient converationsClient = caseClient.getSubClient( "conversations" );
         CommandQueryClient contactsClient = caseClient.getSubClient( "contacts" );
         CommandQueryClient formsClient = caseClient.getSubClient( "forms" );
         CommandQueryClient attachmentsClient = caseClient.getSubClient( "attachments" );

         PossibleFormsModel possibleFormsModel = obf.newObjectBuilder( PossibleFormsModel.class ).use( formsClient ).newInstance();
         CaseLabelsModel labelsModel = obf.newObjectBuilder( CaseLabelsModel.class ).use( generalClient.getSubClient( "labels" ) ).newInstance();
         CaseGeneralModel generalModel = obf.newObjectBuilder( CaseGeneralModel.class ).use( generalClient, possibleFormsModel, labelsModel ).newInstance();
         ConversationsModel conversationsModel = obf.newObjectBuilder( ConversationsModel.class ).use( converationsClient ).newInstance();
         ContactsModel contactsModel = obf.newObjectBuilder( ContactsModel.class ).use( contactsClient ).newInstance();
         CaseFormsModel formsModel = obf.newObjectBuilder( CaseFormsModel.class ).use( formsClient ).newInstance();
         AttachmentsModel attachmentsModel = obf.newObjectBuilder( AttachmentsModel.class ).use( attachmentsClient ).newInstance();

         CaseActionsModel actionsModel = obf.newObjectBuilder( CaseActionsModel.class ).use( caseClient).newInstance();

         return obf.newObjectBuilder( CaseModel.class ).
               use( caseClient,
                     generalModel,
                     conversationsModel,
                     contactsModel,
                     formsModel,
                     attachmentsModel,
                     actionsModel ).newInstance();
      }
   };

   private EventVisitorFilter eventFilter = new EventVisitorFilter( this, "deletedCase", "deletedAssignedCase");

   public LinksValue search(String query) throws ResourceException
   {
      query = SearchTerms.translate( query );

      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( query );

      return client.query( "search", builder.newInstance(), LinksValue.class );
   }

   public CaseModel caze( String id )
   {
      return models.get( id );
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );

      for (CaseModel model : models)
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
