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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.dci.value.LinkValue;
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
         CommandQueryClient user = account.userResource();
         CommandQueryClient projectsClient = user.getSubClient( "workspace" ).getSubClient( "projects" );
         LinksValue projects = projectsClient.query( "projects", LinksValue.class ).<LinksValue>buildWith().prototype();

         super.removeAllChildren();

         Collections.sort( projects.links().get(), new LinkComparator() );

         for (LinkValue project : projects.links().get())
         {
            CommandQueryClient projectClient = projectsClient.getClient( project.href().get() );
            CommandQueryClient projectInboxClientResource = projectClient.getSubClient( "inbox" );
            CommandQueryClient projectAssignmentsClientResource = projectClient.getSubClient( "assignments" );
            CommandQueryClient projectDelegationsClientResource = projectClient.getSubClient( "delegations" );
            CommandQueryClient projectWaitingforClientResource = projectClient.getSubClient( "waitingfor" );

            TaskTableModel inboxModel = obf.newObjectBuilder( TaskTableModel.class ).use( projectInboxClientResource ).newInstance();
            TaskTableModel assignmentsModel = obf.newObjectBuilder( TaskTableModel.class ).use( projectAssignmentsClientResource ).newInstance();
            TaskTableModel delegationsModel = obf.newObjectBuilder( TaskTableModel.class ).use( projectDelegationsClientResource ).newInstance();
            TaskTableModel waitingForModel = obf.newObjectBuilder( TaskTableModel.class ).use( projectWaitingforClientResource ).newInstance();

            WorkspaceProjectInboxNode inboxNode = obf.newObjectBuilder( WorkspaceProjectInboxNode.class ).use( projectInboxClientResource, inboxModel ).newInstance();
            WorkspaceProjectAssignmentsNode assignmentsNode = obf.newObjectBuilder( WorkspaceProjectAssignmentsNode.class ).use( projectAssignmentsClientResource, assignmentsModel ).newInstance();
            WorkspaceProjectDelegationsNode delegationsNode = obf.newObjectBuilder( WorkspaceProjectDelegationsNode.class ).use( projectDelegationsClientResource, delegationsModel ).newInstance();
            WorkspaceProjectWaitingForNode waitingForNode = obf.newObjectBuilder( WorkspaceProjectWaitingForNode.class ).use( projectWaitingforClientResource, waitingForModel ).newInstance();

            add( obf.newObjectBuilder( WorkspaceProjectNode.class ).use( projectClient,
                  inboxNode,
                  assignmentsNode,
                  delegationsNode,
                  waitingForNode,
                  account.tasks(),
                  project.text().get() ).newInstance() );
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