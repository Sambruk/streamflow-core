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
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValueDTO;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;

/**
 * JAVADOC
 */
@Mixins(CaseFormContext.Mixin.class)
public interface CaseFormContext
   extends Interactions
{
   public FormSubmissionValue formsubmission();
   public void updatefield( FieldValueDTO field );
   public void previouspage();
   public void nextpage();

   abstract class Mixin
      extends InteractionsMixin
      implements CaseFormContext
   {
      public FormSubmissionValue formsubmission()
      {
         FormSubmission formSubmission = context.get(FormSubmission.class);

         return formSubmission.getFormSubmission();
      }

      public void previouspage()
      {
         ValueBuilder<FormSubmissionValue> builder = incrementPage( -1 );

         if ( builder != null )
         {
            FormSubmission formSubmission = context.get(FormSubmission.class);
            formSubmission.changeFormSubmission( builder.newInstance() );
         }
      }

      private ValueBuilder<FormSubmissionValue> incrementPage( int increment )
      {
         ValueBuilder<FormSubmissionValue> builder = context.get( FormSubmission.Data.class ).formSubmissionValue().get().buildWith();
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
            FormSubmission formSubmission = context.get(FormSubmission.class);
            formSubmission.changeFormSubmission( builder.newInstance() );
         }
      }

      public void updatefield( FieldValueDTO field )
      {
         FormSubmission formSubmission = context.get(FormSubmission.class);

         formSubmission.changeFieldValue( field.field().get(), field.value().get() );
         /*
         ValueBuilder<FormSubmissionValue> builder = formSubmission.getFormSubmission().buildWith();

         for (PageSubmissionValue pageValue : builder.prototype().pages().get())
         {
            for ( FieldSubmissionValue value : pageValue.fields().get() )
            {
               if ( value.field().get().field().get().equals( field.field().get() ) )
               {
                  value.value().set( field.value().get() );
               }
            }
         }

         formSubmission.changeFormSubmission( builder.newInstance() );
         */
      }

   }
}
