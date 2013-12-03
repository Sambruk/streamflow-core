/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import com.petebevin.markdown.MarkdownProcessor;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionValue;
import se.streamsource.streamflow.api.administration.form.FieldGroupFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.form.FormDraftEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.FormAttachments;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.FormOnClose;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.organization.FormOnRemove;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

import java.util.ArrayList;

/**
 * JAVADOC
 */
@Mixins(FormDrafts.Mixin.class)
public interface FormDrafts
{
   FormDraft getFormDraft(Form form);

   FormDraft createFormDraft(Form form);

   void discardFormDraft(FormDraft form);

   interface Data
   {
      @Aggregated
      @Queryable(false)
      ManyAssociation<FormDraft> formDrafts();

      FormDraft createdFormDraft(@Optional DomainEvent event, String id);

      void discardedFormDraft(@Optional DomainEvent event, FormDraft formDraft);
   }

   abstract class Mixin implements FormDrafts, Data
   {
      @Structure
      Module module;

      @This
      TypedCase.Data typedCase;

      @This
      SubmittedForms.Data submittedForms;

      @Service
      IdentityGenerator idgen;

      public FormDraft getFormDraft(Form form)
      {
         for (FormDraft formDraft : formDrafts().toList())
         {
            if (formDraft.getFormDraftValue() == null)
               return null;
            if (formDraft.getFormDraftValue().form().get().identity().equals( form.toString() ))
            {
               return formDraft;
            }
         }

         return null;
      }

      public FormDraft createFormDraft(Form form)
      {
         if (getFormDraft( form ) != null)
         {
            // already exists, don't create
            return null;
         }

         Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
         FormOnRemove.Data data = (FormOnRemove.Data) orgs.organization().get();
         Form formOnRemove = data.formOnRemove().get();

         if ( formOnRemove != null && form.equals( formOnRemove ) )
         {
            return createDraft( form );
         }

         CaseType caseType = typedCase.caseType().get();
         if (caseType != null)
         {
            SelectedForms.Data forms = (SelectedForms.Data) caseType;
            FormOnClose.Data formOnClose = (FormOnClose.Data) caseType;

            if (forms.selectedForms().contains( form ) || form.equals( formOnClose.formOnClose().get() ))
            {
               return createDraft( form );
            }
         }
         return null;
      }

      private FormDraft  createDraft( Form form )
      {
         SubmittedFormValue submittedFormValue = findLatestSubmittedForm( form );

         ValueBuilder<FormDraftDTO> builder = module.valueBuilderFactory().newValueBuilder( FormDraftDTO.class );

         builder.prototype().description().set( form.getDescription() );
         builder.prototype().form().set( EntityReference.getEntityReference( form ) );

         ValueBuilder<PageSubmissionDTO> pageBuilder = module.valueBuilderFactory().newValueBuilder(
               PageSubmissionDTO.class );
         ValueBuilder<FieldSubmissionDTO> fieldBuilder = module.valueBuilderFactory().newValueBuilder(
               FieldSubmissionDTO.class );
         ValueBuilder<FieldDefinitionValue> valueBuilder = module.valueBuilderFactory().newValueBuilder(
               FieldDefinitionValue.class );

         builder.prototype().pages().set( new ArrayList<PageSubmissionDTO>() );

         Pages.Data pageEntities = (Pages.Data) form;
         for (Page page : pageEntities.pages())
         {
            pageBuilder.prototype().title().set( page.getDescription() );
            pageBuilder.prototype().page().set( EntityReference.getEntityReference( page ) );
            pageBuilder.prototype().fields().set( new ArrayList<FieldSubmissionDTO>() );
            pageBuilder.prototype().rule().set( page.getRule() );


            Fields.Data fieldEntities = (Fields.Data) page;
            for (Field field : fieldEntities.fields())
            {
               FieldValue fieldValue = ((FieldValueDefinition.Data) field).fieldValue().get();

               if (fieldValue instanceof FieldGroupFieldValue)
               {

                  UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
                  FieldGroup fieldGroup = uow.get( FieldGroup.class, ((FieldGroupFieldValue)fieldValue).fieldGroup().get().identity() );

                  ValueBuilder<FieldGroupFieldValue> fieldValueBuilder = module.valueBuilderFactory().newValueBuilder( FieldGroupFieldValue.class ).withPrototype( (FieldGroupFieldValue) fieldValue );
                  fieldValueBuilder.prototype().fieldCount().set( ((Fields.Data)fieldGroup).fields().count() );
                  FieldSubmissionDTO fieldGroupDefinition = createFieldSubmission( field, fieldValueBuilder.newInstance(), submittedFormValue, fieldBuilder, valueBuilder );

                  pageBuilder.prototype().fields().get().add( fieldGroupDefinition );

                  for (Field subField : page.listFieldGroupFields( field ))
                  {
                     FieldValue subFieldValue = ((FieldValueDefinition.Data) subField).fieldValue().get();

                     ValueBuilder<FieldSubmissionDTO> fieldSubmissionBuilder = fieldSubmissionBuilder( subField, subFieldValue, submittedFormValue, fieldBuilder, valueBuilder );
                     valueBuilder.prototype().field().set( EntityReference.getEntityReference( subField ) );
                     fieldSubmissionBuilder.prototype().field().set( valueBuilder.newInstance() );

                     pageBuilder.prototype().fields().get().add( fieldSubmissionBuilder.newInstance() );
                  }
               } else
               {
                  pageBuilder.prototype().fields().get().add( createFieldSubmission( field, fieldValue, submittedFormValue, fieldBuilder, valueBuilder ) );
               }
            }
            builder.prototype().pages().get().add( pageBuilder.newInstance() );
         }

         int pages = builder.prototype().pages().get().size();
         if( pages == 0 )
         {
            throw new IllegalArgumentException( ErrorResources.form_without_pages.name() );
         }
         builder.prototype().pages().get().remove( pages - 1 );
         builder.prototype().pages().get().add( pageBuilder.newInstance() );

         FormDraft draft = createdFormDraft( null, idgen.generate( FormDraftEntity.class ) );
         draft.changeFormDraftValue(  builder.newInstance() );

         return draft;
      }

