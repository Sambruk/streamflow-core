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

package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.ui.workspace.SelectUserOrProjectDialog;
import se.streamsource.streamflow.client.ui.workspace.SelectTaskTypeDialog;
import se.streamsource.streamflow.client.ui.administration.label.SelectLabelsDialog;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class TaskActionsView
   extends JPanel
{
   @Uses
   protected ObjectBuilder<SelectUserOrProjectDialog> userOrProjectSelectionDialog;

   @Uses
   protected ObjectBuilder<SelectTaskTypeDialog> taskTypeDialog;

   @Uses
   protected ObjectBuilder<TaskLabelsDialog> labelSelectionDialog;

   @Service
   DialogService dialogs;

   private TaskActionsModel model;

   private JPanel actionsPanel = new JPanel();

   public TaskActionsView(@Service ApplicationContext context)
   {
      setLayout( new BorderLayout());

      actionsPanel.setLayout(new GridLayout(0, 1));

      add( actionsPanel, BorderLayout.NORTH);

      setActionMap( context.getActionMap(this ));
   }

   public void refresh()
   {
      Actions actions = model.actions();

      actionsPanel.removeAll();

      ActionMap am = getActionMap();

      for (String action : actions.actions().get())
      {
         javax.swing.Action action1 = am.get( action );
         if (action1 != null)
         {
            JButton button = new JButton( action1 );
            button.setHorizontalAlignment( SwingConstants.LEFT );
            actionsPanel.add(button);
         }
      }

      revalidate();
      repaint();
   }

   // Task actions
   @Action
   public void accept()
   {
      model.accept();
      refresh();
   }

   @Action
   public void assign()
   {
      model.assignToMe();
      refresh();
   }

   @Action
   public void complete()
   {
      model.complete();
      refresh();
   }

   @Action
   public void delegate()
   {
      SelectUserOrProjectDialog dialog = userOrProjectSelectionDialog.use( model ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog);

      if (dialog.getSelected() != null)
      {
         model.delegate( dialog.getSelected() );
         refresh();
      }

   }

   @Action
   public void delete()
   {
      model.delete();
   }

   @Action
   public void done()
   {
      model.done();
      refresh();
   }

   @Action
   public void drop()
   {
      model.drop();
      refresh();
   }

   @Action
   public void sendto()
   {
      SelectUserOrProjectDialog dialog = userOrProjectSelectionDialog.use( model ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog);

      if (dialog.getSelected() != null)
      {
         model.sendTo( dialog.getSelected() );
         refresh();
      }
   }

   @Action
   public void label()
   {
      TaskLabelsDialog dialog = labelSelectionDialog.use(model.getPossibleLabels()).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog);

      if (dialog.getSelectedLabels() != null)
      {
         for (ListItemValue listItemValue : dialog.getSelectedLabels())
         {
            model.addLabel( listItemValue.entity().get() );
         }
      }
   }

   @Action
   public void redo()
   {
      model.redo();
      refresh();
   }

   @Action
   public void reactivate()
   {
      model.reactivate();
      refresh();
   }

   @Action
   public void reject()
   {
      model.reject();
      refresh();
   }

   @Action
   public void tasktype()
   {
      SelectTaskTypeDialog dialog = taskTypeDialog.use( model.getPossibleTaskTypes() ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog);

      if (dialog.getSelected() != null)
      {
         model.taskType( dialog.getSelected() );
         refresh();
      }
   }

   @Action
   public void unassign()
   {
      model.unassign();
      refresh();
   }

   public void setModel( TaskActionsModel taskActionsModel )
   {
      this.model = taskActionsModel;
   }
}
