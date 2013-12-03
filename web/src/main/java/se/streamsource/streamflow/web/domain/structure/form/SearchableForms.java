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

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.Qi4jSPI;

import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;

/**
 * TODO
 */
@Mixins(SearchableForms.Mixin.class)
public interface SearchableForms
{
   void updateSearchableFormValues();

   interface Data
   {
      @UseDefaults
      Property<List<String>> searchableFormValues();
   }

   interface Events
   {
      void changedSearchableFormValues(@Optional DomainEvent event, List<String> searchableFormValues);
   }

   class Mixin
      implements SearchableForms, Events
   {
      @This Data data;

      @This
      SubmittedFormsQueries forms;

      @Structure
      Module module;

      @Structure
      Qi4jSPI qi4j;

      public void changedSearchableFormValues(@Optional DomainEvent event, List<String> searchableFormValues)
      {
         data.searchableFormValues().set(searchableFormValues);
      }

      public void updateSearchableFormValues()
      {
         List<String> newSearchableFormValues = new ArrayList<String>();
         for (SubmittedFormValue submittedFormValue : forms.getLatestSubmittedForms())
         {
            for (SubmittedPageValue submittedPageValue : submittedFormValue.pages().get())
            {
               for (SubmittedFieldValue submittedFieldValue : submittedPageValue.fields().get())
               {
                  FieldEntity field = module.unitOfWorkFactory().currentUnitOfWork().get(FieldEntity.class, submittedFieldValue.field().get().identity());
                  if (Specifications.in(TextFieldValue.class, TextAreaFieldValue.class).satisfiedBy((Class<FieldValue>)  field.fieldValue().get().type()))
                  {
                     newSearchableFormValues.add(submittedFieldValue.value().get());
                  }
               }
            }
         }


         if (!data.searchableFormValues().get().equals(newSearchableFormValues))
            changedSearchableFormValues(null, newSearchableFormValues);
      }
   }
}
