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
package se.streamsource.streamflow.web.domain.structure.task;

import org.joda.time.DateTime;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;

/**
 * 
 *
 */
@Mixins(DoubleSignatureTask.Mixin.class)
public interface DoubleSignatureTask
{

   void updateCase( @Optional Case caze );
   void updateSubmittedForm( @Optional SubmittedFormValue submittedFormValue );
   void updateFormDraft( @Optional FormDraft formDraft );
   void updateAccessPoint( @Optional AccessPoint accessPoint );
   void updateEmailValue( @Optional EmailValue email );
   void updateLastReminderSent( @Optional DateTime lastReminder );
   void updateSecondDraftUrl( @Optional String secondDraftUrl );

   interface Data
   {
      @Optional
      Association<Case> caze();

      @Optional
      Property<SubmittedFormValue> submittedForm();

      @Optional
      Association<FormDraft> formDraft();

      @Optional
      Association<AccessPoint> accessPoint();

      @Optional
      Property<EmailValue> email();

      @Optional
      Property<DateTime> lastReminderSent();

      @Optional
      @UseDefaults
      Property<String> secondDraftUrl();

      void updatedCase( @Optional DomainEvent event, @Optional Case caze );
      void updatedSubmittedForm( @Optional DomainEvent event, @Optional SubmittedFormValue submittedFormValue );
      void updatedFormDraft( @Optional DomainEvent event, @Optional FormDraft formDraft);
      void updatedAccessPoint( @Optional DomainEvent event, @Optional AccessPoint accessPoint );
      void updatedEmailValue( @Optional DomainEvent event, @Optional EmailValue email );
      void updatedLastReminderSent( @Optional DomainEvent event, @Optional DateTime lastReminder );
      void updatedSecondDraftUrl( @Optional DomainEvent event, @Optional String secondDraftUrl );
   }

   abstract class Mixin
         implements DoubleSignatureTask, Data
   {

      @This
      Data data;

      public void updateCase( Case caze )
      {
         updatedCase( null, caze );
      }

      public void updatedCase( DomainEvent event, Case caze )
      {
         data.caze().set( caze );
      }

      public void updateSubmittedForm( SubmittedFormValue submittedFormValue )
      {
         updatedSubmittedForm( null, submittedFormValue );
      }

      public void updatedSubmittedForm( DomainEvent event, SubmittedFormValue submittedFormValue )
      {
         data.submittedForm().set( submittedFormValue );
      }

      public void updateFormDraft( FormDraft formDraft )
      {
         updatedFormDraft( null, formDraft );
      }

      public void updatedFormDraft( DomainEvent event, FormDraft formDraft )
      {
         data.formDraft().set( formDraft );
      }

      public void updateAccessPoint( AccessPoint accessPoint )
      {
         updatedAccessPoint( null, accessPoint );
      }

      public void updatedAccessPoint( DomainEvent event, AccessPoint accessPoint )
      {
         data.accessPoint().set( accessPoint );
      }

      public void updateEmailValue( EmailValue email )
      {
         updatedEmailValue( null, email );
      }

      public void updatedEmailValue( DomainEvent event, EmailValue email )
      {
         data.email().set( email );
      }

      public void updateLastReminderSent( @Optional DateTime lastReminder )
      {
         updatedLastReminderSent( null, lastReminder );
      }

      public void updateSecondDraftUrl( @Optional String secondDraftUrl )
      {
         updatedSecondDraftUrl( null, secondDraftUrl );
      }

      public void updatedLastReminderSent( @Optional DomainEvent event, @Optional DateTime lastReminder )
      {
         data.lastReminderSent().set( lastReminder );
      }

      public void updatedSecondDraftUrl( @Optional DomainEvent event, @Optional String secondDraftUrl )
      {
         data.secondDraftUrl().set( secondDraftUrl );
      }
   }
}
