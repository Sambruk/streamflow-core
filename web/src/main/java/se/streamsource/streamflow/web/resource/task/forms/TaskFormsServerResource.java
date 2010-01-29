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

package se.streamsource.streamflow.web.resource.task.forms;

import static org.qi4j.api.entity.EntityReference.getEntityReference;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.web.domain.entity.form.FormsQueries;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.entity.form.FormSubmissionsQueries;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmissions;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;
import se.streamsource.streamflow.resource.task.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

import java.util.Date;

/**
 * Mapped to:
 * /tasks/{task}/forms
 */
public class TaskFormsServerResource
      extends CommandQueryServerResource
{
   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   Qi4jSPI spi;

   public TaskFormsServerResource()
   {
      setNegotiated( true );
      getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
   }

   public SubmittedFormsListDTO listsubmittedforms()
   {
      String formsQueryId = getRequest().getAttributes().get( "task" ).toString();
      SubmittedFormsQueries forms = uowf.currentUnitOfWork().get( SubmittedFormsQueries.class, formsQueryId );

      return forms.getSubmittedForms();
   }

   public EffectiveFieldsDTO effectivefields()
   {
      String formsQueryId = getRequest().getAttributes().get( "task" ).toString();

      SubmittedFormsQueries fields = uowf.currentUnitOfWork().get( SubmittedFormsQueries.class, formsQueryId );

      return fields.effectiveFields();
   }

   public SubmittedFormDTO submittedform( IntegerDTO index) throws ResourceException
   {
      String formsQueryId = getRequest().getAttributes().get( "task" ).toString();
      SubmittedFormsQueries forms = uowf.currentUnitOfWork().get( SubmittedFormsQueries.class, formsQueryId );
      return forms.getSubmittedForm( index.integer().get() );
   }


   public void formsubmission( EntityReferenceDTO formDTO )
   {
      String formSubmissionsId = getRequest().getAttributes().get( "task" ).toString();

      FormSubmissions formSubmissions = uowf.currentUnitOfWork().get( FormSubmissions.class, formSubmissionsId );

      Form form = uowf.currentUnitOfWork().get( Form.class, formDTO.entity().get().identity() );

      FormSubmission formSubmission = formSubmissions.getFormSubmission( form );

      if ( formSubmission == null)
      {
         formSubmission = formSubmissions.createFormSubmission( form );
         formSubmissions.addFormSubmission( formSubmission );
      }
   }

   public ListValue listformsubmissions() 
   {
      String formSubmissionsId = getRequest().getAttributes().get( "task" ).toString();

      FormSubmissionsQueries formSubmissions = uowf.currentUnitOfWork().get( FormSubmissionsQueries.class, formSubmissionsId );

      return formSubmissions.getFormSubmissions();
   }

   @Override
   protected String getConditionalIdentityAttribute()
   {
      return "task";
   }
}