/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
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
import se.streamsource.streamflow.client.util.FileNameExtensionFilter;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import static se.streamsource.streamflow.client.util.i18n.*;

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
   private ActionMap am;

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
      am = context.getActionMap( this );

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
      toolbar.add( new StreamflowButton( am.get( "importvalues" ) ) );
      toolbar.add( new StreamflowButton( am.get( "removeall" ) ) );

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

      elementList.getSelectionModel().addListSelectionListener(
            new SelectionActionEnabler( am.get( "remove" ), am.get( "rename" ), am.get( "up" ), am.get( "down" ) ){

               @Override
               public boolean isSelectedValueValid( Action action )
               {
                  boolean result = true;
                  try
                  {
                     int selectedIndex = elementList.getSelectedIndex();
                     if( selectedIndex == -1 )
                     {
                        result = false;
                     } else if (action.equals( am.get( "up" ) ))
                     {
                        if (selectedIndex == 0)
                           result = false;
                     }else if (action.equals( am.get( "down" ) ))
                     {
                        if (selectedIndex == elementList.getModel().getSize() - 1)
                           result = false;
                     }
                  } catch( IndexOutOfBoundsException e )
                  {
                     result = false;
                  }
                  return result;
               }
            });
      elementList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
   }

   @org.jdesktop.application.Action
   public Task add()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);

      dialogs.showOkCancelHelpDialog( this, dialog , i18n.text( AdministrationResources.name_label ));

      if ( !Strings.empty( dialog.name() ) )
      {
         if ( dialog.name().contains( "[" ) || dialog.name().contains( "]" ))
         {
            dialogs.showOkDialog( this, new JLabel( i18n.text( AdministrationResources.no_such_character )), i18n.text( AdministrationResources.illegal_name ) );
            return null;
         } else
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
         elementList.clearSelection();
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

   @org.jdesktop.application.Action
   public Task importvalues()
   {

      // Ask the user for a file to import values from
      // Can be either Excels or CVS format
      final JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
      fileChooser.setMultiSelectionEnabled( false );
      fileChooser.addChoosableFileFilter( new FileNameExtensionFilter(
            text( AdministrationResources.import_files ), true, "xls", "csv", "txt" ) );
      fileChooser.setDialogTitle( text( AdministrationResources.import_values ) );
      int returnVal = fileChooser.showOpenDialog( this );
      if (returnVal != JFileChooser.APPROVE_OPTION)
      {
         return null;
      }

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.importValues( fileChooser.getSelectedFile().getAbsoluteFile() );
         }
      };
   }

   @org.jdesktop.application.Action
   public Task removeall()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setRemovalMessage( i18n.text( StreamflowResources.all ));

      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed() )
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.removeAll();
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