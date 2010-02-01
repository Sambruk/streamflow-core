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

import static org.qi4j.api.entity.EntityReference.parseEntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValueDTO;
import se.streamsource.streamflow.domain.form.SubmittedPageValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.Date;

/**
 * Mapped to:
 * /tasks/{task}/forms/{formsubmission}
 */
public class TaskFormServerResource
      extends CommandQueryServerResource
{
   public TaskFormServerResource() throws ResourceException
   {
      setNegotiated( true );
      getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
   }

   public FormSubmissionValue formsubmission() throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      FormSubmission formSubmission =
            uow.get( FormSubmission.class, getRequest().getAttributes().get("formsubmission").toString() );

      return formSubmission.getFormSubmission();
   }

   public void updatefield( FieldValueDTO field )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      FormSubmission formSubmission =
            uow.get( FormSubmission.class, getRequest().getAttributes().get("formsubmission").toString() );

      ValueBuilder<FormSubmissionValue> builder = vbf.newValueBuilder( FormSubmissionValue.class ).withPrototype( formSubmission.getFormSubmission() );

      for (SubmittedPageValue pageValue : builder.prototype().pages().get())
      {
         for ( FieldSubmissionValue value : pageValue.fields().get() )
         {
            if ( value.field().get().field().get().equals( field.field().get() ) )
            {
               value.value().set( field.value().get() );
            }
         }
      }

      formSubmission.changeFormSubmission( builder.newInstance() );
   }

   public void submitform( )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      SubmittedForms forms =
            uow.get( SubmittedForms.class, getRequest().getAttributes().get( "task" ).toString() );

      FormSubmission formSubmission =
            uow.get( FormSubmission.class, getRequest().getAttributes().get("formsubmission").toString() );

      checkPermission( formSubmission );

      forms.submitForm( formSubmission.getFormSubmission(), parseEntityReference( getClientInfo().getUser().getIdentifier() ) );
   }

   @Override
   protected String getConditionalIdentityAttribute()
   {
      return "task";
   }
}