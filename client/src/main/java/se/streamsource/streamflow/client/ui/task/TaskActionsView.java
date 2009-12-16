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
import se.streamsource.streamflow.client.ui.workspace.SelectUserOrProjectDialog2;
import se.streamsource.streamflow.domain.task.TaskActions;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * JAVADOC
 */
public class TaskActionsView
   extends JPanel
{
   @Uses
   protected ObjectBuilder<SelectUserOrProjectDialog2> userOrProjectSelectionDialog2;

   @Service
   DialogService dialogs;

   private TaskActionsModel model;

   public TaskActionsView(@Service ApplicationContext context)
   {
      setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );

      setActionMap( context.getActionMap(this ));
   }

   public void refresh()
   {
      TaskActions actions = model.actions();

      removeAll();

      ActionMap am = getActionMap();

      for (String action : actions.actions().get())
      {
         javax.swing.Action action1 = am.get( action );
         if (action1 != null)
         {
            JButton button = new JButton( action1 );
            add(button);
         }
      }

      revalidate();
      repaint();
   }

   // Task actions
   @Action
   public void accept()
   {

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
      SelectUserOrProjectDialog2 dialog = userOrProjectSelectionDialog2.use( model ).newInstance();
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

   }

   @Action
   public void drop()
   {
      model.drop();
      refresh();
   }

   @Action
   public void forward()
   {
      SelectUserOrProjectDialog2 dialog = userOrProjectSelectionDialog2.use( model ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog);

      if (dialog.getSelected() != null)
      {
         model.forward( dialog.getSelected() );
         refresh();
      }
   }

   @Action
   public void reject()
   {

   }

   public void setModel( TaskActionsModel taskActionsModel )
   {
      this.model = taskActionsModel;
   }
}
