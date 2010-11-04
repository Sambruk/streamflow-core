/**
 *
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

package se.streamsource.streamflow.client.ui.administration.labels;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkComparator;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * Admin of labels.
 */
public class LabelsView
      extends JPanel
   implements TransactionListener
{
   LabelsModel model;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   public JList labelList;

   public LabelsView( @Service ApplicationContext context,
                              @Uses final CommandQueryClient client,
                              @Structure ObjectBuilderFactory obf)
   {
      super( new BorderLayout() );
      this.model = obf.newObjectBuilder( LabelsModel.class ).use( client ).newInstance();
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "rename" ) );
      options.add( am.get( "showUsages" ) );
      options.add( am.get( "remove" ) );

      JScrollPane scrollPane = new JScrollPane();
      EventList<LinkValue> itemValueEventList = new SortedList<LinkValue>( model.getList(), new LinkComparator() );
      labelList = new JList( new EventListModel<LinkValue>( itemValueEventList ) );
      labelList.setCellRenderer( new LinkListCellRenderer() );
      scrollPane.setViewportView( labelList );
      add( scrollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( new OptionsAction(options) ) );
      add( toolbar, BorderLayout.SOUTH );

      labelList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ), am.get( "rename" ), am.get( "showUsages" ) ) );

      new RefreshWhenVisible( this, model );
   }

   @Action
   public Task add()
   {
      final NameDialog dialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_label_title ) );

      if (Strings.notEmpty( dialog.name() ) )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.create( dialog.name() );
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final LinkValue selected = (LinkValue) labelList.getSelectedValue();

      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( selected.text().get() );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.remove( selected );
            }
         };
      } else
         return null;
   }

   @Action
   public void showUsages()
   {
      LinkValue item = (LinkValue) labelList.getSelectedValue();
      EventList<LinkValue> usageList = model.usages( item );

      JList list = new JList();
      list.setCellRenderer( new LinkListCellRenderer() );
      list.setModel( new EventListModel<LinkValue>(usageList) );

      dialogs.showOkDialog( this, list );

      usageList.dispose();
   }

   @Action
   public Task rename()
   {
      final LinkValue selected = (LinkValue) labelList.getSelectedValue();

      final NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.rename_label_title ) );

      if (Strings.notEmpty( dialog.name() ) )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeDescription( selected, dialog.name() );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      model.notifyTransactions( transactions );
   }
}