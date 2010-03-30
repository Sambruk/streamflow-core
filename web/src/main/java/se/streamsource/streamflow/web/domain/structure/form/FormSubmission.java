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

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(FormSubmission.Mixin.class)
public interface FormSubmission
{
   FormSubmissionValue getFormSubmission();

   void changeFormSubmission( FormSubmissionValue formSubmission );

   void changeFieldValue( EntityReference fieldId, String newValue);

   interface Data
   {
      Property<FormSubmissionValue> formSubmissionValue();

      void changedFormSubmission( DomainEvent event, FormSubmissionValue formSubmission);
   }

   abstract class Mixin
      implements FormSubmission, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      public FormSubmissionValue getFormSubmission()
      {
         return formSubmissionValue().get();
      }

      public void changeFormSubmission( FormSubmissionValue formSubmission )
      {
         changedFormSubmission( DomainEvent.CREATE, formSubmission );
      }

      public void changeFieldValue( EntityReference fieldId, String newValue )
      {
         ValueBuilder<FormSubmissionValue> builder = formSubmissionValue().get().buildWith();

         int currentPage = builder.prototype().currentPage().get();

         List<FieldSubmissionValue> fields = builder.prototype().pages().get().get( currentPage ).fields().get();
         for (FieldSubmissionValue field : fields)
         {
            if (field.field().get().field().get().equals(fieldId))
            {
               if (field.value().get() != null && field.value().get().equals( newValue ))
                  return; // Skip update - same value

               field.value().set( newValue );
               break;
            }
         }

         changedFormSubmission( DomainEvent.CREATE, builder.newInstance() );
      }

      public void changedFormSubmission( DomainEvent event, FormSubmissionValue formSubmission )
      {
         formSubmissionValue().set( formSubmission );
      }
   }

}