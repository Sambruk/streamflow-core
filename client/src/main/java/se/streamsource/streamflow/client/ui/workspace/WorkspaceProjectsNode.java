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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemComparator;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.users.workspace.projects.WorkspaceProjectClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.WorkspaceProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.WorkspaceProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.WorkspaceProjectInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.WorkspaceProjectWaitingforClientResource;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collections;

/**
 * JAVADOC
 */
public class WorkspaceProjectsNode
      extends DefaultMutableTreeNode
      implements Refreshable, EventListener
{
   private AccountModel account;
   private ObjectBuilderFactory obf;

   public WorkspaceProjectsNode( @Uses AccountModel account,
                                 @Structure final ObjectBuilderFactory obf ) throws Exception
   {
      super( account );
      this.account = account;
      this.obf = obf;
   }

   @Override
   public WorkspaceNode getParent()
   {
      return (WorkspaceNode) super.getParent();
   }

   @Override
   public boolean isLeaf()
   {
      return false;
   }

   @Override
   public boolean getAllowsChildren()
   {
      return true;
   }

   public void refresh()
   {
      try
      {
         se.streamsource.streamflow.client.resource.users.UserClientResource user = account.userResource();
         ListValue projects = user.workspace().projects().listProjects().<ListValue>buildWith().prototype();

         super.removeAllChildren();

         Collections.sort( projects.items().get(), new ListItemComparator() );

         for (ListItemValue project : projects.items().get())
         {
            WorkspaceProjectClientResource workspaceProjectClientResource = user.workspace().projects().project( project.entity().get().identity() );
            WorkspaceProjectInboxClientResource projectInboxClientResource = workspaceProjectClientResource.inbox();
            WorkspaceProjectAssignmentsClientResource projectAssignmentsClientResource = workspaceProjectClientResource.assignments();
            WorkspaceProjectDelegationsClientResource projectDelegationsClientResource = workspaceProjectClientResource.delegations();
            WorkspaceProjectWaitingforClientResource projectWaitingforClientResource = workspaceProjectClientResource.waitingFor();

            add( obf.newObjectBuilder( WorkspaceProjectNode.class ).use( workspaceProjectClientResource,
                  projectInboxClientResource,
                  projectAssignmentsClientResource,
                  projectDelegationsClientResource,
                  projectWaitingforClientResource,
                  account.tasks(),
                  project.description().get() ).newInstance() );
         }

      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh_projects, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      if (children != null)
         for (Object child : children)
         {
            EventListener listener = (EventListener) child;
            listener.notifyEvent( event );
         }
   }
}