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

package se.streamsource.streamflow.client.ui.caze.conversations;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.client.ui.caze.RemovableLabel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

public class AllParticipantsView extends JPanel implements ListEventListener, ActionListener
{
   private AllParticipantsModel modelParticipants;

   private JPanel participantsPanel;

   public AllParticipantsView()
   {
      setLayout( new BorderLayout() );

      participantsPanel = new JPanel( new ModifiedFlowLayout( FlowLayout.LEFT ) );

      add( participantsPanel, BorderLayout.SOUTH );
   }

   public void setLabelsModel( AllParticipantsModel modelParticipants )
   {
      this.modelParticipants = modelParticipants;
      modelParticipants.getParticipants().addListEventListener( this );
      initComponents();
   }

   private void initComponents()
   {
      participantsPanel.removeAll();

      for (int i = 0; i < modelParticipants.getParticipants().size(); i++)
      {
         ListItemValue itemValue = modelParticipants.getParticipants().get( i );
         RemovableLabel label = new RemovableLabel( itemValue );
         label.addActionListener( this );
         participantsPanel.add( label );
      }

      participantsPanel.revalidate();
      participantsPanel.repaint();

   }

   public void setEnabled( boolean enabled )
   {
      for (Component component : participantsPanel.getComponents())
      {
         component.setEnabled( enabled );
      }
   }

   public void listChanged( ListEvent listEvent )
   {
      initComponents();
   }

   public void actionPerformed( ActionEvent e )
   {
      Component component = ((Component) e.getSource());
      RemovableLabel label = (RemovableLabel) component.getParent();
      modelParticipants.removeParticipant( label.item().entity().get() );
   }
}
