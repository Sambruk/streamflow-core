/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.Principal;

import se.streamsource.dci.api.InteractionConstraint;
import se.streamsource.dci.api.InteractionConstraintDeclaration;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;

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
