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
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowApplication;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class PossibleFormsView extends JPanel
      implements ActionListener
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private
   @Service
   StreamflowApplication main;

   private PossibleFormsModel modelForms;

   public PossibleFormsView()
   {
      setLayout( new GridLayout( 0, 1 ) );
      setBorder( BorderFactory.createEmptyBorder( 2,0,2,2 ) );
      setFocusable( false );
   }

   public void setFormsModel( PossibleFormsModel modelForm )
   {
      this.modelForms = modelForm;
      initComponents();
   }

   private void initComponents()
   {
      this.setVisible( false );
      removeAll();

      EventList<LinkValue> formList = modelForms.getForms();

      int count = 0;
      for (LinkValue itemValue : formList)
      {
         PossibleFormView formView = new PossibleFormView( itemValue );
         
         formView.setPreferredSize( new Dimension( 145, 25 ));
         formView.setMinimumSize( new Dimension( 145, 25 ) );
         formView.setMaximumSize( new Dimension( 145, 25 ) );
         formView.addActionListener( this );
         add( formView, Component.LEFT_ALIGNMENT );                           
         count++;
      }

      //this.setPreferredSize( new Dimension( 290, (30 * count)/2 ) );

      this.invalidate();
      this.repaint();
      this.setVisible(true);

   }

   @Override
   public void setEnabled( boolean enabled )
   {
      for (Component component : getComponents())
      {
         component.setEnabled( enabled );
      }
      super.setEnabled( enabled );
   }
   
   public void actionPerformed( ActionEvent e )
   {
	   // Open up the wizard with the correct form for submission.

      if ( e.getSource() instanceof PossibleFormView )
      {
      final PossibleFormView form = (PossibleFormView)e.getSource();   
      FormSubmissionModel model = modelForms.getFormSubmitModel( form.form().id().get() );

      Wizard wizard = WizardPage.createWizard( model.getTitle(), model.getPages(), new WizardPage.WizardResultProducer()
      {

         public Object finish( Map map ) throws WizardException
         {
            modelForms.submit( EntityReference.parseEntityReference( form.form().id().get()) );
            return null;
         }

         public boolean cancel( Map map )
         {
            modelForms.discard( EntityReference.parseEntityReference( form.form().id().get()) );
            return true;
         }
      } );
      Point onScreen = main.getMainFrame().getLocationOnScreen();
      WizardDisplayer.showWizard(wizard, new Rectangle(onScreen, new Dimension( 800, 600 )));

      }
   }
}
