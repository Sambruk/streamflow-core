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
package se.streamsource.streamflow.web.context.external.integrationpoints;


import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoint;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoints;

import static se.streamsource.dci.api.RoleMap.*;

@Mixins( IntegrationPointsContext.Mixin.class )
public interface IntegrationPointsContext
   extends Context, IndexContext<LinksValue>
{
   abstract class Mixin
      implements IntegrationPointsContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         IntegrationPoints.Data data = role( IntegrationPoints.Data.class );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         for( IntegrationPoint point : data.integrationPoints() )
         {
            // we dont want to expose the UUID for integration point, we take description instead!!
            linksBuilder.addLink( point.getDescription(), point.getDescription() );
         }

         return linksBuilder.newLinks();
      }
   }
}