      private FieldSubmissionDTO createFieldSubmission(Field field, FieldValue fieldValue,
                                                       SubmittedFormValue submittedFormValue, ValueBuilder<FieldSubmissionDTO> fieldBuilder,
                                                       ValueBuilder<FieldDefinitionValue> valueBuilder)
      {
         return fieldSubmissionBuilder( field, fieldValue, submittedFormValue, fieldBuilder, valueBuilder ).newInstance();
      }

      private ValueBuilder<FieldSubmissionDTO> fieldSubmissionBuilder(Field field, FieldValue fieldValue,
            SubmittedFormValue submittedFormValue, ValueBuilder<FieldSubmissionDTO> fieldBuilder,
            ValueBuilder<FieldDefinitionValue> valueBuilder)
      {
         valueBuilder.prototype().description().set( field.getDescription() );
         if (fieldValue instanceof CommentFieldValue) 
         {
            valueBuilder.prototype().note().set( (new MarkdownProcessor()).markdown( field.getNote()) );
         } else
         {
            valueBuilder.prototype().note().set( field.getNote() );
         }
         valueBuilder.prototype().field().set( EntityReference.getEntityReference( field ) );
         valueBuilder.prototype().fieldId().set( ((FieldId.Data) field).fieldId().get() );
         DatatypeDefinition datatypeDefinition = ((Datatype.Data) field).datatype().get();
         if (datatypeDefinition != null)
         {
            valueBuilder.prototype().datatypeUrl().set( datatypeDefinition.getUrl() );
            if (fieldValue instanceof TextFieldValue)
            {
               TextFieldValue textFieldValue = (TextFieldValue) fieldValue;
               if (Strings.empty( textFieldValue.regularExpression().get() )
                     && !Strings.empty( datatypeDefinition.getRegularExpression() ))
               {
                  ValueBuilder<TextFieldValue> fieldValueBuilder = module.valueBuilderFactory()
                        .newValueBuilder( TextFieldValue.class ).withPrototype( (TextFieldValue) fieldValue );
                  fieldValueBuilder.prototype().regularExpression().set( datatypeDefinition.getRegularExpression() );
                  fieldValueBuilder.prototype().mandatory().set( field.isMandatory() );
                  fieldValue = fieldValueBuilder.newInstance();
               }
            }

         }
         valueBuilder.prototype().mandatory().set( field.isMandatory() );
         valueBuilder.prototype().fieldValue().set( fieldValue );
         valueBuilder.prototype().rule().set( field.getRule() );

         fieldBuilder.prototype().field().set( valueBuilder.newInstance() );
         String submittedValue = getSubmittedValue( field, submittedFormValue );
         fieldBuilder.prototype().value().set("".equals( submittedValue ) ? null : submittedValue );
         fieldBuilder.prototype().enabled().set( true );


         return fieldBuilder;
      }
      
      public FormDraft createdFormDraft(@Optional DomainEvent event, String id)
      {
         EntityBuilder<FormDraft> submissionEntityBuilder = module.unitOfWorkFactory().currentUnitOfWork()
               .newEntityBuilder( FormDraft.class, id );

         FormDraft formSubmission = submissionEntityBuilder.newInstance();
         formDrafts().add( formSubmission );

         return formSubmission;
      }

      private String getSubmittedValue(Field field, SubmittedFormValue submittedFormValue)
      {
         if (submittedFormValue == null)
            return null;

         for (SubmittedPageValue submittedPageValue : submittedFormValue.pages().get())
         {
            for (SubmittedFieldValue submittedFieldValue : submittedPageValue.fields().get())
            {
               if (submittedFieldValue.field().get().equals( EntityReference.getEntityReference( field ) ))
               {
                  return submittedFieldValue.value().get();
               }
            }
         }
         return null;
      }

      private SubmittedFormValue findLatestSubmittedForm(Form form)
      {
         SubmittedFormValue value = null;
         for (SubmittedFormValue submittedFormValue : submittedForms.submittedForms().get())
         {
            if (submittedFormValue.form().get().equals( EntityReference.getEntityReference( form ) ))
            {
               value = submittedFormValue;
            }
         }
         return value;
      }

      public void discardFormDraft(FormDraft formDraft)
      {
         if (formDrafts().contains( formDraft ))
         {
            for (Attachment attachment : ((FormAttachments.Data) formDraft).formAttachments().toList())
            {
               ((FormAttachments) formDraft).removeFormAttachment( attachment );
            }
            discardedFormDraft( null, formDraft );
         }
      }

      public void discardedFormDraft(@Optional DomainEvent event, FormDraft formDraft)
      {
         formDrafts().remove( formDraft );
         // uowf.currentUnitOfWork().remove( formDraft );
      }
   }
}