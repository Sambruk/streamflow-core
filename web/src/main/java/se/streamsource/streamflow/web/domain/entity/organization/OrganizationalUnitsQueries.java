/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.entity.organization;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnitsQueries.Mixin.class)
public interface OrganizationalUnitsQueries
{
   OrganizationalUnit getOrganizationalUnitByName( String name );

   class Mixin
      implements OrganizationalUnitsQueries
   {
      @This OrganizationalUnits.Data data;

      public OrganizationalUnit getOrganizationalUnitByName( String name )
      {
         for (OrganizationalUnit organizationalUnit : data.organizationalUnits())
         {
            if (((Describable.Data) organizationalUnit).description().get().equals( name ))
               return organizationalUnit;
         }
         throw new IllegalArgumentException( name );
      }
   }
}
