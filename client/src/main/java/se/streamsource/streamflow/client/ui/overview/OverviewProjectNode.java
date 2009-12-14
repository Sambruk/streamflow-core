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

package se.streamsource.streamflow.client.ui.overview;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.resource.users.overview.projects.OverviewProjectClientResource;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class OverviewProjectNode
      extends DefaultMutableTreeNode
      implements EventListener
{
   @Uses
   String projectName;

   public OverviewProjectNode( @Uses OverviewProjectClientResource projectClientResource,
                               @Uses OverviewProjectAssignmentsNode assignmentsNode,
                               @Uses OverviewProjectWaitingForNode waitingForNode,
                               @Structure ObjectBuilderFactory obf )
   {
      super( projectClientResource );

      add( assignmentsNode );

      add( waitingForNode );
   }

   public String projectName()
   {
      return projectName;
   }

   @Override
   public OverviewProjectsNode getParent()
   {
      return (OverviewProjectsNode) super.getParent();
   }

   public void notifyEvent( DomainEvent event )
   {
      for (Object child : children)
      {
         EventListener listener = (EventListener) child;
         listener.notifyEvent( event );
      }
   }
}