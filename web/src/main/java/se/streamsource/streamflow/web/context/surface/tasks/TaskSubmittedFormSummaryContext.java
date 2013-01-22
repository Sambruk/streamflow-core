/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.context.surface.tasks;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.api.workspace.cases.form.FieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedPageDTO;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedPageValue;

import static se.streamsource.dci.api.RoleMap.*;

/**
 *
 * */

@Mixins( TaskSubmittedFormSummaryContext.Mixin.class )
public interface TaskSubmittedFormSummaryContext
   extends Context, IndexContext<SubmittedFormDTO>
{
   abstract class Mixin
      implements TaskSubmittedFormSummaryContext
   {
      @Structure
      Module module;

      public SubmittedFormDTO index()
      {
         SubmittedFormValue form = role( SubmittedFormValue.class );

         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         ValueBuilder<SubmittedFormDTO> formBuilder = module.valueBuilderFactory().newValueBuilder(SubmittedFormDTO.class);
         SubmittedFormDTO formDTO = formBuilder.prototype();

         formDTO.submissionDate().set( form.submissionDate().get() );
         formDTO.href().set(form.form().get().identity());
         formDTO.id().set(form.form().get().identity());

         Describable.Data submitter = uow.get( Describable.Data.class, form.submitter().get().identity() );
         formDTO.submitter().set( submitter.description().get() );

         Describable.Data formName = uow.get( Describable.Data.class, form.form().get().identity() );
         formDTO.form().set( formName.description().get() );

         ValueBuilder<FieldDTO> fieldBuilder = module.valueBuilderFactory().newValueBuilder(FieldDTO.class);
         FieldDTO fieldDTO = fieldBuilder.prototype();

         for (SubmittedPageValue submittedPageValue : form.pages().get())
         {
            ValueBuilder<SubmittedPageDTO> pageBuilder = module.valueBuilderFactory().newValueBuilder(SubmittedPageDTO.class);
            SubmittedPageDTO pageDTO = pageBuilder.prototype();

            Describable.Data page = uow.get(Describable.Data.class, submittedPageValue.page().get().identity());
            pageDTO.name().set(page.description().get());

            for (SubmittedFieldValue fieldValue : submittedPageValue.fields().get())
            {
               Describable.Data field = uow.get( Describable.Data.class, fieldValue.field().get().identity() );
               fieldDTO.field().set( field.description().get() );
               fieldDTO.value().set( fieldValue.value().get() );
               FieldValueDefinition.Data fieldDefinition = uow.get( FieldValueDefinition.Data.class, fieldValue.field().get().identity() );
               fieldDTO.fieldType().set( fieldDefinition.fieldValue().get().type().getName() );
               pageDTO.fields().get().add( fieldBuilder.newInstance() );
            }

            formDTO.pages().get().add(pageBuilder.newInstance());
         }

         formDTO.signatures().get().addAll(form.signatures().get());

         return formBuilder.newInstance();

      }
   }
}
