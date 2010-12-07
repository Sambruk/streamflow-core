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

package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnitsContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface OrganizationalUnitsContext
      extends IndexContext<Iterable<OrganizationalUnit>>, Context
{
   public void createorganizationalunit( @MaxLength(50) StringValue value );

   abstract class Mixin
         implements OrganizationalUnitsContext
   {
      @Structure
      Module module;

      public Iterable<OrganizationalUnit> index()
      {
         OrganizationalUnits.Data ous = RoleMap.role( OrganizationalUnits.Data.class );
         return ous.organizationalUnits();
      }

      public void createorganizationalunit( StringValue value )
      {
         OrganizationalUnits ous = RoleMap.role( OrganizationalUnits.class );

         ous.createOrganizationalUnit( value.string().get() );
      }
   }
}
