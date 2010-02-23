/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.task.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;
import se.streamsource.streamflow.web.domain.entity.form.FormSubmissionEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormSubmissionsQueries;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmissions;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;
import se.streamsource.streamflow.dci.infrastructure.web.context.Context;
import se.streamsource.streamflow.dci.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.dci.infrastructure.web.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(TaskFormsContext.Mixin.class)
public interface TaskFormsContext
   extends SubContexts<TaskFormContext>, Context
{
      public SubmittedFormsListDTO listsubmittedforms();
      public EffectiveFieldsDTO effectivefields();
      public SubmittedFormDTO submittedform( IntegerDTO index);
      public void createformsubmission( EntityReferenceDTO formDTO );
      public void discard( EntityReferenceDTO formDTO );
      public EntityReferenceDTO formsubmission( EntityReferenceDTO formDTO );
      public void submit( EntityReferenceDTO formDTO ) throws ResourceException;

   abstract class Mixin
      extends ContextMixin
      implements TaskFormsContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      public SubmittedFormsListDTO listsubmittedforms()
      {
         SubmittedFormsQueries forms = context.role(SubmittedFormsQueries.class);
         return forms.getSubmittedForms();
      }

      public EffectiveFieldsDTO effectivefields()
      {
         SubmittedFormsQueries fields = context.role(SubmittedFormsQueries.class);

         return fields.effectiveFields();
      }

      public SubmittedFormDTO submittedform( IntegerDTO index)
      {
         SubmittedFormsQueries forms = context.role(SubmittedFormsQueries.class);

         return forms.getSubmittedForm( index.integer().get() );
      }

      public void createformsubmission( EntityReferenceDTO formDTO )
      {
         FormSubmissions formSubmissions = context.role(FormSubmissions.class);

         Form form = uowf.currentUnitOfWork().get( Form.class, formDTO.entity().get().identity() );

         formSubmissions.createFormSubmission( form );
      }

      public void discard( EntityReferenceDTO formDTO )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         FormSubmissions formSubmissions = context.role(FormSubmissions.class);

         Form form = uowf.currentUnitOfWork().get( Form.class, formDTO.entity().get().identity() );

         formSubmissions.discardFormSubmission( form );
      }

      public EntityReferenceDTO formsubmission( EntityReferenceDTO formDTO )
      {
         FormSubmissionsQueries formSubmissions = context.role(FormSubmissionsQueries.class);

         return formSubmissions.getFormSubmission( formDTO.entity().get() );
      }

      public void submit( EntityReferenceDTO formDTO ) throws ResourceException
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         EntityReferenceDTO dto = context.role(FormSubmissionsQueries.class).getFormSubmission( formDTO.entity().get() );

         if ( dto == null )
         {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
         }

         FormSubmission formSubmission =
               uow.get( FormSubmission.class, dto.entity().get().identity() );

         Submitter submitter = context.role(Submitter.class);

         context.role( SubmittedForms.class).submitForm( formSubmission, submitter );
      }

      public TaskFormContext context( String id )
      {
         context.playRoles( uowf.currentUnitOfWork().get( FormSubmissionEntity.class, id ), FormSubmissionEntity.class);

         return subContext( TaskFormContext.class );
      }
   }
}
