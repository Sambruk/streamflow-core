/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.InteractionConstraint;
import se.streamsource.dci.api.InteractionConstraintDeclaration;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.organization.FormOnRemove;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InteractionConstraintDeclaration(HasFormOnRemove.HasFormOnRemoveConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HasFormOnRemove
{
   boolean value() default true;

   class HasFormOnRemoveConstraint
         implements InteractionConstraint<HasFormOnRemove>
   {

      @Structure
      Module module;

      public boolean isValid( HasFormOnRemove hasFormOnRemove, RoleMap roleMap )
      {
         Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
         FormOnRemove.Data data = (FormOnRemove.Data) orgs.organization().get();

         boolean isSet = data.formOnRemove().get() != null;

         return hasFormOnRemove.value() == isSet;
      }
   }
}
