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

package se.streamsource.streamflow.web.resource.organizations;

import org.restlet.resource.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.api.*;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.OrganizationalUnitsContext;
import se.streamsource.streamflow.web.domain.structure.organization.*;

/**
 * JAVADOC
 */
public class OrganizationalUnitsResource
      extends CommandQueryResource
      implements SubResources
{
   public OrganizationalUnitsResource()
   {
      super(OrganizationalUnitsContext.class);
   }

   public LinksValue index() throws Throwable
   {
      Iterable<OrganizationalUnit> ous = (Iterable<OrganizationalUnit>) invoke();

      return new LinksBuilder(module.valueBuilderFactory()).addDescribables(ous).newLinks();
   }

   public void resource( String segment ) throws ResourceException
   {
      // Add both OU and ownning Organization to RoleMap, but make sure OU takes precedence
      OrganizationalUnit ou = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationalUnit.class, segment );
      OwningOrganization orgOwner = (OwningOrganization) ou;
      RoleMap.current().set( orgOwner.organization().get() );
      RoleMap.setCurrentRoleMap( new RoleMap(RoleMap.current()) );
      RoleMap.current().set( ou );

      subResource( OrganizationalUnitResource.class );
   }
}
