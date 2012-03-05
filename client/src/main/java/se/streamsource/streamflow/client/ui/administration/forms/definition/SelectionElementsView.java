/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;

/**
 * JAVADOC
 */
public class SelectionElementsView
      extends JPanel
   implements TransactionListener
{
   private JXList elementList;

   private StreamflowButton upButton;
   private StreamflowButton downButton;
   private SelectionElementsModel model;

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   public SelectionElementsView( @Service ApplicationContext context,
                                 @Uses SelectionElementsModel model )
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("4dlu, 4dlu, 4dlu, 4dlu"));

      JScrollPane scrollPanel = new JScrollPane();
      ActionMap am = context.getActionMap( this );

      JPanel toolbar = new JPanel();
      toolbar.add( new StreamflowButton( am.get( "add" ) ) );
      toolbar.add( new StreamflowButton( am.get( "remove" ) ) );
      upButton = new StreamflowButton( am.get( "up" ) );
      toolbar.add( upButton );
      downButton = new StreamflowButton( am.get( "down" ) );
      toolbar.add( downButton );
      upButton.setEnabled( false );
      downButton.setEnabled( false );
      toolbar.add( new StreamflowButton( am.get( "rename" ) ) );

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
   public Task add()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);

      dialogs.showOkCancelHelpDialog( this, dialog , i18n.text( AdministrationResources.name_label ));

      if ( !Strings.empty( dialog.name() ) )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.addElement( dialog.name() );
            }
         };
      }
      else return null;
   }


   @org.jdesktop.application.Action
   public Task remove( )
   {
      final int index = elementList.getSelectedIndex();

      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setRemovalMessage( elementList.getStringAt( index ));

      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (index != -1 && dialog.isConfirmed() )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.removeElement( index );
            }
         };
      } else
         return null;
   }

   @org.jdesktop.application.Action
   public Task up()
   {
      final int index = elementList.getSelectedIndex();
      if (index > 0 && index < elementList.getModel().getSize())
      {
         elementList.setSelectedIndex( index-1 );

         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.moveElement( "up", index );
            }
         };
      } else
         return null;
   }

   @org.jdesktop.application.Action
   public Task down()
   {
      final int index = elementList.getSelectedIndex();
      if (index >= 0 && index < elementList.getModel().getSize() - 1)
      {
         elementList.setSelectedIndex( index+1 );
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.moveElement( "down", index );
            }
         };
      } else
         return null;
   }

   @org.jdesktop.application.Action
   public Task rename()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);

      dialogs.showOkCancelHelpDialog( this, dialog , i18n.text( AdministrationResources.rename ));

      if ( !Strings.empty( dialog.name() ))
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeElementName( dialog.name(), elementList.getSelectedIndex() );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.refresh();
   }
}