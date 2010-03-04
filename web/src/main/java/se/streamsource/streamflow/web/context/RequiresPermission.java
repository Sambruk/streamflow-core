/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.context;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import se.streamsource.dci.context.InteractionContext;
import se.streamsource.streamflow.web.application.security.StreamFlowPrincipal;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;

import javax.security.auth.Subject;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.Principal;

/**
 * JAVADOC
 */
@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Constraints(RequiresPermission.RequiresPermissionConstraint.class)
public @interface RequiresPermission
{
   String value();

   class RequiresPermissionConstraint
      implements Constraint<RequiresPermission, InteractionContext>
   {
      public boolean isValid( RequiresPermission requiresPermission, InteractionContext context )
      {
         Authorization policy = context.role( Authorization.class );

         Principal principal = context.role( Subject.class ).getPrincipals( Principal.class ).iterator().next();

         return policy.hasPermission( principal.getName(), requiresPermission.value() );
      }
   }
}
