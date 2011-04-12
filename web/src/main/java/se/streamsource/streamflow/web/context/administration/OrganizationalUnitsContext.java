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

package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.structure.*;
import org.qi4j.library.constraints.annotation.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnitsContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface OrganizationalUnitsContext
      extends IndexContext<Iterable<OrganizationalUnit>>, Context
{
   public void createorganizationalunit( @MaxLength(50) @Name("string") String value );

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

      public void createorganizationalunit( String name )
      {
         OrganizationalUnits ous = RoleMap.role( OrganizationalUnits.class );

         ous.createOrganizationalUnit( name );
      }
   }
}
