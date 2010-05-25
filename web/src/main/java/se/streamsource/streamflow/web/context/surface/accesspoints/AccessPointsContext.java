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
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
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

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Iterator;

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
         ProxyUser proxyUSer = getAuthenticatedProxyUSer();

         if (proxyUSer == null)
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

         Organization organization = proxyUSer.organization().get();

         AccessPoints.Data data = (AccessPoints.Data) organization;

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( data.accessPoints() );

         return linksBuilder.newLinks();
      }

      private ProxyUser getAuthenticatedProxyUSer()
      {
         Iterator<Principal> principalIterator = context.get( Subject.class ).getPrincipals( Principal.class ).iterator();
         if (principalIterator.hasNext())
         {
            Principal principal = principalIterator.next();

            UserAuthentication authentication = module.unitOfWorkFactory().currentUnitOfWork().get( UserAuthentication.class, principal.getName() );

            if (authentication instanceof ProxyUser )
               return (ProxyUser) authentication;
         }

         return null;
      }

      public AccessPointContext context( String id )
      {
         ProxyUser proxyUSer = getAuthenticatedProxyUSer();
         if (proxyUSer == null) throw new ContextNotFoundException();

         AccessPoints.Data data = (AccessPoints.Data) proxyUSer.organization().get();

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