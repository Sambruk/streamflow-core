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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.streamsource.dci.api.InteractionConstraint;
import se.streamsource.dci.api.InteractionConstraintDeclaration;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.FormOnClose;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.form.Form;

/**
 * Check if current case has any possible resolutions
 */
@InteractionConstraintDeclaration(HasFormOnClose.HasFormOnCloseConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HasFormOnClose
{
   boolean value() default true; // True -> Form on close should exist

   class HasFormOnCloseConstraint
         implements InteractionConstraint<HasFormOnClose>
   {
      public boolean isValid( HasFormOnClose hasFormOnClose, RoleMap roleMap )
      {
         CaseType type = RoleMap.role( TypedCase.Data.class ).caseType().get();
         Form form = type == null ? null : ((FormOnClose.Data)type).formOnClose().get();
         if (hasFormOnClose.value())
         {
            return type != null && form != null;
         } else
         {
            return type == null || form == null;
         }
      }
   }
}
