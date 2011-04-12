/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context;

import se.streamsource.dci.api.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;

import java.lang.annotation.*;
import java.security.*;

/**
 * Check if current principal has a given permission
 */
@InteractionConstraintDeclaration(RequiresPermission.RequiresPermissionConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresPermission
{
   PermissionType value();

   class RequiresPermissionConstraint
         implements InteractionConstraint<RequiresPermission>
   {
      public boolean isValid( RequiresPermission requiresPermission, RoleMap roleMap )
      {
         try
         {
            Principal principal = roleMap.get( Principal.class );

            // Administrator has all permissions
            if (principal.getName().equals("administrator"))
               return true;

            Authorization policy = roleMap.get( Authorization.class );
            return policy.hasPermission( principal.getName(), requiresPermission.value().name() );
         } catch (IllegalArgumentException e)
         {
            return false;
         }
      }
   }
}
