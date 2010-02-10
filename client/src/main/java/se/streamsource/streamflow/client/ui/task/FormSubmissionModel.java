/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import org.netbeans.spi.wizard.WizardPage;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValueDTO;
import se.streamsource.streamflow.domain.form.SubmittedPageValue;

import java.util.List;
import java.util.ArrayList;

/**
 * Model for handling a form submission and subsequently submitting it
 */
public class FormSubmissionModel
{
   private ValueBuilderFactory vbf;
   private CommandQueryClient client;
   private FormSubmissionValue formSubmission;
   private List<WizardPage> pages;

   public FormSubmissionModel(@Uses CommandQueryClient client,
                              @Structure ObjectBuilderFactory obf,
                              @Structure ValueBuilderFactory vbf)
   {
      this.vbf = vbf;
      this.client = client;
      try
      {
         formSubmission = (FormSubmissionValue) client.query( "formsubmission", FormSubmissionValue.class ).buildWith().prototype();
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_get_form_submission, e );
      }
      pages = new ArrayList<WizardPage>( formSubmission.pages().get().size() );
      for (SubmittedPageValue page : formSubmission.pages().get())
      {
         if ( page.fields().get() != null && page.fields().get().size() >0 )
         {
            pages.add( obf.newObjectBuilder( FormSubmissionWizardPage.class)
                  .use( this, page ).newInstance() );
         }
      }
   }

   public WizardPage[] getPages()
   {
      WizardPage[] wizardPages = new WizardPage[ pages.size() ];
      pages.toArray( wizardPages );
      return wizardPages;
   }

   public String getTitle()
   {
      return formSubmission.description().get();
   }

   public void updateField( EntityReference reference, String name ) throws ResourceException
   {
      ValueBuilder<FieldValueDTO> builder = vbf.newValueBuilder( FieldValueDTO.class );
      builder.prototype().field().set( reference );
      builder.prototype().value().set( name );

      client.putCommand( "updatefield", builder.newInstance());
   }
}