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
package se.streamsource.streamflow.web.rest.resource.external.integrationpoints;

import org.qi4j.api.entity.Identity;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.external.integrationpoints.IntegrationPointContext;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCase;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCasesQueries;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoint;
import se.streamsource.streamflow.web.rest.resource.external.ShadowCaseResource;

public class IntegrationPointResource
   extends CommandQueryResource
   implements SubResources
{
   public IntegrationPointResource()
   {
      super( IntegrationPointContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      IntegrationPoint integrationPoint = RoleMap.role( IntegrationPoint.class );

      ShadowCasesQueries query = RoleMap.role( ShadowCasesQueries.class );
      ShadowCase caze = query.findExternalCase( integrationPoint.getDescription().toLowerCase(), segment );

      if( caze == null )
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND);
      } else
      {
         setRole( ShadowCase.class, ((Identity)caze).identity().get() );
         subResource( ShadowCaseResource.class );
      }
   }
}
