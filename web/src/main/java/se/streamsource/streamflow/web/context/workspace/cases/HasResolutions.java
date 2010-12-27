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

package se.streamsource.streamflow.web.context.workspace.cases;

import se.streamsource.dci.api.InteractionConstraint;
import se.streamsource.dci.api.InteractionConstraintDeclaration;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.qi4j.api.util.Iterables.first;

/**
 * Check if current case has any possible resolutions
 */
@InteractionConstraintDeclaration(HasResolutions.HasResolutionsConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HasResolutions
{
   boolean value() default true; // True -> resolution should exist

   class HasResolutionsConstraint
         implements InteractionConstraint<HasResolutions>
   {
      public boolean isValid( HasResolutions hasResolutions, RoleMap roleMap )
      {
         CaseType type = RoleMap.role( TypedCase.Data.class ).caseType().get();
         if (hasResolutions.value())
         {
            return type != null && first( type.getSelectedResolutions() ) != null;
         } else
         {
            return type == null || first( type.getSelectedResolutions() ) == null;
         }
      }
   }
}
