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

package se.streamsource.streamflow.web.context.surface.administration.organizations.accesspoints;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;

/**
 * JAVADOC
 */
@Mixins(AccessPointsContext.Mixin.class)
public interface AccessPointsContext
   extends SubContexts<AccessPointContext>, IndexInteraction<LinksValue>, Interactions
{
   abstract class Mixin
      extends InteractionsMixin
      implements AccessPointsContext
   {
      public LinksValue index()
      {
         AccessPoints.Data data = context.get( AccessPoints.Data.class );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( data.accessPoints() );

         return linksBuilder.newLinks();
      }

      public AccessPointContext context( String id )
      {
         AccessPoints.Data data = context.get( AccessPoints.Data.class );

         for (AccessPoint accessPoint : data.accessPoints())
         {
            AccessPointEntity entity = (AccessPointEntity) accessPoint;
            if ( entity.identity().get().equals( id ) )
            {
               context.set( accessPoint );
            }
         }
         return subContext( AccessPointContext.class);
      }
   }
}