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
package se.streamsource.streamflow.web.rest.resource.administration;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.administration.AdministrationContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.rest.resource.organizations.OrganizationalUnitsResource;
import se.streamsource.streamflow.web.rest.resource.organizations.OrganizationsResource;

/**
 * JAVADOC
 */
public class AdministrationResource
      extends CommandQueryResource
{
   public AdministrationResource()
   {
      super( AdministrationContext.class );
   }

   @SubResource
   public void server()
   {
      subResource( ServerResource.class );
   }

   @SubResource
   public void organizations()
   {
      setRole( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
      subResource( OrganizationsResource.class );
   }

   @SubResource
   public void organizationalunits()
   {
      subResource( OrganizationalUnitsResource.class );
   }
}
