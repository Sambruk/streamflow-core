/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.PinstripePainter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.resource.task.TaskValue;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base class for all views of task lists.
 */
public class TaskTableView
      extends JPanel
{
   @Service
   protected DialogService dialogs;

   @Service
   protected StreamFlowApplication application;

   protected JXTable taskTable;
   protected TaskCreationNode taskCreation;
   protected TaskTableModel model;
   private TasksDetailView2 detailsView;
   protected EntityReference dialogSelection;

   public void init( @Service ApplicationContext context,
                     @Uses @Optional TaskCreationNode node,
                     @Uses final TasksModel tasksModel,
                     @Uses final TaskTableModel model,
                     @Uses final TasksDetailView2 detailsView,
                     @Uses TableFormat tableFormat)
   {
      setLayout( new BorderLayout() );
      this.taskCreation = node;
      this.model = model;
      this.detailsView = detailsView;
      setLayout( new BorderLayout() );

      ActionMap am = context.getActionMap( TaskTableView.class, this );
      setActionMap( am );
      MacOsUIWrapper.convertAccelerators(context.getActionMap(
				TaskTableView.class, this));

      // Toolbar
      JPanel toolbar = new JPanel();

      // Table
      EventJXTableModel tableModel = new EventJXTableModel(model.getEventList(), tableFormat);
      taskTable = new JXTable( tableModel );
      taskTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      taskTable.getActionMap().getParent().setParent( am );
      taskTable.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS ) );
      taskTable.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS ) );

      JScrollPane taskScrollPane = new JScrollPane( taskTable );

      add( toolbar, BorderLayout.NORTH );

      taskTable.getColumn( 1 ).setPreferredWidth( 150 );
      taskTable.getColumn( 1 ).setMaxWidth( 150 );
      taskTable.getColumn( 2 ).setPreferredWidth( 150 );
      taskTable.getColumn( 2 ).setMaxWidth( 150 );
      taskTable.getColumn( taskTable.getColumnCount() - 1 ).setMaxWidth( 50 );
      taskTable.getColumn( taskTable.getColumnCount() - 1 ).setResizable( false );

      taskTable.setAutoCreateColumnsFromModel( false );

      add(taskScrollPane, BorderLayout.CENTER);

      JXTable.BooleanEditor completableEditor = new JXTable.BooleanEditor();
      taskTable.setDefaultEditor( Boolean.class, completableEditor );
      taskTable.setDefaultRenderer( Date.class, new DefaultTableRenderer( new StringValue()
      {
         private SimpleDateFormat format = new SimpleDateFormat();

         public String getString( Object value )
         {
            if (value == null) return "";
            Date time = (Date) value;
            return format.format( time );
         }
      } ) );
      taskTable.setDefaultRenderer( States.class, new TaskStatusTableCellRenderer() );

      taskTable.addHighlighter( HighlighterFactory.createAlternateStriping() );

      PinstripePainter p = new PinstripePainter();
      p.setAngle(90);
      p.setPaint(Color.LIGHT_GRAY);

      taskTable.addHighlighter( new PainterHighlighter( new HighlightPredicate()
      {
         public boolean isHighlighted( Component component, ComponentAdapter componentAdapter )
         {
            if (componentAdapter != null)
            {
               Object value = componentAdapter.getValue( componentAdapter.getColumnCount() - 1 );
               return value.equals( States.DROPPED) || value.equals( States.COMPLETED);
            } else
            return false;
         }
      }, p ) );

      buildToolbar( toolbar );

      taskTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {
         TaskValue selectedTask;

         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               try
               {
                  if (taskTable.getSelectionModel().isSelectionEmpty())
                  {
//                     detailsView.removeCurrent();
                  } else
                  {
                     TaskValue value = null;
                     try
                     {
                        value = getSelectedTask();
                     } catch (Exception e1)
                     {
                        // Ignore
                        return;
                     }

                     if (value == selectedTask)
                        return;

                     selectedTask = value;

                     TaskModel taskModel = tasksModel.task( value.id().get() );

                     detailsView.show( taskModel );
                  }
               } catch (Exception e1)
               {
                  throw new OperationException( TaskResources.could_not_view_details, e1 );
               }
            }
         }
      } );

      addFocusListener( new FocusAdapter()
      {
         public void focusGained( FocusEvent e )
         {
            taskTable.requestFocusInWindow();
         }
      } );
   }

   protected void buildToolbar( JPanel toolbar )
   {
      if (taskCreation != null)
         addToolbarButton( toolbar, "createTask" );
      addToolbarButton( toolbar, "refresh" );
   }

   protected Action addToolbarButton( JPanel toolbar, String name )
   {
      ActionMap am = getActionMap();
      Action action = am.get( name );
      action.putValue( Action.SMALL_ICON, i18n.icon( (ImageIcon) action.getValue( Action.SMALL_ICON ), 16 ) );
      JButton button = new JButton(action);
      button.registerKeyboardAction(action, (KeyStroke) action
				.getValue(javax.swing.Action.ACCELERATOR_KEY),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
//      NotificationGlassPane.registerButton(button);

      toolbar.add( button );
      return action;
   }

   public JXTable getTaskTable()
   {
      return taskTable;
   }

   public TasksDetailView2 getTaskDetails()
   {
      return detailsView;
   }

   public TaskValue getSelectedTask()
   {
      int selectedRow = getTaskTable().getSelectedRow();
      if (selectedRow == -1)
         return null;
      else
         return model.getEventList().get( getTaskTable().convertRowIndexToModel( selectedRow ) );
   }

   @org.jdesktop.application.Action()
   public void createTask() throws ResourceException
   {
      taskCreation.createTask();
      model.refresh();

      JXTable table = getTaskTable();
      int index = table.convertRowIndexToView( model.getEventList().size() - 1 );
      table.getSelectionModel().setSelectionInterval( index, index );
      table.scrollRowToVisible( index );

      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            detailsView.requestFocusInWindow();
         }
      } );
   }

   @org.jdesktop.application.Action
   public void refresh() throws ResourceException
   {
      model.refresh();
   }
}