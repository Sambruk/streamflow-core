/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.context.workspace.cases.general;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Iterables;

import se.streamsource.dci.api.InteractionConstraint;
import se.streamsource.dci.api.InteractionConstraintDeclaration;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;

/**
 * Check if current form has a draft already
 */
@InteractionConstraintDeclaration(HasSignedSubmittedForm.Constraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HasSignedSubmittedForm
{
   boolean value() default true;

   class Constraint
         implements InteractionConstraint<HasSignedSubmittedForm>
   {
      public boolean isValid( HasSignedSubmittedForm hasSignedSubmittedFormAnnotation, RoleMap roleMap )
      {
         Form form = RoleMap.role( Form.class );
         Iterable<SubmittedFormValue> submissions = RoleMap.role( SubmittedForms.class ).getSubmittedFormValues(form);
         boolean anySigned = Iterables.matchesAny(hasSignature, submissions);

         if (hasSignedSubmittedFormAnnotation.value()) {
            return anySigned;
         }
         else {
            return !anySigned;
         }
      }

      private static Specification<SubmittedFormValue> hasSignature = new Specification<SubmittedFormValue>() {
         @Override
         public boolean satisfiedBy(SubmittedFormValue item) {
            return item.signatures().get().size() > 0;
         }
      };
   }
}
