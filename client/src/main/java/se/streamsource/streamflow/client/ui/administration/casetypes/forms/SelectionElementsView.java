/*
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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

/**
 * JAVADOC
 */
public class SelectionElementsView
      extends JPanel
{
   private JXList elementList;

   private JButton upButton;
   private JButton downButton;
   private SelectionElementsModel model;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;


   public SelectionElementsView( @Service ApplicationContext context,
                                 @Uses SelectionElementsModel model )
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("4dlu, 4dlu, 4dlu, 4dlu"));

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
      toolbar.add( new JButton( am.get( "rename" ) ) );

      model.refresh();
      elementList = new JXList( new EventListModel<String>(model.getEventList()) );
      elementList.setCellRenderer( new DefaultListCellRenderer()
      {
         @Override
         public Component getListCellRendererComponent( JList jList, Object o, int i, boolean b, boolean b1 )
         {
            if ("".equals( o ))
            {
               Component cell = super.getListCellRendererComponent( jList, i18n.text( WorkspaceResources.name_label ), i, b, b1);
               cell.setForeground( Color.GRAY );
               return cell;
            }
            return super.getListCellRendererComponent( jList, o, i, b, b1 );    //To change body of overridden methods use File | Settings | File Templates.
         }
      });

      scrollPanel.setViewportView( elementList );

      add( scrollPanel, BorderLayout.CENTER );
      add( toolbar, BorderLayout.SOUTH );

      elementList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );
      elementList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "rename" ) ) );
      elementList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      elementList.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {

         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = elementList.getSelectedIndex();

               upButton.setEnabled( idx != 0 );
               downButton.setEnabled( idx != elementList.getModel().getSize() - 1 );
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
      NameDialog dialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, dialog , i18n.text( AdministrationResources.name_label ));

      if ( Strings.notEmpty( dialog.name() ) )
      {
         model.addElement( dialog.name() );
         elementList.setSelectedIndex( elementList.getModel().getSize() - 1 );
      }
   }


   @org.jdesktop.application.Action
   public void remove( )
   {
      int index = elementList.getSelectedIndex();

      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( elementList.getStringAt( index ));

      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (index != -1 && dialog.isConfirmed() )
      {
         model.removeElement( index );
         elementList.setSelectedIndex( index == 0 && elementList.getModel().getSize() >= 1 ? index : index - 1);
      }
   }

   @org.jdesktop.application.Action
   public void up()
   {
      int index = elementList.getSelectedIndex();
      if (index > 0 && index < elementList.getModel().getSize())
      {
         model.moveElement( "up", index );
         elementList.setSelectedIndex( index-1 );
      }
   }

   @org.jdesktop.application.Action
   public void down()
   {
      int index = elementList.getSelectedIndex();
      if (index >= 0 && index < elementList.getModel().getSize() - 1)
      {
         model.moveElement( "down", index );
         elementList.setSelectedIndex( index+1 );
      }
   }

   @org.jdesktop.application.Action
   public void rename()
   {
      NameDialog dialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, dialog , i18n.text( AdministrationResources.rename ));

      if ( Strings.notEmpty( dialog.name() ))
      {
         model.changeElementName( dialog.name(), elementList.getSelectedIndex() );
      }
   }

}