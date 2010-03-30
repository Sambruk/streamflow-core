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

package se.streamsource.streamflow.client.ui.administration.roles;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class RolesView
      extends JPanel
{
   RolesModel model;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   public JList roleList;

   public RolesView( @Service ApplicationContext context, @Uses final RolesModel model )
   {
      super( new BorderLayout() );
      this.model = model;

      setActionMap( context.getActionMap( this ) );

      roleList = new JList( new EventListModel<LinkValue>(new SortedList<LinkValue>(model.getRoles(), new LinkComparator())) );

      roleList.setCellRenderer( new ListItemListCellRenderer() );
      add( roleList, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( getActionMap().get( "add" ) ) );
      toolbar.add( new JButton( getActionMap().get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      addAncestorListener( new RefreshWhenVisible( model, this ) );
   }

   @Action
   public void add()
   {
      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_role_title ) );
      String name = dialog.name();
      if (name != null)
      {
         model.createRole( name );
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
         LinkValue selected = (LinkValue) roleList.getSelectedValue();
         model.removeRole( selected.id().get() );
         model.refresh();
      }
   }

}