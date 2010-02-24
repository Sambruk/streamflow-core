/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaskConversationParticipantsView extends JPanel implements ListEventListener, ActionListener
{
   private TaskConversationParticipantsModel model;

   private JPanel labelPanel;


   public TaskConversationParticipantsView(@Service ApplicationContext context)
   {
      setLayout( new BorderLayout() );

      ActionMap am = context.getActionMap( this );
      labelPanel = new JPanel();

      add( labelPanel, BorderLayout.WEST );

      JButton openParticipantDialog = new JButton(am.get( "openParticipantDialog" ));
      add( openParticipantDialog, BorderLayout.EAST);
   }

   public void setModel( TaskConversationParticipantsModel model )
   {
      this.model = model;
      model.getParticipants().addListEventListener( this );
      initComponents();
   }

   private void initComponents()
   {
      /*labelPanel.removeAll();

      for (int i = 0; i < model.getParticipants().size(); i++)
      {
         LinkValue itemValue = model.getParticipants().get( i );
         RemovableLabel label = new RemovableLabel( itemValue );
         label.addActionListener( this );
         labelPanel.add( label );
      }

      labelPanel.revalidate();
      labelPanel.repaint();
      */
   }

   public void listChanged( ListEvent listEvent )
   {
      initComponents();
   }

   public void actionPerformed( ActionEvent e )
   {
      Component component = ((Component) e.getSource());
      RemovableLabel label = (RemovableLabel) component.getParent();
      model.removeParticipant( label.item().entity().get() );
   }

   @Action
   public void openParticipantDialog()
   {

   }
}
