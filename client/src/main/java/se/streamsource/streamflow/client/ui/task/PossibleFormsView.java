package se.streamsource.streamflow.client.ui.task;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JPanel;

import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.api.wizard.WizardDisplayer;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;

public class PossibleFormsView extends JPanel implements ListEventListener, ActionListener
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private
   @Service
   StreamFlowApplication main;


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
      PossibleFormView formsView = (PossibleFormView) component.getParent();

      final FormSubmissionModel model = modelForms.getFormSubmitModel( formsView.form().entity().get().identity() );

      Wizard wizard = WizardPage.createWizard( model.getTitle(), model.getPages(), new WizardPage.WizardResultProducer()
      {

         public Object finish( Map map ) throws WizardException
         {
            model.submit();
            return null;
         }

         public boolean cancel( Map map )
         {
            return true;
         }
      } );
      Point onScreen = main.getMainFrame().getLocationOnScreen();
      WizardDisplayer.showWizard(wizard, new Rectangle(onScreen, new Dimension( 800, 600 )));
   }
}
