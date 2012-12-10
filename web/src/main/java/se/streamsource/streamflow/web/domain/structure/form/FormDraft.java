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
package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.SecondSigneeInfoValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.infrastructure.plugin.map.KartagoMapService;

import java.util.Collections;

/**
 * JAVADOC
 */
@Mixins(FormDraft.Mixin.class)
public interface FormDraft
{
   FormDraftDTO getFormDraftValue();

   void changeFormDraftValue( FormDraftDTO formDraftDTO);

   void changeFieldValue( EntityReference fieldId, String fieldValue );

   void addFormSignatureValue( FormSignatureDTO formSignatureDTO);

   void removeFormSignatures();

   void changeFieldAttachmentValue( AttachmentFieldDTO fieldAttachment );

   FieldSubmissionDTO getFieldValue( EntityReference fieldId );

   void enableEmailMessage();

   void disableEmailMessage();

   void changeEmailsToBeNotified( StringValue message );

   void addSecondSigneeInfo( SecondSigneeInfoValue secondSignee );

   interface Data
   {
      Property<FormDraftDTO> formDraftValue();

      void changedFormDraft( @Optional DomainEvent event, FormDraftDTO formDraftDTO);

      void changedFieldValue( @Optional DomainEvent event, EntityReference fieldId, String fieldValue );

      void addedFormSignatureValue( @Optional DomainEvent event, FormSignatureDTO formSignatureDTO);

      void removedFormSignatures( @Optional DomainEvent event );

      void changedFieldAttachmentValue( @Optional DomainEvent event, AttachmentFieldDTO fieldAttachment );

      void changedNotifyByEmail( @Optional DomainEvent event, Boolean value );

      void changedEmailsToBeNotified( @Optional DomainEvent event, String emails );

      void addedSecondSigneeInfo( @Optional DomainEvent event, SecondSigneeInfoValue secondSigneeValue);
   }

   abstract class Mixin
         implements FormDraft, Data
   {

      @Structure
      Module module;

      @Optional
      @Service
      KartagoMapService kartagoMapService;
      
      public FormDraftDTO getFormDraftValue()
      {
         return formDraftValue().get();
      }

      public void changeFormDraftValue( FormDraftDTO formDraftDTO)
      {
         changedFormDraft( null, formDraftDTO);
      }

      public void changeFieldValue( EntityReference fieldId, String newValue )
      {
         FormDraftDTO formDraft = formDraftValue().get();
         FieldSubmissionDTO field = findField( formDraft, fieldId );

         if (field.value().get() != null && field.value().get().equals( newValue ))
         {
            return;
         } // Skip update - same value

         FieldValue value = field.field().get().fieldValue().get();
         if (value.validate( newValue ))
         {
            changedFieldValue( null, fieldId, newValue );
         }
      }


      public void addFormSignatureValue( FormSignatureDTO formSignatureDTO)
      {
         //validate
         addedFormSignatureValue( null, formSignatureDTO);
      }

      public void removeFormSignatures()
      {
         removedFormSignatures( null );
      }

      private FieldSubmissionDTO findField( FormDraftDTO draft, EntityReference fieldRef )
      {
         for (PageSubmissionDTO pageSubmissionDTO : draft.pages().get())
         {
            for (FieldSubmissionDTO field : pageSubmissionDTO.fields().get())
            {
               if (field.field().get().field().get().equals( fieldRef ))
               {
                  return field;
               }
            }
         }
         return null;
      }

      public FieldSubmissionDTO getFieldValue( EntityReference fieldId )
      {
         return findField( formDraftValue().get(), fieldId );
      }

      public void changedFormDraft( DomainEvent event, FormDraftDTO formDraftDTO)
      {
         formDraftValue().set(formDraftDTO);
      }

      public void enableEmailMessage()
      {
         Boolean current = formDraftValue().get().mailSelectionEnablement().get();
         if ( current == null )
         {
            changedNotifyByEmail( null, Boolean.TRUE );
         } else if ( current.equals( Boolean.FALSE ))
         {
            changedNotifyByEmail( null, Boolean.TRUE );
         }
      }

      public void disableEmailMessage()
      {
         Boolean current = formDraftValue().get().mailSelectionEnablement().get();
         if ( current == null )
         {
            changedNotifyByEmail( null, Boolean.FALSE );
         } else if ( current.equals( Boolean.TRUE ))
         {
            changedNotifyByEmail( null, Boolean.FALSE );
         }
      }

      public void changeEmailsToBeNotified( StringValue message )
      {
         changedEmailsToBeNotified( null, message.string().get() );
      }


      public void changedFieldValue( @Optional DomainEvent event, EntityReference fieldId, String fieldValue )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         FieldSubmissionDTO field = findField( builder.prototype(), fieldId );
         field.value().set( fieldValue );

         formDraftValue().set( builder.newInstance() );
      }

      public void addedFormSignatureValue( @Optional DomainEvent event, FormSignatureDTO formSignatureDTO)
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         builder.prototype().signatures().get().add(formSignatureDTO);

         formDraftValue().set( builder.newInstance() );
      }

      public void removedFormSignatures( @Optional DomainEvent event )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         builder.prototype().signatures().set( Collections.<FormSignatureDTO>emptyList() );

         formDraftValue().set( builder.newInstance() );
      }

      public void changeFieldAttachmentValue( AttachmentFieldDTO fieldAttachment )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();

         FieldSubmissionDTO field = findField( builder.prototype(), fieldAttachment.field().get() );
         if (field != null)
         {
            changedFieldAttachmentValue( null, fieldAttachment );
         }
      }

      public void changedFieldAttachmentValue( DomainEvent event, AttachmentFieldDTO fieldAttachment )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         FieldSubmissionDTO field = findField( builder.prototype(), fieldAttachment.field().get() );

         ValueBuilder<AttachmentFieldSubmission> valueBuilder = module.valueBuilderFactory().newValueBuilder(AttachmentFieldSubmission.class);
         valueBuilder.prototype().attachment().set( fieldAttachment.attachment().get() );
         valueBuilder.prototype().name().set( fieldAttachment.name().get() );

         field.value().set( valueBuilder.newInstance().toJSON() );

         formDraftValue().set( builder.newInstance() );
      }

      public void changedNotifyByEmail( @Optional DomainEvent event, Boolean value )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         builder.prototype().mailSelectionEnablement().set( value );

         formDraftValue().set( builder.newInstance() );
      }

      public void changedEmailsToBeNotified( @Optional DomainEvent event, String emails )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         builder.prototype().enteredEmails().set( emails );

         formDraftValue().set( builder.newInstance() );
      }

      public void addSecondSigneeInfo( SecondSigneeInfoValue secondSignee )
      {
         addedSecondSigneeInfo( null, secondSignee );
      }

      public void addedSecondSigneeInfo( @Optional DomainEvent event, SecondSigneeInfoValue secondSignee )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         builder.prototype().secondsignee().set( secondSignee );

         formDraftValue().set( builder.newInstance() );
      }
   }

}