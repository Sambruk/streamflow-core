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
package se.streamsource.streamflow.web.rest.resource.external.integrationpoints;

import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Iterables;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.external.integrationpoints.IntegrationPointsContext;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoint;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoints;

/**
 * Created with IntelliJ IDEA.
 * User: arvidhuss
 * Date: 2/6/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class IntegrationPointsResource
   extends CommandQueryResource
   implements SubResources
{
   public IntegrationPointsResource()
   {
      super( IntegrationPointsContext.class );
   }
   public void resource( final String segment ) throws ResourceException
   {
      IntegrationPoints.Data integrationPoints =  RoleMap.role( IntegrationPoints.Data.class );
      IntegrationPoint integrationPoint = Iterables.first( Iterables.filter( new Specification<IntegrationPoint>()
      {
        public boolean satisfiedBy( IntegrationPoint item )
        {
           return item.getDescription().equalsIgnoreCase( segment );
        }
      }, integrationPoints.integrationPoints() ) );

      if( integrationPoint == null )
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND);
      } else
      {
         RoleMap.current().set( integrationPoint, IntegrationPoint.class );
         subResource( IntegrationPointResource.class );
      }

   }
}
