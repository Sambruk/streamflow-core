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
package se.streamsource.streamflow.web.rest.resource.external.administration.integrationpoints;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.administration.external.integrationpoints.IntegrationPointsAdministrationContext;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoints;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * Created with IntelliJ IDEA.
 * User: arvidhuss
 * Date: 2/6/13
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class IntegrationPointsAdministrationResource
   extends CommandQueryResource
   implements SubResources
{
   public IntegrationPointsAdministrationResource()
   {
      super( IntegrationPointsAdministrationContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      findManyAssociation( role(IntegrationPoints.Data.class).integrationPoints(), segment );
      subResource( IntegrationPointAdministrationResource.class );
   }
}
