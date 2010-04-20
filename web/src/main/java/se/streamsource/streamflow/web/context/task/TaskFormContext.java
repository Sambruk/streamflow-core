/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.task;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValueDTO;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;

/**
 * JAVADOC
 */
@Mixins(TaskFormContext.Mixin.class)
public interface TaskFormContext
   extends Interactions
{
   public FormSubmissionValue formsubmission();
   public void updatefield( FieldValueDTO field );

   abstract class Mixin
      extends InteractionsMixin
      implements TaskFormContext
   {
      public FormSubmissionValue formsubmission()
      {
         FormSubmission formSubmission = context.get(FormSubmission.class);

         return formSubmission.getFormSubmission();
      }

      public void updatefield( FieldValueDTO field )
      {
         FormSubmission formSubmission = context.get(FormSubmission.class);

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
      }

   }
}
