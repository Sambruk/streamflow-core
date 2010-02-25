package se.streamsource.streamflow.client.ui.task;

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

public class TaskLabelsView extends JPanel implements ListEventListener, ActionListener
{
   private TaskLabelsModel modelTask;

   private JPanel labelPanel;

   public TaskLabelsView()
   {
      setLayout( new BorderLayout() );

      labelPanel = new JPanel( new ModifiedFlowLayout( FlowLayout.LEFT ) );
      //labelPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
      //setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));

      add( labelPanel, BorderLayout.SOUTH );
   }

   public void setLabelsModel( TaskLabelsModel modelTask )
   {
      this.modelTask = modelTask;
      modelTask.getLabels().addListEventListener( this );
      initComponents();
   }

   private void initComponents()
   {
      labelPanel.removeAll();

      for (int i = 0; i < modelTask.getLabels().size(); i++)
      {
         ListItemValue itemValue = modelTask.getLabels().get( i );
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
      modelTask.removeLabel( label.item().entity().get() );
   }
}
