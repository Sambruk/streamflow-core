package se.streamsource.streamflow.client.ui.task;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

public class PossibleFormsView extends JPanel implements ListEventListener, ActionListener
{
   private FormsListModel modelForms;

   private JPanel formPanel;

   public PossibleFormsView()
   {
      setLayout( new BorderLayout() );

      formPanel = new JPanel( new ModifiedFlowLayout( FlowLayout.LEFT ) );

      add( formPanel, BorderLayout.SOUTH );
   }

   public void setFormsModel( FormsListModel modelForm )
   {
      this.modelForms = modelForm;
      modelForm.getForms().addListEventListener( this );
      initComponents();
   }

   private void initComponents()
   {
      formPanel.removeAll();

      for (int i = 0; i < modelForms.getForms().size(); i++)
      {
         ListItemValue itemValue = modelForms.getForms().get( i );
         PossibleFormView formView = new PossibleFormView( itemValue );
         formView.addActionListener( this );
         formPanel.add( formView );
      }

      formPanel.revalidate();
      formPanel.repaint();

   }

   public void listChanged( ListEvent listEvent )
   {
      initComponents();
   }

   public void actionPerformed( ActionEvent e )
   {
	   // Open up the wizard with the correct form for submission.
      Component component = ((Component) e.getSource());
      PossibleFormView labelView = (PossibleFormView) component.getParent();
//      modelForms.removeLabel( labelView.label().entity().get() );
   }
}
