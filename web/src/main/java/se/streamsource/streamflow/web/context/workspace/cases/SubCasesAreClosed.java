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
package se.streamsource.streamflow.web.context.workspace.cases;

import se.streamsource.dci.api.InteractionConstraint;
import se.streamsource.dci.api.InteractionConstraintDeclaration;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.SubCases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check if current case has only closed subcases (recursively)
 */
@InteractionConstraintDeclaration(SubCasesAreClosed.SubCasesAreClosedConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SubCasesAreClosed
{
   class SubCasesAreClosedConstraint
         implements InteractionConstraint<SubCasesAreClosed>
   {
      public boolean isValid( SubCasesAreClosed hasResolutions, RoleMap roleMap )
      {
         return checkClosedSubcases( RoleMap.role( SubCases.Data.class ) );
      }

      private boolean checkClosedSubcases( SubCases.Data subcases )
      {
         for (Case aCase : subcases.subCases())
         {
            if (!aCase.isStatus( CaseStates.CLOSED ) || !checkClosedSubcases( (SubCases.Data) aCase ))
               return false;
         }

         return true;
      }
   }
}
