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
package se.streamsource.streamflow.web.application.pdf;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.PageEntity;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedPageValue;
import se.streamsource.streamflow.web.domain.structure.form.VisibilityRule;

/**
 * This class is able to validate visibility rules for a SubmittedFormValue.
 *
 */
public class FormVisibilityRuleValidator
{
   @Structure
   Module module;

   private SubmittedFormValue submittedForm;

   public FormVisibilityRuleValidator( @Uses SubmittedFormValue submittedForm )
   {
      this.submittedForm = submittedForm;
   }

   public boolean visible( SubmittedFieldValue submittedFieldValue )
   {
      boolean visible;

      final VisibilityRule fieldRule = module.unitOfWorkFactory().currentUnitOfWork().get( FieldEntity.class, submittedFieldValue.field().get().identity() );

      if( fieldRule.getRule() == null || Strings.empty( fieldRule.getRule().field().get() ))
      {
         visible = true;
      } else
      {
         // find submitted field matching rule field and fetch value - check if submitted field is visible - yes - validate
         // this call will be recursive if several fields are chained by visibility rules.
         SubmittedFieldValue fieldValue = Iterables.first( Iterables.filter( new Specification<SubmittedFieldValue>()
         {
            public boolean satisfiedBy( SubmittedFieldValue field )
            {
               return field.field().get().identity().equals( fieldRule.getRule().field().get() );
            }
         }, submittedForm.fields() ) );

         if( visible( fieldValue ) )
         {
            // validate
            visible = fieldRule.validate( fieldValue.value().get() );

         } else
         {
            visible = false;
         }
      }

      return visible;
   }

   public boolean visible( SubmittedPageValue submittedPageValue )
   {
      boolean visible;

      final VisibilityRule pageRule = module.unitOfWorkFactory().currentUnitOfWork().get( PageEntity.class, submittedPageValue.page().get().identity() );

      if( pageRule.getRule() == null || Strings.empty( pageRule.getRule().field().get() ))
      {
         visible = true;
      } else
      {
         //find submitted field matching rule field and fetch value - check if submitted field is visible - yes - validate
         SubmittedFieldValue fieldValue = Iterables.first( Iterables.filter( new Specification<SubmittedFieldValue>()
         {
            public boolean satisfiedBy( SubmittedFieldValue field )
            {
               return field.field().get().identity().equals( pageRule.getRule().field().get() );
            }
         }, submittedForm.fields() ) );

         if( visible( fieldValue ) )
         {
           // validate
           visible = pageRule.validate( fieldValue.value().get() );

         } else
         {
            visible = false;
         }
      }

      return visible;
   }
}
