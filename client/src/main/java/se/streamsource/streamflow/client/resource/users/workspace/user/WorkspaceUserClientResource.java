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

package se.streamsource.streamflow.client.resource.users.workspace.user;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.client.resource.LabelsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.assignments.WorkspaceUserAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.delegations.WorkspaceUserDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.WorkspaceUserInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.waitingfor.WorkspaceUserWaitingForClientResource;

/**
 * JAVADOC
 */
public class WorkspaceUserClientResource
      extends CommandQueryClientResource
{
   public WorkspaceUserClientResource( @Uses Context context, @Uses Reference reference )
   {
      super( context, reference );
   }

   public WorkspaceUserInboxClientResource inbox()
   {
      return getSubResource( "inbox", WorkspaceUserInboxClientResource.class );
   }

   public WorkspaceUserAssignmentsClientResource assignments()
   {
      return getSubResource( "assignments", WorkspaceUserAssignmentsClientResource.class );
   }

   public WorkspaceUserDelegationsClientResource delegations()
   {
      return getSubResource( "delegations", WorkspaceUserDelegationsClientResource.class );
   }

   public WorkspaceUserWaitingForClientResource waitingFor()
   {
      return getSubResource( "waitingfor", WorkspaceUserWaitingForClientResource.class );
   }

   public LabelsClientResource labels()
   {
      return getSubResource( "labels", LabelsClientResource.class );
   }
}