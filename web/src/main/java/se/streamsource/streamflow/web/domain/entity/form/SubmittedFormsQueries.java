/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.form.FieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormListDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormsListDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedPageDTO;
import se.streamsource.streamflow.api.workspace.cases.general.SecondSigneeInfoValue;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedPageValue;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(SubmittedFormsQueries.Mixin.class)
public interface SubmittedFormsQueries
{
   SubmittedFormsListDTO getSubmittedForms();

   SubmittedFormDTO getSubmittedForm( int idx );

   /**
    * Get the latest form submissions for each type of form
    *
    * @return
    */
   Iterable<SubmittedFormValue> getLatestSubmittedForms();

   AttachmentFieldSubmission getAttachmentFieldValue(String id);

   class Mixin
         implements SubmittedFormsQueries
   {
      @This
      SubmittedForms.Data submittedForms;

      @Structure
      Module module;

      public SubmittedFormsListDTO getSubmittedForms()
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         ValueBuilder<SubmittedFormsListDTO> listBuilder = module.valueBuilderFactory().newValueBuilder(SubmittedFormsListDTO.class);
         ValueBuilder<SubmittedFormListDTO> formBuilder = module.valueBuilderFactory().newValueBuilder(SubmittedFormListDTO.class);
         SubmittedFormsListDTO list = listBuilder.prototype();
         SubmittedFormListDTO formDTO = formBuilder.prototype();

         for (SubmittedFormValue form : submittedForms.submittedForms().get())
         {
            formDTO.submissionDate().set( form.submissionDate().get() );

            Describable.Data submitter = uow.get( Describable.Data.class, form.submitter().get().identity() );
            formDTO.submitter().set( submitter.description().get() );

            Describable.Data formName = uow.get( Describable.Data.class, form.form().get().identity() );
            formDTO.form().set( formName.description().get() );
            formDTO.href().set(form.form().get().identity());
            formDTO.id().set(form.form().get().identity());
            formDTO.unread().set( form.unread().get() );
            list.forms().get().add( formBuilder.newInstance() );
         }

         return listBuilder.newInstance();
      }

      public SubmittedFormDTO getSubmittedForm( int idx )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         ValueBuilder<SubmittedFormDTO> formBuilder = module.valueBuilderFactory().newValueBuilder(SubmittedFormDTO.class);
         SubmittedFormDTO formDTO = formBuilder.prototype();

         SubmittedFormValue form = submittedForms.submittedForms().get().get( idx );

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

         if( form.secondsignee().get() != null )
         {
            ValueBuilder<SecondSigneeInfoValue> secondSigneeInfoBuilder = form.secondsignee().get().buildWith();
            DoubleSignatureTask.Data task = (DoubleSignatureTask.Data)findSecondSigneeTaskRef( form );
            if (task != null) {
               secondSigneeInfoBuilder.prototype().secondsigneetaskref().set( ((Identity)task).identity().get() );
               secondSigneeInfoBuilder.prototype().lastReminderSent().set( task.lastReminderSent().get() );
               secondSigneeInfoBuilder.prototype().secondDraftUrl().set( task.secondDraftUrl().get() );
            }
            formDTO.secondSignee().set( secondSigneeInfoBuilder.newInstance() );
         }
         return formBuilder.newInstance();
      }

      private DoubleSignatureTask findSecondSigneeTaskRef( SubmittedFormValue form )
      {
         DoubleSignatureTask.Data doubleSignData = QueryExpressions.templateFor( DoubleSignatureTask.Data.class );
         Query<DoubleSignatureTask> query = module.queryBuilderFactory().newQueryBuilder( DoubleSignatureTask.class )
               .where( and( eq( doubleSignData.submittedForm().get().submissionDate(), form.submissionDate().get() ),
                            eq( doubleSignData.submittedForm().get().form(), form.form().get() ),
                            eq( doubleSignData.submittedForm().get().submitter(), form.submitter().get() )
               ) ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

         DoubleSignatureTask task = query.find();
         return task;
      }

      public Iterable<SubmittedFormValue> getLatestSubmittedForms()
      {
         List<SubmittedFormValue> forms = (List<SubmittedFormValue>) Iterables.addAll(new ArrayList<SubmittedFormValue>(), submittedForms.submittedForms().get());

         Collections.reverse(forms);

         // Filter out duplicates
         return Iterables.filter(new Specification<SubmittedFormValue>()
         {
            Set<String> formIds = new HashSet<String>();

            public boolean satisfiedBy(SubmittedFormValue submittedFormValue)
            {
               String formId = submittedFormValue.form().get().identity();
               if (formIds.contains(formId))
                  return false;

               formIds.add(formId);

               return true;
            }
         }, forms);
      }

      public AttachmentFieldSubmission getAttachmentFieldValue(String id)
      {

         for (int i = 0; i < this.getSubmittedForms().forms().get().size(); i++)
         {
            for (SubmittedPageDTO submittedPageDTO : this.getSubmittedForm(i).pages().get())
            {
               for (FieldDTO fieldDTO : submittedPageDTO.fields().get())
               {
                  if (fieldDTO.fieldType().get().equals(AttachmentFieldValue.class.getName()))
                  {
                     if (!Strings.empty( fieldDTO.value().get() ))
                     {
                        AttachmentFieldSubmission submission = module.valueBuilderFactory().newValueFromJSON(AttachmentFieldSubmission.class, fieldDTO.value().get());
                        if (submission.attachment().get().identity().equals(id)) return submission;
                     }
                  }
               }
            }
         }
         return null;
      }
   }
}
