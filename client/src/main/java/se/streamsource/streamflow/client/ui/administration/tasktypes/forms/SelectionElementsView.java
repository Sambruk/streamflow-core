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

package se.streamsource.streamflow.client.ui.administration.tasktypes.forms;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;

/**
 * JAVADOC
 */
public class SelectionElementsView
      extends JPanel
{
   private JXTable elementList;

   private JButton upButton;
   private JButton downButton;
   private SelectionElementsModel model;

   public SelectionElementsView( @Service ApplicationContext context,
                                 @Uses SelectionElementsModel model )
   {
      super( new BorderLayout() );
      this.model = model;
      JScrollPane scrollPanel = new JScrollPane();
      ActionMap am = context.getActionMap( this );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      upButton = new JButton( am.get( "up" ) );
      toolbar.add( upButton );
      downButton = new JButton( am.get( "down" ) );
      toolbar.add( downButton );
      upButton.setEnabled( false );
      downButton.setEnabled( false );

      model.refresh();
      elementList = new JXTable( model );
      elementList.setSortable( false );
      elementList.setDefaultRenderer( String.class, new DefaultTableCellRenderer() {

         @Override
         public Component getTableCellRendererComponent( JTable jTable, Object o, boolean b, boolean b1, int i, int i1 )
         {
            if ("".equals( o ))
            {
               Component cell = super.getTableCellRendererComponent( jTable, i18n.text( WorkspaceResources.name_label ), b, b1, i, i1 );
               cell.setForeground( Color.GRAY );
               return cell;
            }
            return super.getTableCellRendererComponent( jTable, o, b, b1, i, i1 );
         }
      });

      scrollPanel.setViewportView( elementList );

      add( scrollPanel, BorderLayout.CENTER );
      add( toolbar, BorderLayout.SOUTH );

      elementList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );
      elementList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      elementList.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {

         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = elementList.getSelectedRow();

               upButton.setEnabled( idx != 0 );
               downButton.setEnabled( idx != elementList.getModel().getRowCount() - 1 );
               if (idx == -1)
               {
                  upButton.setEnabled( false );
                  downButton.setEnabled( false );
               }
            }
         }
      } );
   }

   @org.jdesktop.application.Action
   public void add()
   {
      model.addElement( );
   }


   @org.jdesktop.application.Action
   public void remove( )
   {
      int index = elementList.getSelectedRow();
      if (index != -1)
      {
         model.removeElement( index );
         elementList.clearSelection();
      }
   }

   @org.jdesktop.application.Action
   public void up()
   {
      int index = elementList.getSelectedRow();
      if (index > 0 && index < elementList.getModel().getRowCount())
      {
         model.moveElement( "up", index );
         elementList.setRowSelectionInterval( index-1, index-1 );
      }
   }

   @org.jdesktop.application.Action
   public void down()
   {
      int index = elementList.getSelectedRow();
      if (index >= 0 && index < elementList.getModel().getRowCount() - 1)
      {
         model.moveElement( "down", index );
         elementList.setRowSelectionInterval( index+1, index+1 );
      }
   }
}