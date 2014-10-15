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
package se.streamsource.streamflow.web.context.workspace.cases;

import se.streamsource.dci.api.InteractionConstraint;
import se.streamsource.dci.api.InteractionConstraintDeclaration;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.project.CaseTypeRequired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check if current case needs to have case type set before it can be closed.
 */
@InteractionConstraintDeclaration(RequiresCaseType.RequiresCaseTypeConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresCaseType
{
   boolean value() default true; // True -> CaseType must be set before close

   class RequiresCaseTypeConstraint
         implements InteractionConstraint<RequiresCaseType>
   {
      public boolean isValid( RequiresCaseType requiresCaseType, RoleMap roleMap )
      {
         Owner owner =  RoleMap.role(Ownable.Data.class).owner().get();
         boolean caseTypeRequired = ((CaseTypeRequired.Data)owner).caseTypeRequired().get();
         CaseType type = RoleMap.role( TypedCase.Data.class ).caseType().get();

         if (requiresCaseType.value())
         {
             return isCaseTypeRequiredAndNotSet(caseTypeRequired, type);
         } else
         {
             return caseTypeRequired && type != null;
         }
      }

       private boolean isCaseTypeRequiredAndNotSet(boolean caseTypeRequired, CaseType type) {
           return caseTypeRequired && type == null;
       }
   }
}
