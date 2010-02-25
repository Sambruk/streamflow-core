package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class PossibleFormsView extends JPanel implements ListEventListener, ActionListener
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private
   @Service
   StreamFlowApplication main;

   private PossibleFormsModel modelForms;

   private JPanel formPanel;

   public PossibleFormsView()
   {
      setLayout( new BorderLayout() );

      formPanel = new JPanel( new ModifiedFlowLayout( FlowLayout.LEFT ) );

      add( formPanel, BorderLayout.SOUTH );
   }

   public void setFormsModel( PossibleFormsModel modelForm )
   {
      this.modelForms = modelForm;
      modelForm.getForms().addListEventListener( this );
      initComponents();
   }

   private void initComponents()
   {
      formPanel.removeAll();

      EventList<LinkValue> formList = modelForms.getForms();
      if (formList.size() > 0)
         setBorder( BorderFactory.createEtchedBorder() );
      else
         setBorder( BorderFactory.createEmptyBorder() );

      for (LinkValue itemValue : formList)
      {
         PossibleFormView formView = new PossibleFormView( itemValue );
         formView.addActionListener( this );
         formPanel.add( formView );
      }

      formPanel.revalidate();
      formPanel.repaint();

   }

   @Override
   public void setEnabled( boolean enabled )
   {
      for (Component component : formPanel.getComponents())
      {
         component.setEnabled( enabled );
      }
      super.setEnabled( enabled );
   }

   public void listChanged( ListEvent listEvent )
   {
      initComponents();
   }

   public void actionPerformed( ActionEvent e )
   {
	   // Open up the wizard with the correct form for submission.
      Component component = ((Component) e.getSource());
      final PossibleFormView formsView = (PossibleFormView) component.getParent();

      FormSubmissionModel model = modelForms.getFormSubmitModel( formsView.form().id().get() );

      Wizard wizard = WizardPage.createWizard( model.getTitle(), model.getPages(), new WizardPage.WizardResultProducer()
      {

         public Object finish( Map map ) throws WizardException
         {
            modelForms.submit( EntityReference.parseEntityReference( formsView.form().id().get()) );
            return null;
         }

         public boolean cancel( Map map )
         {
            modelForms.discard( EntityReference.parseEntityReference( formsView.form().id().get()) );
            return true;
         }
      } );
      Point onScreen = main.getMainFrame().getLocationOnScreen();
      WizardDisplayer.showWizard(wizard, new Rectangle(onScreen, new Dimension( 800, 600 )));
   }
}
