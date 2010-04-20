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

package se.streamsource.streamflow.client.ui.administration.tasktypes;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.label.SelectionDialog;
import se.streamsource.streamflow.client.ui.administration.tasktypes.forms.FormModel;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import java.awt.*;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

/**
 * JAVADOC
 */
public class TaskTypesView
      extends JPanel
{
   TaskTypesModel model;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Uses
   ObjectBuilder<SelectionDialog> possibleMoveToDialogs;

   public JList taskTypesList;

   public TaskTypesView( @Service ApplicationContext context, @Uses TaskTypesModel model )
   {
      super( new BorderLayout() );
      this.model = model;

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "rename" ) );
      options.add( am.get( "move" ) );
      options.add( am.get( "showUsages" ) );
      options.add( am.get( "remove" ) );

      JScrollPane scrollPane = new JScrollPane();
      taskTypesList = new JList( new EventListModel<LinkValue>( new SortedList<LinkValue>(model.getTaskTypeList(), new LinkComparator()) ) );
      taskTypesList.setCellRenderer( new LinkListCellRenderer() );
      scrollPane.setViewportView( taskTypesList );
      add( scrollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( new OptionsAction(options) ) );
      add( toolbar, BorderLayout.SOUTH );

      taskTypesList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ), am.get("rename"), am.get("showUsages") ) );
   }

   @Action
   public void add()
   {
      NameDialog dialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_tasktype_title ) );

      if (dialog.name() != null)
      {
         model.newTaskType( dialog.name() );
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
         LinkValue selected = (LinkValue) taskTypesList.getSelectedValue();
         model.removeTaskType( selected.id().get() );
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
         LinkValue item = (LinkValue) taskTypesList.getSelectedValue();
         model.getTaskTypeModel( item.id().get() ).changeDescription( dialog.name() );
         model.refresh();
      }
   }

   @Action
   public void showUsages()
   {
      LinkValue item = (LinkValue) taskTypesList.getSelectedValue();
      EventList<LinkValue> usageList = model.getTaskTypeModel( item.id().get() ).usages();

      JList list = new JList();
      list.setCellRenderer( new LinkListCellRenderer() );
      list.setModel( new EventListModel<LinkValue>(usageList) );

      dialogs.showOkDialog( this, list );

      usageList.dispose();
   }

   @Action
   public void move()
   {
      LinkValue selected = (LinkValue) taskTypesList.getSelectedValue();
      TaskTypeModel taskTypeModel = model.getTaskTypeModel( selected.id().get() );
      SelectionDialog dialog = possibleMoveToDialogs.use(taskTypeModel.getPossibleMoveTo()).newInstance();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_move_to ) );

      if (dialog.getSelectedLinks() != null)
      {
         for (LinkValue linkValue : dialog.getSelectedLinks())
         {
            taskTypeModel.moveTaskType( linkValue );
            model.refresh();
         }
      }
   }

   public JList getTaskTypesList()
   {
      return taskTypesList;
   }

}