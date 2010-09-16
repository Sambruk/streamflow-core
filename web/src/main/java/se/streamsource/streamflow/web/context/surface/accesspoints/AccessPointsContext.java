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

package se.streamsource.streamflow.web.context.surface.accesspoints;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

/**
 * JAVADOC
 */
@Mixins(AccessPointsContext.Mixin.class)
public interface AccessPointsContext
   extends SubContexts<AccessPointContext>, IndexContext<LinksValue>, Context
{
   abstract class Mixin
      extends ContextMixin
      implements AccessPointsContext
   {
      public LinksValue index()
      {
         UserAuthentication authentication = roleMap.get( UserAuthentication.class );

         if ( ! (authentication instanceof ProxyUser) )
         {
            TitledLinksBuilder builder = new TitledLinksBuilder( module.valueBuilderFactory() );
            ValueBuilder<LinkValue> valueBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
            valueBuilder.prototype().href().set( ".." );
            valueBuilder.prototype().id().set( "" );
            valueBuilder.prototype().text().set( "back" );

            builder.addLink( valueBuilder.newInstance() );
            builder.addTitle( "USER NOT A PROXY USER" );

            return builder.newLinks();
         }

         ProxyUser proxyUser = (ProxyUser) authentication;
         Organization organization = proxyUser.organization().get();

         AccessPoints.Data data = (AccessPoints.Data) organization;

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( data.accessPoints() );

         return linksBuilder.newLinks();
      }

      public AccessPointContext context( String id )
      {
         ProxyUser proxyUser = roleMap.get( ProxyUser.class );

         AccessPoints.Data data = (AccessPoints.Data) proxyUser.organization().get();

         for (AccessPoint accessPoint : data.accessPoints())
         {
            AccessPointEntity entity = (AccessPointEntity) accessPoint;
            if ( entity.identity().get().equals( id ) )
            {
               roleMap.set( accessPoint );
               return subContext( AccessPointContext.class);
            }
         }
         throw new ContextNotFoundException();
      }
   }
}