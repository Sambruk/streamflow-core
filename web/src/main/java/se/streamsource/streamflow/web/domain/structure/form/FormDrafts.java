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

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.FormDraftValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;

import java.util.ArrayList;

/**
 * JAVADOC
 */
@Mixins(FormDrafts.Mixin.class)
public interface FormDrafts
{
   FormDraft getFormSubmission( Form form );

   FormDraft createFormSubmission( Form form );

   void discardFormDraft( FormDraft form );

   interface Data
   {
      @Aggregated
      @Queryable(false)
      ManyAssociation<FormDraft> formSubmissions();

      FormDraft createdFormDraft( DomainEvent event, Form form );

      void discardedFormDraft( DomainEvent event, FormDraft formDraft );
   }

   abstract class Mixin
         implements FormDrafts, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      @This
      TypedCase.Data typedCase;

      @This
      SubmittedForms.Data submittedForms;

      @Structure
      UnitOfWorkFactory uowf;

      public FormDraft getFormSubmission( Form form )
      {
         for (FormDraft formSubmission : formSubmissions().toList())
         {
            EntityReference formReference = EntityReference.getEntityReference( form );
            if ( formSubmission.getFormDraft().form().get().equals( formReference ))
            {
               return formSubmission;
            }
         }

         return null;
      }

      public FormDraft createFormSubmission( Form form )
      {
         if ( getFormSubmission( form ) != null )
         {
            // already exists, don't create
            return null;
         }

         CaseType caseType = typedCase.caseType().get();
         if ( caseType != null )
         {
            SelectedForms.Data forms = (SelectedForms.Data) caseType;

            if ( forms.selectedForms().contains( form ) )
            {
               return createdFormDraft( DomainEvent.CREATE, form );
            }
         }
         return null;
      }

      public FormDraft createdFormDraft( DomainEvent event, Form form )
      {
         SubmittedFormValue submittedFormValue = findLatestSubmittedForm( form );

         EntityBuilder<FormDraft> submissionEntityBuilder = uowf.currentUnitOfWork().newEntityBuilder( FormDraft.class );

         ValueBuilder<FormDraftValue> builder = vbf.newValueBuilder( FormDraftValue.class );

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
               FieldValue fieldValue = ((FieldValueDefinition.Data) field).fieldValue().get();

               valueBuilder.prototype().description().set( field.getDescription() );
               valueBuilder.prototype().note().set( field.getNote() );
               valueBuilder.prototype().field().set( EntityReference.getEntityReference( field ));
               valueBuilder.prototype().fieldId().set( ((FieldId.Data)field).fieldId().get());
               valueBuilder.prototype().mandatory().set( field.isMandatory() );
               valueBuilder.prototype().fieldValue().set( fieldValue );

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

         FormDraft formSubmission = submissionEntityBuilder.newInstance();
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

      public void discardFormDraft( FormDraft formSubmission )
      {
         if (formSubmissions().contains( formSubmission ))
         {
            discardedFormDraft( DomainEvent.CREATE, formSubmission );
         }
      }

      public void discardedFormDraft( DomainEvent event, FormDraft formDraft )
      {
         formSubmissions().remove( formDraft );
         //uowf.currentUnitOfWork().remove( formDraft );
      }
   }
}