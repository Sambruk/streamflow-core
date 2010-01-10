/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Check that a Task is in a particular state
 */
@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Constraints(HasStatus.HasStatusConstraint.class)
public @interface HasStatus
{
   States[] value();

   public class HasStatusConstraint
         implements Constraint<HasStatus, Status>
   {
      public boolean isValid( HasStatus hasStatus, Status value )
      {
         States status = ((Status.Data) value).status().get();

         for (States states : hasStatus.value())
         {
            if (states.equals( status ))
               return true;
         }

         return false;
      }
   }
}