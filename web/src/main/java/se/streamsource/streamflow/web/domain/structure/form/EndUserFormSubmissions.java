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

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.EndUserFormDraftValue;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormSubmissionEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(EndUserFormSubmissions.Mixin.class)
public interface EndUserFormSubmissions
{
   EndUserFormDraftValue getFormDraft( Form form, AnonymousEndUser endUser );

   EndUserFormDraftValue createFormDraft( Form form, AnonymousEndUser endUser );

   void discardFormDraft( Form form, AnonymousEndUser endUser );

   void submitForm( Form form, AnonymousEndUser endUser );

   interface Data
   {
      @UseDefaults
      Property<List<EndUserFormDraftValue>> formDrafts();

      EndUserFormDraftValue createdFormDraft( DomainEvent event, Form form, AnonymousEndUser endUser );

      void discardedFormDraft( DomainEvent event, EndUserFormDraftValue formDraft );

      void submittedForm( DomainEvent event, AnonymousEndUser endUser, EndUserFormDraftValue formDraft );
   }

   abstract class Mixin
         implements EndUserFormSubmissions, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      SelectedForms.Data selectedForms;

      @This
      AccessPoint.Data accesspoint;

      @This
      Labelable.Data labelable;

      public EndUserFormDraftValue getFormDraft( Form form, AnonymousEndUser endUser )
      {
         for (EndUserFormDraftValue formDraft : formDrafts().get())
         {
            EntityReference formReference = EntityReference.getEntityReference( form );
            if ( formDraft.form().get().equals( formReference ))
            {
               EntityReference user = EntityReference.getEntityReference( endUser );
               if ( formDraft.enduser().get().equals( user ))
               {
                  return formDraft;
               }
            }
         }
         return null;
      }

      public EndUserFormDraftValue createFormDraft( Form form, AnonymousEndUser endUser )
      {
         if ( getFormDraft( form, endUser ) != null )
         {
            // already exists, don't create
            return null;
         }

         if ( selectedForms.selectedForms().contains( form ) )
         {
            return createdFormDraft( DomainEvent.CREATE, form, endUser );
         }
         return null;
      }

      public EndUserFormDraftValue createdFormDraft( DomainEvent event, Form form, AnonymousEndUser endUser )
      {
         EntityBuilder<FormSubmission> submissionEntityBuilder = uowf.currentUnitOfWork().newEntityBuilder( FormSubmission.class );

         ValueBuilder<FormSubmissionValue> builder = vbf.newValueBuilder( FormSubmissionValue.class );

         builder.prototype().description().set( form.getDescription() );
         EntityReference formReference = EntityReference.getEntityReference( form );
         builder.prototype().form().set( formReference );

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

               if (fieldValue instanceof CommentFieldValue)
                  continue;

               valueBuilder.prototype().description().set( field.getDescription() );
               valueBuilder.prototype().note().set( field.getNote() );
               valueBuilder.prototype().field().set( EntityReference.getEntityReference( field ));
               valueBuilder.prototype().mandatory().set( field.getMandatory() );
               valueBuilder.prototype().fieldValue().set( fieldValue );

               fieldBuilder.prototype().field().set( valueBuilder.newInstance() );
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
         ValueBuilder<EndUserFormDraftValue> formDraftBuilder = vbf.newValueBuilder( EndUserFormDraftValue.class );
         formDraftBuilder.prototype().enduser().set( EntityReference.getEntityReference( endUser ));
         formDraftBuilder.prototype().form().set( formReference );
         formDraftBuilder.prototype().formsubmission().set( EntityReference.getEntityReference( formSubmission ));

         EndUserFormDraftValue value = formDraftBuilder.newInstance();
         List<EndUserFormDraftValue> draftValues = formDrafts().get();
         draftValues.add( value );
         formDrafts().set( draftValues );

         return value;
      }

      public void discardFormDraft( Form form, AnonymousEndUser endUser )
      {
         EndUserFormDraftValue formDraft = getFormDraft( form, endUser );
         if ( formDrafts().get().contains( formDraft ) )
         {
            discardedFormDraft( DomainEvent.CREATE, formDraft );
         }
      }

      public void discardedFormDraft( DomainEvent event, EndUserFormDraftValue formDraft )
      {
         FormSubmissionEntity formSubmission = uowf.currentUnitOfWork().get( FormSubmissionEntity.class, formDraft.formsubmission().get().identity() );
         formSubmission.deleteEntity();
         formDrafts().get().remove( formDraft );
      }

      public void submitForm( Form form, AnonymousEndUser endUser )
      {
         EndUserFormDraftValue submission = getFormDraft( form, endUser );

         if ( submission != null )
         {
            submittedForm( DomainEvent.CREATE, endUser, submission );
         }

      }

      public void submittedForm( DomainEvent event, AnonymousEndUser endUser, EndUserFormDraftValue formDraft )
      {
         // create case
         CaseEntity caseEntity = endUser.createDraft();
         caseEntity.changeDescription( "Surface Case (Anonymous)" );
         caseEntity.changeCaseType( accesspoint.caseType().get() );
         caseEntity.createdBy().set( endUser );

         for (Label label : labelable.labels())
         {
            caseEntity.addLabel( label );
         }

         // submit form
         FormSubmission formSubmission = uowf.currentUnitOfWork().get( FormSubmission.class, formDraft.formsubmission().get().identity() );
         caseEntity.submitForm( formSubmission, endUser );

         // send case
         caseEntity.unassign();
         caseEntity.sendTo( (ProjectEntity) accesspoint.project().get() );
         caseEntity.open();
      }
   }
}