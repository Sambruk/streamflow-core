/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.users;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.UserEntityDTO;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.FileNameExtensionFilter;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.ResourceActionEnabler;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Color;
import java.awt.Component;

import static se.streamsource.streamflow.client.util.i18n.*;

public class UsersAdministrationListView
   extends ListDetailView
{
   UsersAdministrationListModel model;

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   public UsersAdministrationListView( @Service ApplicationContext context, @Uses final UsersAdministrationListModel model )
   {
      this.model = model;

      ActionMap am = context.getActionMap( this );
      setActionMap( am );


      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("createuser"), new SelectionActionEnabler( new Action[]{am.get( "importusers" )} ){
               @Override
               public boolean isSelectedValueValid( Action action )
               {
                  return action.isEnabled();
               }
            },
          new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            return module.objectBuilderFactory().newObjectBuilder(UserAdministrationDetailView.class).use( model.newResourceModel(detailLink)).newInstance();
         }
      }, new ListCellRenderer()
      {
         private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

         public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
         {
            UserEntityDTO user = (UserEntityDTO) value;
            StringBuilder sb = new StringBuilder( );
            if ( !user.joined().get() )
            {
               sb.append( "<html><s>" ).append( user.text().get() ).append( "</s></html>" );
            } else
            {
               sb.append( "<html>").append( user.text().get() ).append( "</html>" );
            }
            JLabel label = (JLabel) defaultRenderer.getListCellRendererComponent( list, sb.toString(), index, isSelected, cellHasFocus );
            if ( user.disabled().get() )
            {
               label.setForeground( Color.GRAY );
            }
            return label;
         }
      });

      //new RefreshWhenShowing(this, model);

      ResourceActionEnabler resourceActionEnabler = new ResourceActionEnabler(
            am.get( "createuser" ),
            am.get( "importusers" )
      )
      {
         @Override
         protected ResourceValue getResource()
         {
            model.refresh();
            return model.getResourceValue();
         }
      };
      new RefreshWhenShowing( this, resourceActionEnabler );
   }

   @org.jdesktop.application.Action
   public Task createuser()
   {
      final CreateUserDialog dialog = module.objectBuilderFactory().newObject(CreateUserDialog.class);
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_user_title ) );

      if ( dialog.userCommand() != null )
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.createUser( dialog.userCommand() );
            }
         };
      } else
      {
         return null;
      }
   }

   @org.jdesktop.application.Action
   public Task importusers()
   {

      // Ask the user for a file to import user/pwd pairs from
      // Can be either Excels or CVS format
      final JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
      fileChooser.setMultiSelectionEnabled( false );
      fileChooser.addChoosableFileFilter( new FileNameExtensionFilter(
            text( AdministrationResources.import_files ), true, "xls", "csv", "txt" ) );
      fileChooser.setDialogTitle( text( AdministrationResources.import_users ) );
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
            model.importUsers( fileChooser.getSelectedFile().getAbsoluteFile() );
         }
      };
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );

      super.notifyTransactions( transactions );
   }

}