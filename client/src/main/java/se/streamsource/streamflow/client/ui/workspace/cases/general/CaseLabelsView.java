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

package se.streamsource.streamflow.client.ui.workspace.cases.general;

import ca.odell.glazedlists.event.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CaseLabelsView
        extends JPanel
        implements ListEventListener, TransactionListener
{
   @Service
   private DialogService dialogs;

   @Uses
   private ObjectBuilder<SelectLinkDialog> labelSelectionDialog;

   private CaseLabelsModel model;
   private JButton actionButton;

   public CaseLabelsView(@Service ApplicationContext context, @Uses CaseLabelsModel model, @Structure ObjectBuilderFactory obf )
   {
      this.actionButton = actionButton;
      setActionMap( context.getActionMap(this ));
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            CaseLabelsView.class, this ) );
      
      this.model = model;
      model.getLabels().addListEventListener( this );

      setLayout(new FlowLayout(FlowLayout.LEFT));
      //setBorder( BorderFactory.createLineBorder( Color.BLUE, 1));
      new RefreshWhenShowing(this, model);
   }

   public CaseLabelsModel getModel()
   {
      return model;
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);
      for (Component component : getComponents())
      {
         component.setEnabled(enabled);
      }
   }

   public void listChanged(ListEvent listEvent)
   {
      removeAll();

      for (int i = 0; i < model.getLabels().size(); i++)
      {
         LinkValue linkValue = model.getLabels().get(i);
         RemovableLabel label = new RemovableLabel(linkValue, false);
         label.setToolTipText(linkValue.text().get());
         label.getButton().addActionListener(getActionMap().get("remove"));
         label.setEnabled(isEnabled());
         add(label);
      }

      revalidate();
      repaint();
   }

   @Action
   public Task addLabel()
   {
      final SelectLinkDialog dialog = labelSelectionDialog.use(model.getPossibleLabels()).newInstance();
      dialog.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      dialogs.showOkCancelHelpDialog(actionButton == null ? this : actionButton, dialog);

      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            for (LinkValue listItemValue : dialog.getSelectedLinks())
            {
               model.addLabel(listItemValue);
            }
         }
      };
   }


   @Action
   public Task remove(final ActionEvent e)
   {
      return new CommandTask()
      {
         @Override
         public void command()
                 throws Exception
         {
            Component component = ((Component) e.getSource());
            RemovableLabel label = (RemovableLabel) component.getParent();
            model.removeLabel(label.link());
         }
      };
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      model.notifyTransactions(transactions);
   }

   public void setButtonRelation(JButton button)
   {
      this.actionButton = button;
   }
}
