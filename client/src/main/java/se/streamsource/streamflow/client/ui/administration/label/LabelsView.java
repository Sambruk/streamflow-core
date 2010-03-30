/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.administration.label;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.JListPopup;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.*;
import java.awt.*;

/**
 * Admin of labels on organizational level.
 */
public class LabelsView
      extends JPanel
{
   LabelsModel model;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   public JListPopup labelList;

   public LabelsView( @Service ApplicationContext context, @Uses LabelsModel model )
   {
      super( new BorderLayout() );
      this.model = model;

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      JPopupMenu popup = new JPopupMenu();
      popup.add( am.get( "rename" ) );

      JScrollPane scrollPane = new JScrollPane();
      EventList<LinkValue> itemValueEventList = new SortedList<LinkValue>( model.getLabelList(), new LinkComparator() );
      labelList = new JListPopup( new EventListModel<LinkValue>( itemValueEventList ), popup );
      labelList.setCellRenderer( new LinkListCellRenderer() );
      scrollPane.setViewportView( labelList );
      add( scrollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      labelList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      addAncestorListener( new RefreshWhenVisible( model, this ) );
   }

   @Action
   public void add()
   {
      NameDialog dialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_label_title ) );

      if (dialog.name() != null)
      {
         labelList.clearSelection();
         model.createLabel( dialog.name() );
         model.refresh();
      }
   }

   @Action
   public void remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamFlowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         LinkValue selected = (LinkValue) labelList.getSelectedValue();
         model.removeLabel( selected );
         model.refresh();
      }
   }

   @Action
   public void rename()
   {
      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog );

      if (dialog.name() != null)
      {
         model.changeDescription( (LinkValue)labelList.getSelectedValue(), dialog.name() );
         model.refresh();
      }
   }
}