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
package se.streamsource.streamflow.web.domain.interaction.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;

/**
 * Check that security settings applied for a specific case
 */
@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Constraints(RequiresUnrestricted.RequiresSecrecyAppliesConstraint.class)
public @interface RequiresUnrestricted
{
   public class RequiresSecrecyAppliesConstraint
         implements Constraint<RequiresUnrestricted, CaseAccessRestriction.Data>
   {
      public boolean isValid( RequiresUnrestricted applies, CaseAccessRestriction.Data value )
      {
         return !value.restricted().get();
      }
   }
}