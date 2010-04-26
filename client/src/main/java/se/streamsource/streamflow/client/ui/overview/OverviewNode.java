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

package se.streamsource.streamflow.client.ui.overview;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class OverviewNode
      extends DefaultMutableTreeNode
      implements EventListener
{
   private OverviewProjectsNode projectsNode;

   public OverviewNode( @Uses OverviewProjectsNode projectsNode,
                        @Uses AccountModel accountModel )
   {
      super( accountModel );
      this.projectsNode = projectsNode;

      add( projectsNode );
   }

   @Override
   public AccountModel getUserObject()
   {
      return (AccountModel) super.getUserObject();
   }

   public OverviewProjectsNode getProjectsNode()
   {
      return projectsNode;
   }

   public void notifyEvent( DomainEvent event )
   {
      projectsNode.notifyEvent( event );
   }
}