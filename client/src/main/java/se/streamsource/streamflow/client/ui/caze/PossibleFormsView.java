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
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;
import static se.streamsource.streamflow.util.Iterables.*;

public class PossibleFormsView extends JPanel
      implements ActionListener, Refreshable
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private
   @Service
   StreamflowApplication main;

   private PossibleFormsModel modelForms;
   public Wizard wizard;
   private final CommandQueryClient client;

   public PossibleFormsView(@Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      this.client = client;
      setLayout( new GridLayout( 0, 1 ) );
      setBorder( BorderFactory.createEmptyBorder( 2, 0, 2, 2 ) );
      setFocusable( false );

      modelForms = obf.newObjectBuilder( PossibleFormsModel.class ).use( client ).newInstance();

      new RefreshWhenVisible(this, this);
   }

   public void refresh()
   {
      modelForms.refresh();

      removeAll();

      EventList<LinkValue> formList = modelForms.getForms();

      for (LinkValue itemValue : formList)
      {
         PossibleFormView formView = new PossibleFormView( itemValue );

         formView.setPreferredSize( new Dimension( 145, 25 ) );
         formView.setMinimumSize( new Dimension( 145, 25 ) );
         formView.setMaximumSize( new Dimension( 145, 25 ) );
         formView.addActionListener( this );
         add( formView, Component.LEFT_ALIGNMENT );
      }

      this.invalidate();
      this.repaint();
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

      if (e.getSource() instanceof PossibleFormView)
      {
         final PossibleFormView form = (PossibleFormView) e.getSource();

         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( EntityReference.parseEntityReference( form.form().id().get() ) );

         EntityReferenceDTO formSubmission = null;
         try
         {
            formSubmission = client.query( "formsubmission", builder.newInstance(), EntityReferenceDTO.class );
         } catch (ResourceException e1)
         {
            // Create it
            client.postCommand( "createformsubmission", builder.newInstance() );
            formSubmission = client.query( "formsubmission", builder.newInstance(), EntityReferenceDTO.class );
         }

         FormSubmissionModel model = obf.newObjectBuilder( FormSubmissionModel.class )
               .use( client.getSubClient( formSubmission.entity().get().identity() ) ).newInstance();

         wizard = WizardPage.createWizard( model.getTitle(), model.getPages(), new WizardPage.WizardResultProducer()
         {

            public Object finish( Map map ) throws WizardException
            {
               modelForms.submit( EntityReference.parseEntityReference( form.form().id().get() ) );
               return null;
            }

            public boolean cancel( Map map )
            {
               modelForms.discard( EntityReference.parseEntityReference( form.form().id().get() ) );
               return true;
            }
         } );
         Point onScreen = main.getMainFrame().getLocationOnScreen();
         WizardDisplayer.showWizard( wizard, new Rectangle( onScreen, new Dimension( 800, 600 ) ) );

      }
   }
}
