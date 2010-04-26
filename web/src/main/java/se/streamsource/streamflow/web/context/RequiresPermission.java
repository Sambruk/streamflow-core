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

package se.streamsource.streamflow.web.context;

import se.streamsource.dci.api.InteractionConstraint;
import se.streamsource.dci.api.InteractionConstraintDeclaration;
import se.streamsource.dci.api.Context;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;

import javax.security.auth.Subject;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.Principal;
import java.util.Iterator;

/**
 * JAVADOC
 */
@InteractionConstraintDeclaration(RequiresPermission.RequiresPermissionConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission
{
   String value();

   class RequiresPermissionConstraint
      implements InteractionConstraint<RequiresPermission>
   {
      public boolean isValid( RequiresPermission requiresPermission, Context context )
      {
         Authorization policy = context.get( Authorization.class );

         Iterator<Principal> principalIterator = context.get( Subject.class ).getPrincipals( Principal.class ).iterator();
         if (principalIterator.hasNext())
         {
            Principal principal = principalIterator.next();

            return policy.hasPermission( principal.getName(), requiresPermission.value() );
         } else
         {
            return false;
         }
      }
   }
}
