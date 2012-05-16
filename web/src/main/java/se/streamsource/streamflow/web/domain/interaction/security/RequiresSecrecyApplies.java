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
package se.streamsource.streamflow.web.domain.interaction.security;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.entity.association.Association;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Check that security settings applied for a specific case
 */
@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Constraints(RequiresSecrecyApplies.RequiresSecrecyAppliesConstraint.class)
public @interface RequiresSecrecyApplies
{
   public class RequiresSecrecyAppliesConstraint
         implements Constraint<RequiresSecrecyApplies, Ownable.Data>
   {
      public boolean isValid( RequiresSecrecyApplies applies, Ownable.Data value )
      {
         Association<Owner> owner = value.owner();
         if ( owner.get() == null ) return false;

         return ((CaseAccessOptionalDefaults.Data)owner.get()).enableOptionalDefaults().get();
      }
   }
}