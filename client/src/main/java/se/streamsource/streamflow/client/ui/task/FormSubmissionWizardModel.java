/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.task.TaskSubmittedFormsClientResource;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;

import javax.swing.JButton;

/**
 * JAVADOC
 */
public class FormSubmissionWizardModel
{

   @Structure
   ObjectBuilderFactory obf;

   @Uses
   TaskSubmittedFormsClientResource submittedFormsResource;

   private JButton previous;
   private JButton submit;

   WeakModelMap<String, FormSubmitModel> formSubmitModels = new WeakModelMap<String, FormSubmitModel>()
   {
      protected FormSubmitModel newModel( String key )
      {
         return obf.newObjectBuilder( FormSubmitModel.class ).
               use( submittedFormsResource.formDefinition( key ) ).newInstance();
      }
   };


   public FormSubmitModel getFormSubmitModel( String id )
   {
      return formSubmitModels.get( id );
   }

   public void submitForm( SubmitFormDTO submitDTO )
   {
      try
      {
         submittedFormsResource.submitForm( submitDTO );
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_submit_form, e );
      }
   }

   public JButton previousButton( JButton previousButton )
   {
      this.previous = previousButton;
      return previous;
   }

   public JButton submitButton( JButton submitButton )
   {
      this.submit = submitButton;
      return submit;
   }

   public void initialStep()
   {
      previous.setEnabled( false );
      submit.setEnabled( false );
   }

   public void nextStep()
   {
      previous.setEnabled( true );
      submit.setEnabled( true );
   }

   public void previousStep()
   {
      previous.setEnabled( false );
      submit.setEnabled( false );
   }
}