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
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class WorkspaceUserInboxNode
      extends DefaultMutableTreeNode
      implements EventListener
{
   @Uses
   private WorkspaceUserInboxModel model;

   public String toString()
   {
      String text = i18n.text( WorkspaceResources.inboxes_node );
      int unread = model.count();
      if (unread > 0)
      {
         text += " (" + unread + ")";
      } else
      {
         text += "                ";
      }

      return text;
   }

   @Override
   public WorkspaceUserNode getParent()
   {
      return (WorkspaceUserNode) super.getParent();
   }

   public WorkspaceUserInboxModel inboxModel()
   {
      return model;
   }

   public void notifyEvent( DomainEvent event )
   {
      model.notifyEvent( event );
   }
}