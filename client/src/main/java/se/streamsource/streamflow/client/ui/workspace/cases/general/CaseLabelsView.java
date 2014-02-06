/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CaseLabelsView
        extends JPanel
        implements ListEventListener, TransactionListener
{
   @Service
   private DialogService dialogs;

   @Structure
   Module module;

   private CaseLabelsModel model;
   private StreamflowButton actionButton;

   private boolean textBold;

   public CaseLabelsView(@Service ApplicationContext context, @Uses CaseLabelsModel model)
   {
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
         LinkValue knowledgeBase = null;
         try
         {
            knowledgeBase = model.getKnowledgeBaseLink(linkValue);
         } catch (Exception e)
         {
            // Ignore
         }

         RemovableLabel label = new RemovableLabel(linkValue, knowledgeBase);
         if( textBold )
         {
            label.getLabel().setFont( label.getLabel().getFont().deriveFont( Font.BOLD ) );
         }

         label.setToolTipText( linkValue.text().get() );
         label.getButton().addActionListener( getActionMap().get( "remove" ) );
         label.setEnabled(isEnabled());
         add(label);
      }

      revalidate();
      repaint();
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task addLabel()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(model.getPossibleLabels()).newInstance();
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
            model.removeLabel(label.getRemoveLink());
         }
      };
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (Events.matches( Events.withNames( "addedLabel", "removedLabel", "changedStatus" ), transactions ))
      {
         model.refresh();
         if( actionButton != null )
            actionButton.requestFocusInWindow();
      }
   }

   /**
    * Set button relation and make sure the button is requesting focus on click.
    * This should ensure that the value from a focused input field is saved before the button action.
    * @param button The referenced button.
    */
   public void setButtonRelation(StreamflowButton button)
   {
      this.actionButton = button;
      actionButton.addActionListener( new ActionListener()
      {

         public void actionPerformed(ActionEvent e)
         {
            actionButton.requestFocusInWindow();
         }
      } );
   }

   public void setTextBold( boolean textBold )
   {
      this.textBold = textBold;
   }
}
