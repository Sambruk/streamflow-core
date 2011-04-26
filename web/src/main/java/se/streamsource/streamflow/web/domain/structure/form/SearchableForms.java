package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.Qi4j;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.util.Iterables;
import org.qi4j.spi.Qi4jSPI;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;

import java.util.ArrayList;
import java.util.List;

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
      UnitOfWorkFactory uowf;

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
                  FieldEntity field = uowf.currentUnitOfWork().get(FieldEntity.class, submittedFieldValue.field().get().identity());
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
