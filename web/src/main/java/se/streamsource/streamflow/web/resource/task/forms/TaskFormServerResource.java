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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FormDefinitionValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.Fields;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormListDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * Mapped to:
 * /tasks/{task}/forms/{form}
 */
public class TaskFormServerResource
      extends CommandQueryServerResource
{
   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   Qi4jSPI spi;

   public TaskFormServerResource() throws ResourceException
   {
      setNegotiated( true );
      getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
   }

   public FormDefinitionValue form() throws ResourceException
   {
      String formId = getRequest().getAttributes().get("form").toString();

      String formsQueryId = getRequest().getAttributes().get( "task" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      SubmittedForms.Data forms = uow.get( SubmittedForms.Data.class, formsQueryId );

      SubmittedFormValue submitted = null;
      for (SubmittedFormValue dto : forms.submittedForms().get())
      {
         if (dto.form().get().identity().equals( formId ))
         {
            if ( submitted == null || submitted.submissionDate().get().before( dto.submissionDate().get() ))
            {
               submitted = dto;
            }
         }
      }
      Map<String, String> submittedValues = null;
      if ( submitted != null )
      {
         submittedValues = new HashMap<String, String>();

         for (SubmittedFieldValue fieldValue : submitted.values().get())
         {
            submittedValues.put( fieldValue.field().get().identity(), fieldValue.value().get() );
         }
      }

      FormEntity form = uow.get( FormEntity.class, formId);

      ValueBuilder<FormDefinitionValue> builder =
            vbf.newValueBuilder(FormDefinitionValue.class);

      builder.prototype().note().set(form.note().get());
      builder.prototype().description().set(form.description().get());
      builder.prototype().form().set(EntityReference.parseEntityReference(formId));
      builder.prototype().fields().set( new ArrayList<FieldSubmissionValue>() );

      ValueBuilder<FieldSubmissionValue> fieldBuilder = vbf.newValueBuilder( FieldSubmissionValue.class );
      ValueBuilder<FieldDefinitionValue> fieldDefinitionBuilder = vbf.newValueBuilder( FieldDefinitionValue.class );

      Fields.Data fields;
      try
      {
         fields = uow.get( Fields.Data.class, formId );
         for (Field field : fields.fields())
         {
            FieldEntity entity = (FieldEntity) field;

            fieldDefinitionBuilder.prototype().fieldValue().set( entity.fieldValue().get() );
            fieldDefinitionBuilder.prototype().field().set( EntityReference.parseEntityReference( entity.identity().get() ));
            fieldDefinitionBuilder.prototype().description().set( field.getDescription() );
            fieldDefinitionBuilder.prototype().note().set( entity.note().get() );
            fieldBuilder.prototype().field().set( fieldDefinitionBuilder.newInstance() );
            if ( submittedValues != null )
            {
               fieldBuilder.prototype().value().set( submittedValues.get( entity.identity().get() ));
            }
            builder.prototype().fields().get().add( fieldBuilder.newInstance() );
         }

      } catch (NoSuchEntityException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e );
      }

      return builder.newInstance();
   }

   @Override
   protected String getConditionalIdentityAttribute()
   {
      return "task";
   }
}