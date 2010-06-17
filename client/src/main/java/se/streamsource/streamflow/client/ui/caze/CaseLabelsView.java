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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.qi4j.api.entity.EntityReference;
import se.streamsource.dci.value.LinkValue;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CaseLabelsView extends JPanel implements ListEventListener, ActionListener
{
   private CaseLabelsModel modelCase;

   private boolean useBorders;

   public CaseLabelsView()
   {
      setLayout( new FlowLayout( FlowLayout.LEFT ) );
      //setBorder( BorderFactory.createLineBorder( Color.BLUE, 1));
   }

   public void setLabelsModel( CaseLabelsModel modelCase )
   {
      this.modelCase = modelCase;
      modelCase.getLabels().addListEventListener( this );
      initComponents();
   }

   private void initComponents()
   {
      removeAll();

      for (int i = 0; i < modelCase.getLabels().size(); i++)
      {
         LinkValue linkValue = modelCase.getLabels().get( i );
         RemovableLabel label = new RemovableLabel( linkValue, useBorders );
         label.addActionListener( this );

         add( label );
      }

      revalidate();
      repaint();

   }

   public void setEnabled( boolean enabled )
   {
      for (Component component : getComponents())
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
      modelCase.removeLabel( EntityReference.parseEntityReference( label.link().id().get() ) );
   }

   public void useBorders( boolean useBorders )
   {
      this.useBorders = useBorders;
   }
}
