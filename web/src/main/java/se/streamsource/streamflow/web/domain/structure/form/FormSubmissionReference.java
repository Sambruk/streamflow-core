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

import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import org.qi4j.api.property.Property;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.mixin.Mixins;

/**
 * JAVADOC
 */
@Mixins(FormSubmissionReference.Mixin.class)
public interface FormSubmissionReference
{
   FormSubmissionValue getFormSubmission();

   void changeFormSubmission( FormSubmissionValue formSubmission );

   interface Data
   {
      Property<FormSubmissionValue> formSubmissionValue();

      void changedFormSubmission( DomainEvent event, FormSubmissionValue formSubmission);
   }

   abstract class Mixin
      implements FormSubmissionReference, Data
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

      public void changedFormSubmission( DomainEvent event, FormSubmissionValue formSubmission )
      {
         formSubmissionValue().set( formSubmission );
      }
   }
}