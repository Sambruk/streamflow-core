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

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;

import java.util.ArrayList;

/**
 * JAVADOC
 */
@Mixins(FormSubmissions.Mixin.class)
public interface FormSubmissions
{
   FormSubmission getFormSubmission( Form form );

   FormSubmission createFormSubmission( Form form );

   void discardFormSubmission( Form form );

   interface Data
   {
      ManyAssociation<FormSubmission> formSubmissions();

      FormSubmission createdFormSubmission( DomainEvent event, Form form );

      void discardedFormSubmission( DomainEvent event, FormSubmission formSubmission );
   }

   abstract class Mixin
         implements FormSubmissions, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      @This
      TypedTask.Data typedTask;

      @This
      SubmittedForms.Data submittedForms;

      @Structure
      UnitOfWorkFactory uowf;

      public FormSubmission getFormSubmission( Form form )
      {
         for (FormSubmission formSubmission : formSubmissions().toList())
         {
            EntityReference formReference = EntityReference.getEntityReference( form );
            if ( formSubmission.getFormSubmission().form().get().equals( formReference ))
            {
               return formSubmission;
            }
         }

         return null;
      }

      public FormSubmission createFormSubmission( Form form )
      {
         if ( getFormSubmission( form ) != null )
         {
            // already exists, don't create
            return null;
         }

         TaskType tasktype = typedTask.taskType().get();
         if ( tasktype != null )
         {
            Forms.Data forms = (Forms.Data) tasktype;

            if ( forms.forms().contains( form ) )
            {
               return createdFormSubmission( DomainEvent.CREATE, form );
            }
         }
         return null;
      }

      public FormSubmission createdFormSubmission( DomainEvent event, Form form )
      {
         SubmittedFormValue submittedFormValue = findLatestSubmittedForm( form );

         EntityBuilder<FormSubmission> submissionEntityBuilder = uowf.currentUnitOfWork().newEntityBuilder( FormSubmission.class );

         ValueBuilder<FormSubmissionValue> builder = vbf.newValueBuilder( FormSubmissionValue.class );

         builder.prototype().description().set( form.getDescription() );
         builder.prototype().form().set( EntityReference.getEntityReference( form ));

         ValueBuilder<PageSubmissionValue> pageBuilder = vbf.newValueBuilder( PageSubmissionValue.class );
         ValueBuilder<FieldSubmissionValue> fieldBuilder = vbf.newValueBuilder( FieldSubmissionValue.class );
         ValueBuilder<FieldDefinitionValue> valueBuilder = vbf.newValueBuilder( FieldDefinitionValue.class );
         builder.prototype().pages().set( new ArrayList<PageSubmissionValue>() );

         Pages.Data pageEntities = (Pages.Data) form;
         for (Page page : pageEntities.pages())
         {
            pageBuilder.prototype().title().set( page.getDescription() );
            pageBuilder.prototype().page().set( EntityReference.getEntityReference( page ));
            pageBuilder.prototype().fields().set( new ArrayList<FieldSubmissionValue>() );

            Fields.Data fieldEntities = (Fields.Data) page;
            for (Field field : fieldEntities.fields())
            {
               valueBuilder.prototype().description().set( field.getDescription() );
               valueBuilder.prototype().note().set( field.getNote() );
               valueBuilder.prototype().field().set( EntityReference.getEntityReference( field ));
               valueBuilder.prototype().mandatory().set( field.getMandatory() );
               valueBuilder.prototype().fieldValue().set( ((FieldValueDefinition.Data) field).fieldValue().get() );

               fieldBuilder.prototype().field().set( valueBuilder.newInstance() );
               fieldBuilder.prototype().value().set( getSubmittedValue( field, submittedFormValue ) );
               fieldBuilder.prototype().enabled().set( true );
               pageBuilder.prototype().fields().get().add( fieldBuilder.newInstance() );
            }
            builder.prototype().pages().get().add( pageBuilder.newInstance() );
         }

         int pages = builder.prototype().pages().get().size();
         builder.prototype().pages().get().remove( pages-1 );
         builder.prototype().pages().get().add( pageBuilder.newInstance() );

         submissionEntityBuilder.instance().changeFormSubmission( builder.newInstance() );

         FormSubmission formSubmission = submissionEntityBuilder.newInstance();
         formSubmissions().add( formSubmission );

         return formSubmission;
      }

      private String getSubmittedValue( Field field, SubmittedFormValue submittedFormValue )
      {
         if ( submittedFormValue == null)
            return null;

         for (SubmittedFieldValue submittedFieldValue : submittedFormValue.values().get())
         {
            if ( submittedFieldValue.field().get().equals( EntityReference.getEntityReference( field )))
            {
               return submittedFieldValue.value().get();
            }
         }
         return null;
      }

      private SubmittedFormValue findLatestSubmittedForm( Form form )
      {
         SubmittedFormValue value = null;
         for (SubmittedFormValue submittedFormValue : submittedForms.submittedForms().get())
         {
            if ( submittedFormValue.form().get().equals( EntityReference.getEntityReference( form )))
            {
               value = submittedFormValue;
            }
         }
         return value;
      }

      public void discardFormSubmission( Form form )
      {
         FormSubmission formSubmission = getFormSubmission( form );
         if ( formSubmission != null )
         {
            discardedFormSubmission( DomainEvent.CREATE, formSubmission );
         }
      }

      public void discardedFormSubmission( DomainEvent event, FormSubmission formSubmission )
      {
         formSubmissions().remove( formSubmission );
      }
   }
}