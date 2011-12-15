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

package se.streamsource.streamflow.client.ui.administration.labels;

import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ActionMap;
import se.streamsource.streamflow.client.util.StreamflowButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.ResourceActionEnabler;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;

/**
 * Admin of labels.
 */
public class LabelsView
      extends JPanel
   implements TransactionListener
{
   LabelsModel model;

   @Structure
   Module module;

   @Service
   DialogService dialogs;

   public JList list;

   public LabelsView( @Service ApplicationContext context,
                              @Uses final LabelsModel model)
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "rename" ) );
      options.add( am.get( "move" ) );
      options.add( am.get( "showUsages" ) );
      options.add( am.get( "knowledgeBase" ) );
      options.add( am.get( "remove" ) );

      JScrollPane scrollPane = new JScrollPane();
      EventList<LinkValue> itemValueEventList = model.getList();
      list = new JList( new EventListModel<LinkValue>( itemValueEventList ) );
      list.setCellRenderer( new LinkListCellRenderer() );
      scrollPane.setViewportView( list );
      add( scrollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new StreamflowButton( am.get( "add" ) ) );
      toolbar.add( new StreamflowButton( new OptionsAction(options) ) );
      add( toolbar, BorderLayout.SOUTH );

      list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ), am.get( "rename" ), am.get( "move" ), am.get("knowledgeBase"), am.get( "showUsages" ) ) );

      new RefreshWhenShowing( this, model );

      new RefreshWhenShowing(options, new ResourceActionEnabler(am.get("knowledgeBase"))
      {
         @Override
         protected ResourceValue getResource()
         {
            model.refresh();
            return model.getResourceValue();
         }
      });
   }

   @Action
   public Task add()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_label_title ) );

      if (!Strings.empty( dialog.name() ) )
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
      final LinkValue selected = (LinkValue) list.getSelectedValue();

      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
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
   public Task move()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(model.getPossibleMoveTo(selected)).newInstance();
      dialog.setPreferredSize( new Dimension(200,300) );

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_move_to ) );

      if (dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.moveForm(selected, dialog.getSelectedLink());
            }
         };
      } else
         return null;
   }

   @Action
   public void showUsages()
   {
      LinkValue item = (LinkValue) list.getSelectedValue();
      EventList<LinkValue> usageList = model.usages( item );

      JList list = new JList();
      list.setCellRenderer( new LinkListCellRenderer() );
      list.setModel( new EventListModel<LinkValue>(usageList) );

      dialogs.showOkDialog( this, list );

      usageList.dispose();
   }

   @Action
   public void knowledgeBase() throws URISyntaxException, IOException
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();

      LinkValue url = model.getKnowledgeBaseLink(selected);

      Desktop.getDesktop().browse(new URI(url.href().get()));
   }

   @Action
   public Task rename()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();

      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.rename_label_title ) );

      if (!Strings.empty( dialog.name() ) )
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

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );
   }
}