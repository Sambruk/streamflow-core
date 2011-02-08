/*
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

package se.streamsource.streamflow.web.resource.workspace.cases;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.workspace.cases.CaseCommandsContext;
import se.streamsource.streamflow.web.context.workspace.cases.CaseContext;
import se.streamsource.streamflow.web.context.workspace.cases.form.CaseSubmittedFormsContext;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.resource.workspace.cases.conversation.ConversationResource;
import se.streamsource.streamflow.web.resource.workspace.cases.conversation.ConversationsResource;

/**
 * JAVADOC
 */
@RequiresPermission( PermissionType.read )
public class CaseResource
      extends CommandQueryResource
{
   public CaseResource()
   {
      super( CaseContext.class, CaseCommandsContext.class );
   }

   @SubResource
   public void general()
   {
      subResource( CaseGeneralResource.class );
   }

   @SubResource
   public void conversations()
   {
      subResource( ConversationsResource.class );
   }

   @SubResource
   public void contacts()
   {
      subResource( ContactsResource.class );
   }

   @SubResource
   public void submittedforms()
   {
      subResourceContexts( CaseSubmittedFormsContext.class );
   }

   @SubResource
   public void formdrafts()
   {
      subResource( CaseFormDraftsResource.class );
   }

   @SubResource
   public void possibleforms()
   {
      subResource( CasePossibleFormsResource.class );
   }

   @SubResource
   public void attachments()
   {
      subResource( AttachmentsResource.class );
   }
   
   @SubResource
   public void history()
   {
      RoleMap.current().set( RoleMap.role( History.class ).getHistory() );
      subResource( ConversationResource.class );
   }
}
