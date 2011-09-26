/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.ui.administration.filters;

import static se.streamsource.streamflow.client.util.i18n.icon;
import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.PopupAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;

/**
 * TODO
 */
public class ActionsView extends JPanel implements TransactionListener
{
   private @Service DialogService dialogs;

   private @Structure Module module;

   private ActionsModel model;

   public JList list;
   
   public ActionsView(@Service ApplicationContext context, @Structure final Module module,
         @Uses final ActionsModel model)
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      JScrollPane scrollPane = new JScrollPane();
      EventList<LinkValue> itemValueEventList = model.getUnsortedList();
      list = new JList( new EventListModel<LinkValue>( itemValueEventList ) );
      scrollPane.setViewportView( list );
      add( scrollPane, BorderLayout.CENTER );

      JPopupMenu addPopup = new JPopupMenu();
      addPopup.add( am.get( "addEmail" ));
      addPopup.add( am.get( "addClose" ));
      PopupAction popupAction = new PopupAction( addPopup, (String) text( AdministrationResources.add_filter_action ), icon( Icons.add, 16));
      
      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "remove" ) );
      
      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( popupAction ) );
      toolbar.add( new JButton( new OptionsAction(options) ) );
      add( toolbar, BorderLayout.SOUTH );

      list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      list.setCellRenderer( new DefaultListCellRenderer()
      {

         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
               boolean cellHasFocus)
         {
            if (value instanceof LinkValue)
            {
               LinkValue itemValue = (LinkValue) value;
               String val = "";
               if (itemValue != null)
               {
                  if ("closeaction".equals( itemValue.rel().get() ))
                  {
                     val = text( AdministrationResources.close_case );
                  } else
                  {
                     val = text( AdministrationResources.send_email_to, itemValue.text().get() );
                  }
               }

               return super.getListCellRendererComponent( list, val, index, isSelected, cellHasFocus );
            } else
               return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
         }
      } );

      new RefreshWhenShowing( this, model );
   }

   @Action
   public Task addEmail()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder( SelectLinkDialog.class )
            .use( model.getPossibleRecipients() ).newInstance();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_recipient_title ) );

      if (dialog.getSelectedLinks() != null && dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command() throws Exception
            {
               model.createEmailAction( dialog.getSelectedLink() );
            }
         };
      } else
         return null;
   }

   @Action
   public Task addClose()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject( ConfirmationDialog.class );
      dialog.setCustomMessage( text( AdministrationResources.add_filter_close_action_confirmation ) );
      dialogs.showOkCancelHelpDialog( this, dialog, text( StreamflowResources.confirmation ) );

      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command() throws Exception
            {
               model.closeCaseAction();
            }
         };
      } else
         return null;
   }
   
   
   @Action
   public Task remove()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject( ConfirmationDialog.class );
      final LinkValue linkValue = model.getIndex().links().get().get( list.getSelectedIndex() );
      
      if ( "emailaction".equals(linkValue.rel().get()))
      {
         dialog.setCustomMessage( text( AdministrationResources.remove_action_confirmation, text( AdministrationResources.send_email_to, linkValue.text().get() )));         
      }
      else if( "closeaction".equals(linkValue.rel().get()))
      {
         dialog.setCustomMessage( text( AdministrationResources.remove_action_confirmation, text( AdministrationResources.close_case)));         
      }
      
      dialogs.showOkCancelHelpDialog( this, dialog, text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.remove( linkValue );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (Events.matches( Events.withNames( "updatedFilter" ), transactions ))
         model.refresh();
   }
}
