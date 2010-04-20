/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import se.streamsource.streamflow.domain.interaction.gtd.States;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Check that a Case is in a particular state
 */
@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Constraints(RequiresStatus.RequiresStatusConstraint.class)
public @interface RequiresStatus
{
   States[] value();

   public class RequiresStatusConstraint
         implements Constraint<RequiresStatus, Status>
   {
      public boolean isValid( RequiresStatus requiresStatus, Status value )
      {
         States status = ((Status.Data) value).status().get();

         for (States states : requiresStatus.value())
         {
            if (states.equals( status ))
               return true;
         }

         return false;
      }
   }
}