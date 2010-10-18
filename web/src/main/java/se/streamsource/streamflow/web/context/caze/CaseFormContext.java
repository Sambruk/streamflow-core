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

package se.streamsource.streamflow.web.context.caze;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.streamflow.domain.form.FieldValueDTO;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmissions;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;

import java.io.IOException;

/**
 * JAVADOC
 */
@Mixins(CaseFormContext.Mixin.class)
public interface CaseFormContext
   extends Context, DeleteContext
{
   public FormSubmissionValue formsubmission();
   public void updatefield( FieldValueDTO field );
   public void previouspage();
   public void nextpage();

   public void submit();

   abstract class Mixin
      extends ContextMixin
      implements CaseFormContext
   {
      public FormSubmissionValue formsubmission()
      {
         FormSubmission formSubmission = roleMap.get(FormSubmission.class);

         return formSubmission.getFormSubmission();
      }

      public void previouspage()
      {
         ValueBuilder<FormSubmissionValue> builder = incrementPage( -1 );

         if ( builder != null )
         {
            FormSubmission formSubmission = roleMap.get(FormSubmission.class);
            formSubmission.changeFormSubmission( builder.newInstance() );
         }
      }

      private ValueBuilder<FormSubmissionValue> incrementPage( int increment )
      {
         ValueBuilder<FormSubmissionValue> builder = roleMap.get( FormSubmission.Data.class ).formSubmissionValue().get().buildWith();
         int page = builder.prototype().currentPage().get() + increment;
         int pages = builder.prototype().pages().get().size();

         if ( page < pages && page >= 0 )
         {
            builder.prototype().currentPage().set( page );
            return builder;
         }
         return null;
      }

      public void nextpage()
      {
         ValueBuilder<FormSubmissionValue> builder = incrementPage( 1 );

         if ( builder != null )
         {
            FormSubmission formSubmission = roleMap.get(FormSubmission.class);
            formSubmission.changeFormSubmission( builder.newInstance() );
         }
      }

      public void updatefield( FieldValueDTO field )
      {
         FormSubmission formSubmission = roleMap.get(FormSubmission.class);
         formSubmission.changeFieldValue( field.field().get(), field.value().get() );
      }

      public void submit()
      {
         FormSubmission formSubmission = roleMap.get(FormSubmission.class);

         Submitter submitter = roleMap.get( Submitter.class );

         roleMap.get( SubmittedForms.class ).submitForm( formSubmission, submitter );
      }

      public void delete() throws ResourceException, IOException
      {
         FormSubmissions formSubmissions = roleMap.get( FormSubmissions.class );
         FormSubmission formSubmission = roleMap.get(FormSubmission.class);
         formSubmissions.discardFormSubmission( formSubmission );
      }
   }
}
