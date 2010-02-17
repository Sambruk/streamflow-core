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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventQuery;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.ForEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class TasksDetailView2
      extends JPanel
{
   private TaskDetailView current = null;

   @Structure
   ObjectBuilderFactory obf;

   private TransactionVisitor subscriber;

   public TasksDetailView2(  @Uses TaskDetailView current, @Service EventSource events )
   {
      super(new BorderLayout());

      setPreferredSize( new Dimension( getWidth(), 500 ) );

      this.current = current;

/*
      subscriber = new ForEvents( new EventQuery().withNames( "changedDescription" ), new EventVisitor()
      {
         public boolean visit( DomainEvent event )
         {
            String id = event.entity().get();
            for (int i = 0; i < getComponentCount(); i++)
            {
               TaskDetailView detailView = (TaskDetailView) getComponentAt( i );
               TaskModel taskModel = detailView.getTaskModel();

               if (taskModel.taskId().equals( id ))
               {
                  setTitleAt( i, getTaskDescription( taskModel ) );
                  break;
               }
            }

            return false;
         }
      } );

      events.registerListener( subscriber );
*/
   }

   public void show( final TaskModel task )
   {
      if (getComponents().length == 0)
         add(current, BorderLayout.CENTER);

      current.setTaskModel( task );
   }
}