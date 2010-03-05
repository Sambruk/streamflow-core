/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration.projects;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.Set;

/**
 * JAVADOC
 */
public class ProjectMembersView
      extends JPanel
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Uses
   ObjectBuilder<SelectUsersAndGroupsDialog> selectUsersAndGroups;

   @Structure
   ObjectBuilderFactory obf;

   public JXList membersList;
   private ProjectMembersModel membersModel;

   public ProjectMembersView( @Service ApplicationContext context,
                              @Uses final ProjectMembersModel membersModel )
   {
      super( new BorderLayout() );
      this.membersModel = membersModel;

      setActionMap( context.getActionMap( this ) );

      membersList = new JXList( new EventListModel<LinkValue>(membersModel.getMembers()) );

      membersList.setCellRenderer( new LinkListCellRenderer() );

      add( membersList, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( getActionMap().get( "add" ) ) );
      toolbar.add( new JButton( getActionMap().get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
      membersList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( getActionMap().get( "remove" ) ) );


      addAncestorListener( new RefreshWhenVisible( membersModel, this ) );
   }


   @Action
   public void add()
   {
      SelectUsersAndGroupsDialog dialog = selectUsersAndGroups.use( membersModel.getFilterResource() ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_user_or_group_title ) );
      Set<String> members = dialog.getUsersAndGroups();
      if (members != null)
      {
         membersModel.addMembers( members );
         membersModel.refresh();
      }
   }

   @Action
   public void remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamFlowResources.confirmation ) );
      if (dialog.isConfirmed() && !membersList.isSelectionEmpty())
      {
         membersModel.removeMember( (LinkValue) membersList.getSelectedValue() );
         membersModel.refresh();
      }
   }
}