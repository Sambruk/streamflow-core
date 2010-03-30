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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * JAVADOC
 */
public class WorkspaceUserNode
      extends DefaultMutableTreeNode
      implements EventListener
{
   public WorkspaceUserNode( @Uses AccountModel account,
                             @Uses WorkspaceUserInboxNode inbox,
                             @Uses WorkspaceUserAssignmentsNode assignments,
                             @Uses WorkspaceUserDelegationsNode delegations,
                             @Uses WorkspaceUserWaitingForNode waitingFor ) throws ResourceException
   {
      super( account );

      add( inbox );

      add( assignments );

      add( delegations );

      add( waitingFor );
   }

   public void notifyEvent( DomainEvent event )
   {
      for (Object child : children)
      {
         EventListener listener = (EventListener) child;
         listener.notifyEvent( event );
      }
   }

   @Override
   public WorkspaceNode getParent()
   {
      return (WorkspaceNode) super.getParent();
   }

   @Override
   public String toString()
   {
      return ((AccountModel)getUserObject()).settings().name().get();
   }
}