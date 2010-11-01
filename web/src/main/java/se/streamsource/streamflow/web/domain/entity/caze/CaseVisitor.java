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

package se.streamsource.streamflow.web.domain.entity.caze;

import se.streamsource.streamflow.resource.caze.CaseVisitorConfigValue;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;

/**
 * Configurable visitor for case structure
 */
public class CaseVisitor<ThrowableType extends Throwable>
{
   protected CaseVisitorConfigValue config;

   public CaseVisitor( CaseVisitorConfigValue config )
   {
      this.config = config;
   }
   
   public boolean visitCase( Case caze ) throws ThrowableType
   {
      return true;
   }

   public boolean visitContacts( Contacts contacts) throws ThrowableType
   {
      return config.contacts().get();
   }

   public boolean visitConversations( Conversations conversations) throws ThrowableType
   {
      return config.conversations().get();
   }

   public boolean visitEffectiveFields( SubmittedForms effectiveFields ) throws ThrowableType
   {
      return config.effectiveFields().get();
   }

   public boolean visitSubmittedForms(SubmittedForms submittedForms ) throws ThrowableType
   {
      return config.submittedForms().get();
   }

   public boolean visitAttachments( Attachments attachments) throws ThrowableType
   {
      return config.attachments().get();
   }

}
