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

package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;


public class AccessPointsView
   extends JPanel
{
   AccessPointsModel model;

      @Uses
      Iterable<NameDialog> nameDialogs;

      @Service
      DialogService dialogs;

      @Uses
      Iterable<ConfirmationDialog> confirmationDialog;

      public JList accessPointList;

      public AccessPointsView( @Service ApplicationContext context, @Uses AccessPointsModel model )
      {
         super( new BorderLayout() );
         this.model = model;

         setBorder( Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

         ActionMap am = context.getActionMap( this );
         setActionMap( am );

         JPopupMenu options = new JPopupMenu();
         options.add( am.get( "rename" ) );
         options.add( am.get( "remove" ) );

         JScrollPane scrollPane = new JScrollPane();
         accessPointList = new JList( new EventListModel<LinkValue>( new SortedList<LinkValue>(model.getAccessPointsList(), new LinkComparator()) ) );
         accessPointList.setCellRenderer( new LinkListCellRenderer() );
         scrollPane.setViewportView( accessPointList );
         add( scrollPane, BorderLayout.CENTER );

         JPanel toolbar = new JPanel();
         toolbar.add( new JButton( am.get( "add" ) ) );
         toolbar.add( new JButton( new OptionsAction(options) ));
         add( toolbar, BorderLayout.SOUTH );

         accessPointList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ), am.get( "rename" ) ) );
      }

      @Action
      public void add()
      {
         NameDialog dialog = nameDialogs.iterator().next();

         dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_accesspoint_title ) );

         if (dialog.name() != null)
         {
            model.newAccessPoint( dialog.name() );
         }
      }

      @Action
      public void remove()
      {
         ConfirmationDialog dialog = confirmationDialog.iterator().next();
         dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
         if (dialog.isConfirmed())
         {
            LinkValue selected = (LinkValue) accessPointList.getSelectedValue();
            model.removeAccessPoint( selected.id().get() );
         }
      }

      @Action
      public void rename()
      {
         NameDialog dialog = nameDialogs.iterator().next();
         dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.change_accesspoint_title ) );

         if (dialog.name() != null)
         {
            model.changeDescription( (LinkValue) accessPointList.getSelectedValue(), dialog.name() );
         }
      }

   public JList getAccessPointsList()
   {
      return accessPointList;
   }
}
