/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.RemovableLabel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

public class ConversationParticipantsView
      extends JPanel
      implements ListEventListener, TransactionListener
{
   @Service
   DialogService dialogs;

   @Uses
   ObjectBuilder<SelectLinkDialog> participantsDialog;

   ConversationParticipantsModel model;
   private JPanel participants;

   public ConversationParticipantsView(@Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      super(new BorderLayout());

      setActionMap( context.getActionMap(this ));

      model = obf.newObjectBuilder( ConversationParticipantsModel.class ).use( client ).newInstance();
      model.participants().addListEventListener( this );

      participants = new JPanel(new FlowLayout( FlowLayout.LEFT ));
      participants.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
      add( participants, BorderLayout.CENTER);

      javax.swing.Action addParticipantsAction = getActionMap().get(
            "addParticipants" );
      JButton addParticipants = new JButton( addParticipantsAction );
      addParticipants.registerKeyboardAction( addParticipantsAction,
            (KeyStroke) addParticipantsAction
                  .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      add(addParticipants, BorderLayout.EAST);

      new RefreshWhenShowing(this, model);
   }

   private void initComponents()
   {
      participants.removeAll();

      for (int i = 0; i < model.participants().size(); i++)
      {
         LinkValue link = model.participants().get( i );
         RemovableLabel label = new RemovableLabel( link, RemovableLabel.LEFT );
         label.getButton().addActionListener( getActionMap().get("removeParticipant" ));
         participants.add( label );
      }

      participants.revalidate();
      participants.repaint();

   }

   public void listChanged( ListEvent listEvent )
   {
      initComponents();
   }

   @Action
   public Task addParticipants()
   {
      final SelectLinkDialog dialog = participantsDialog.use( model.possibleParticipants() ).newInstance();
      dialog.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( CaseResources.choose_participant ) );

      return new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            for (LinkValue participant : dialog.getSelectedLinks())
            {
               model.addParticipant( participant );
            }
         }
      };
   }


   @Action
   public Task removeParticipant(final ActionEvent e)
   {
      return new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            Component component = ((Component) e.getSource());
            RemovableLabel label = (RemovableLabel) component.getParent();
            model.removeParticipant( label.link() );
         }
      };
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withNames("addedParticipant", "removedParticipant"  ), transactions ))
      {
         model.refresh();
      }
   }

}
