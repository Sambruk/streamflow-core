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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CaseLabelsView extends JPanel implements ListEventListener, ActionListener
{
   private CaseLabelsModel modelCase;

   private JPanel labelPanel;

   public CaseLabelsView()
   {
      setLayout( new BorderLayout() );

      labelPanel = new JPanel( new ModifiedFlowLayout( FlowLayout.LEFT ) );
      //labelPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
      //setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));

      add( labelPanel, BorderLayout.SOUTH );
   }

   public void setLabelsModel( CaseLabelsModel modelCase )
   {
      this.modelCase = modelCase;
      modelCase.getLabels().addListEventListener( this );
      initComponents();
   }

   private void initComponents()
   {
      labelPanel.removeAll();

      for (int i = 0; i < modelCase.getLabels().size(); i++)
      {
         ListItemValue itemValue = modelCase.getLabels().get( i );
         RemovableLabel label = new RemovableLabel( itemValue );
         label.addActionListener( this );
         labelPanel.add( label );
      }

      labelPanel.revalidate();
      labelPanel.repaint();

   }

   public void setEnabled( boolean enabled )
   {
      for (Component component : labelPanel.getComponents())
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
      modelCase.removeLabel( label.item().entity().get() );
   }
}
