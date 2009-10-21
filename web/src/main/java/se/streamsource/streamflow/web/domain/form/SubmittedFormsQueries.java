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

package se.streamsource.streamflow.web.domain.form;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.task.FieldDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormListDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;

/**
 * JAVADOC
 */
@Mixins(SubmittedFormsQueries.SubmittedFormsQueriesMixin.class)
public interface SubmittedFormsQueries
{
    SubmittedFormsListDTO getSubmittedForms();

    SubmittedFormDTO getSubmittedForm(int idx);

    class SubmittedFormsQueriesMixin
        implements SubmittedFormsQueries
    {
        @This SubmittedForms.SubmittedFormsState submittedForms;

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        UnitOfWorkFactory uowf;

        public SubmittedFormsListDTO getSubmittedForms()
        {
            UnitOfWork uow = uowf.currentUnitOfWork();

            ValueBuilder<SubmittedFormsListDTO> listBuilder = vbf.newValueBuilder(SubmittedFormsListDTO.class );
            ValueBuilder<SubmittedFormListDTO> formBuilder = vbf.newValueBuilder(SubmittedFormListDTO.class );
            SubmittedFormsListDTO list = listBuilder.prototype();
            SubmittedFormListDTO formDTO = formBuilder.prototype();

            for (SubmittedFormValue form : submittedForms.submittedForms().get())
            {
                formDTO.submissionDate().set( form.submissionDate().get() );

                Describable.DescribableState submitter = uow.get( Describable.DescribableState.class, form.submitter().get().identity() );
                formDTO.submitter().set( submitter.description().get() );

                Describable.DescribableState formName = uow.get( Describable.DescribableState.class, form.form().get().identity() );
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

            Describable.DescribableState submitter = uow.get( Describable.DescribableState.class, form.submitter().get().identity() );
            formDTO.submitter().set( submitter.description().get() );

            Describable.DescribableState formName = uow.get( Describable.DescribableState.class, form.form().get().identity() );
            formDTO.form().set( formName.description().get() );

            ValueBuilder<FieldDTO> fieldBuilder = vbf.newValueBuilder( FieldDTO.class );
            FieldDTO fieldDTO = fieldBuilder.prototype();

            for (FieldValue fieldValue : form.values().get())
            {
                Describable.DescribableState field = uow.get( Describable.DescribableState.class, fieldValue.field().get().identity() );
                fieldDTO.field().set( field.description().get() );
                fieldDTO.value().set( fieldValue.value().get() );
                formDTO.values().get().add( fieldBuilder.newInstance() );
            }

            return formBuilder.newInstance();
        }
    }
}
