package se.streamsource.streamflow.client.ui.task.conversations;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.client.ui.task.RemovableLabel;
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
