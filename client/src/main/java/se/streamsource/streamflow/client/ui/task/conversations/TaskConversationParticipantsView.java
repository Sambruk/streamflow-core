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

package se.streamsource.streamflow.client.ui.task.conversations;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.jdesktop.application.Action;
import se.streamsource.streamflow.client.ui.task.RemovableLabel;
import se.streamsource.streamflow.infrastructure.application.LinkValue;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaskConversationParticipantsView extends JPanel implements ListEventListener, ActionListener
{
   
   TaskConversationParticipantsModel model;

   private JPanel labelPanel;


   public TaskConversationParticipantsView()
   {
      setLayout( new BorderLayout() );

      labelPanel = new JPanel();

      add( labelPanel, BorderLayout.WEST );

      //JButton openParticipantDialog = new JButton(am.get( "openParticipantDialog" ));
      //add( openParticipantDialog, BorderLayout.EAST);
   }

   public void setModel( TaskConversationParticipantsModel model )
   {
      this.model = model;
      this.model.refresh();
      model.participants().addListEventListener( this );
      initComponents();
   }

   private void initComponents()
   {
      labelPanel.removeAll();

      for (int i = 0; i < model.participants().size(); i++)
      {
         LinkValue link = model.participants().get( i );
         RemovableLabel label = new RemovableLabel( link, new FlowLayout( FlowLayout.LEFT, 2, 1 ), RemovableLabel.LEFT );
         label.addActionListener( this );
         labelPanel.add( label );
      }

      labelPanel.revalidate();
      labelPanel.repaint();

   }

   public void listChanged( ListEvent listEvent )
   {
      initComponents();
   }

   public void actionPerformed( ActionEvent e )
   {
      Component component = ((Component) e.getSource());
      RemovableLabel label = (RemovableLabel) component.getParent();
      model.removeParticipant( label.link() );
   }

   @Action
   public void openParticipantDialog()
   {

   }
}
