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

package se.streamsource.streamflow.client.ui.overview;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class OverviewProjectAssignmentsNode
      extends DefaultMutableTreeNode
      implements EventListener
{
   @Uses
   private TaskTableModel model;

   @Override
   public String toString()
   {
      String text = i18n.text( OverviewResources.assignments_node );
      int total = model.getEventList().size();
      if (total > 0)
      {
         text += " (" + total + ")";
      } else
      {
         text += "                ";
      }

      return text;
   }

   @Override
   public OverviewProjectNode getParent()
   {
      return (OverviewProjectNode) super.getParent();
   }

   public TaskTableModel taskTableModel()
   {
      return model;
   }

   public void notifyEvent( DomainEvent event )
   {
      model.notifyEvent( event );
   }
}