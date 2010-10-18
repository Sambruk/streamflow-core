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

package se.streamsource.streamflow.web.domain.entity.form;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.EffectiveFieldValue;
import se.streamsource.streamflow.domain.form.EffectiveFormFieldsValue;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.caze.EffectiveFieldDTO;
import se.streamsource.streamflow.resource.caze.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormListDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormsListDTO;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;

/**
 * JAVADOC
 */
@Mixins(SubmittedFormsQueries.Mixin.class)
public interface SubmittedFormsQueries
{
   SubmittedFormsListDTO getSubmittedForms();

   SubmittedFormDTO getSubmittedForm( int idx );

   EffectiveFieldsDTO effectiveFields();

   class Mixin
         implements SubmittedFormsQueries
   {
      @This
      SubmittedForms.Data submittedForms;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      public SubmittedFormsListDTO getSubmittedForms()
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         ValueBuilder<SubmittedFormsListDTO> listBuilder = vbf.newValueBuilder( SubmittedFormsListDTO.class );
         ValueBuilder<SubmittedFormListDTO> formBuilder = vbf.newValueBuilder( SubmittedFormListDTO.class );
         SubmittedFormsListDTO list = listBuilder.prototype();
         SubmittedFormListDTO formDTO = formBuilder.prototype();

         for (SubmittedFormValue form : submittedForms.submittedForms().get())
         {
            formDTO.submissionDate().set( form.submissionDate().get() );

            Describable.Data submitter = uow.get( Describable.Data.class, form.submitter().get().identity() );
            formDTO.submitter().set( submitter.description().get() );

            Describable.Data formName = uow.get( Describable.Data.class, form.form().get().identity() );
            formDTO.form().set( formName.description().get() );
            list.forms().get().add( formBuilder.newInstance() );
         }

         return listBuilder.newInstance();
      }

      public SubmittedFormDTO getSubmittedForm( int idx )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();
         ValueBuilder<SubmittedFormDTO> formBuilder = vbf.newValueBuilder( SubmittedFormDTO.class );
         SubmittedFormDTO formDTO = formBuilder.prototype();

         SubmittedFormValue form = submittedForms.submittedForms().get().get( idx );

         formDTO.submissionDate().set( form.submissionDate().get() );

         Describable.Data submitter = uow.get( Describable.Data.class, form.submitter().get().identity() );
         formDTO.submitter().set( submitter.description().get() );

         Describable.Data formName = uow.get( Describable.Data.class, form.form().get().identity() );
         formDTO.form().set( formName.description().get() );

         ValueBuilder<FieldDTO> fieldBuilder = vbf.newValueBuilder( FieldDTO.class );
         FieldDTO fieldDTO = fieldBuilder.prototype();

         for (SubmittedFieldValue fieldValue : form.values().get())
         {
            Describable.Data field = uow.get( Describable.Data.class, fieldValue.field().get().identity() );
            fieldDTO.field().set( field.description().get() );
            fieldDTO.value().set( fieldValue.value().get() );
            FieldValueDefinition.Data fieldDefinition = uow.get( FieldValueDefinition.Data.class, fieldValue.field().get().identity() );
            fieldDTO.fieldType().set( fieldDefinition.fieldValue().get().type().getName() );
            formDTO.values().get().add( fieldBuilder.newInstance() );
         }

         return formBuilder.newInstance();
      }

      public EffectiveFieldsDTO effectiveFields()
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         ValueBuilder<EffectiveFieldsDTO> listBuilder = vbf.newValueBuilder( EffectiveFieldsDTO.class );
         ValueBuilder<EffectiveFieldDTO> fieldBuilder = vbf.newValueBuilder( EffectiveFieldDTO.class );
         EffectiveFieldsDTO list = listBuilder.prototype();
         EffectiveFieldDTO fieldDTO = fieldBuilder.prototype();

         EffectiveFormFieldsValue effectiveFormFields = submittedForms.effectiveFieldValues().get();
         if (effectiveFormFields != null)
         {
            for (EffectiveFieldValue fieldValue : effectiveFormFields.fields().get())
            {
               if ( !"".equals( fieldValue.value().get()))
               {
                  fieldDTO.submissionDate().set( fieldValue.submissionDate().get() );

                  Describable.Data submitter = uow.get( Describable.Data.class, fieldValue.submitter().get().identity() );
                  fieldDTO.submitter().set( submitter.description().get() );

                  fieldDTO.fieldValue().set( fieldValue.value().get() );
                  Describable.Data fieldName = uow.get( Describable.Data.class, fieldValue.field().get().identity() );
                  Describable.Data formName = uow.get( Describable.Data.class, fieldValue.form().get().identity() );
                  fieldDTO.formName().set( formName.description().get() );
                  fieldDTO.fieldName().set( fieldName.description().get() );
                  FieldValueDefinition.Data fieldDefinition = uow.get( FieldValueDefinition.Data.class, fieldValue.field().get().identity() );
                  fieldDTO.fieldType().set( fieldDefinition.fieldValue().get().type().getName() );
                  list.effectiveFields().get().add( fieldBuilder.newInstance() );
               }
            }
         }

         return listBuilder.newInstance();
      }
   }
}
