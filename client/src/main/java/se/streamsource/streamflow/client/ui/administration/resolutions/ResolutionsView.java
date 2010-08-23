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

package se.streamsource.streamflow.client.ui.administration.resolutions;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import com.jgoodies.forms.factories.Borders;

import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import java.awt.*;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

/**
 * Admin of resolutions.
 */
public class ResolutionsView
      extends JPanel
{
   ResolutionsModel model;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   public JList list;

   public ResolutionsView( @Service ApplicationContext context, @Uses ResolutionsModel model )
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "rename" ) );
//      options.add( am.get( "showUsages" ) );
      options.add( am.get( "remove" ) );

      JScrollPane scrollPane = new JScrollPane();
      EventList<LinkValue> itemValueEventList = new SortedList<LinkValue>( model.getEventList(), new LinkComparator() );
      list = new JList( new EventListModel<LinkValue>( itemValueEventList ) );
      list.setCellRenderer( new LinkListCellRenderer() );
      scrollPane.setViewportView( list );
      add( scrollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( new OptionsAction(options) ) );
      add( toolbar, BorderLayout.SOUTH );

      list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ), am.get( "rename" ), am.get( "showUsages" ) ) );

      addAncestorListener( new RefreshWhenVisible( model, this ) );
   }

   @Action
   public void add()
   {
      NameDialog dialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_resolution_title ) );

      if ( Strings.notEmpty( dialog.name() ) )
      {
         list.clearSelection();
         model.create( dialog.name() );
         model.refresh();
      }
   }

   @Action
   public void remove()
   {
      LinkValue selected = (LinkValue) list.getSelectedValue();

      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( selected.text().get() );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         model.remove( selected );
         model.refresh();
      }
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
   public void rename()
   {
      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog );

      if ( Strings.notEmpty( dialog.name() ) )
      {
         model.changeDescription( (LinkValue) list.getSelectedValue(), dialog.name() );
         model.refresh();
      }
   }
}