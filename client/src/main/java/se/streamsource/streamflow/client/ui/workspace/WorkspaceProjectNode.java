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

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class WorkspaceProjectNode
      extends DefaultMutableTreeNode
      implements EventListener
{
   @Uses
   String projectName;

   public WorkspaceProjectNode(@Uses WorkspaceProjectInboxNode inbox,
                                @Uses WorkspaceProjectAssignmentsNode assignments,
                                @Uses WorkspaceProjectDelegationsNode delegations,
                                @Uses WorkspaceProjectWaitingForNode waitingFor )
   {
      super( null );

      add( inbox );
      add( assignments );
      add( delegations );
      add( waitingFor );
   }

   public String projectName()
   {
      return projectName;
   }

   @Override
   public WorkspaceProjectsNode getParent()
   {
      return (WorkspaceProjectsNode) super.getParent();
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
   public String toString()
   {
      return projectName;
   }
}