/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.domain.entity.caze;

import org.qi4j.api.io.Input;
import org.qi4j.api.io.Inputs;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLog;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;

/**
 * JAVADOC
 */
public class CaseDescriptor
{
   private final Case caze;

   public CaseDescriptor(Case aCase)
   {
      caze = aCase;
   }

   public Case getCase()
   {
      return caze;
   }

   public Input<ContactDTO, RuntimeException> contacts()
   {
      return Inputs.iterable(((Contacts.Data)caze).contacts().get());
   }

   public Input<SubmittedFormValue, RuntimeException> submittedForms()
   {
      return Inputs.iterable(((SubmittedForms.Data) caze).submittedForms().get());
   }

   public Input<Conversation, RuntimeException> conversations()
   {
      return Inputs.iterable(((Conversations.Data)caze).conversations());

   }

   public Input<Attachment, RuntimeException> attachments()
   {
      return Inputs.iterable(((Attachments.Data)caze).attachments());
   }

   public Input<CaseLogEntryValue, RuntimeException> caselog()
   {
      return Inputs.iterable(((CaseLog.Data)((CaseLoggable.Data)caze).caselog().get()).entries().get());
   }
}
