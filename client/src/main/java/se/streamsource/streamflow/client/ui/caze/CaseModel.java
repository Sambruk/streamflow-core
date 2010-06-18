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

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.caze.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * Model for case details.
 */
public class CaseModel
      implements EventListener
{
   @Uses
   private CaseInfoModel info;

   @Uses
   private CaseActionsModel actions;

   @Uses
   private ConversationsModel conversations;

   @Uses
   private CaseGeneralModel general;

   @Uses
   private ContactsModel contacts;

   @Uses
   private CaseFormsModel forms;

   @Uses
   private AttachmentsModel attachments;

   @Uses
   CommandQueryClient client;

   public String caseId()
   {
      return client.getReference().getLastSegment();
   }

   public CaseInfoModel info()
   {
      return info;
   }
   
   public ConversationsModel conversations()
   {
      return conversations;
   }

   public CaseGeneralModel general()
   {
      return general;
   }

   public ContactsModel contacts()
   {
      return contacts;
   }

   public CaseFormsModel forms()
   {
      return forms;
   }


   public AttachmentsModel attachments()
   {
      return attachments;
   }

   public CaseActionsModel actions()
   {
      return actions;
   }

   public void notifyEvent( DomainEvent event )
   {
      info.notifyEvent( event );
      conversations.notifyEvent( event );
      general.notifyEvent( event );
      contacts.notifyEvent( event );
      forms.notifyEvent( event );
      attachments.notifyEvent( event ); 
   }
}