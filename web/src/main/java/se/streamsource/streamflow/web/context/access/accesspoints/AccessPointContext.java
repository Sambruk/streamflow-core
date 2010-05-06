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

package se.streamsource.streamflow.web.context.access.accesspoints;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.AccessPointValue;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.context.access.accesspoints.ProxyUserContext;
import se.streamsource.streamflow.web.domain.entity.user.ProxyUserEntity;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUsers;

import java.util.ArrayList;

/**
 * JAVADOC
 */
@Mixins(AccessPointContext.Mixin.class)
public interface AccessPointContext
   extends SubContexts<ProxyUserContext>, IndexInteraction<LinksValue>, Interactions, DeleteInteraction
{
   AccessPointValue accesspoint();

   abstract class Mixin
      extends InteractionsMixin
      implements AccessPointContext
   {
      public AccessPointValue accesspoint()
      {
         ValueBuilder<AccessPointValue> builder = module.valueBuilderFactory().newValueBuilder( AccessPointValue.class );
         AccessPoint accessPoint = context.get( AccessPoint.class );
         AccessPoint.Data data = context.get( AccessPoint.Data.class );
         builder.prototype().entity().set( EntityReference.getEntityReference( accessPoint ));
         builder.prototype().name().set( accessPoint.getDescription() );
         builder.prototype().project().set( data.project().get().getDescription() );
         builder.prototype().caseType().set( data.caseType().get().getDescription() );

         builder.prototype().labels().set( new ArrayList<String>() );
         for (Label label : data.labels())
         {
            builder.prototype().labels().get().add( label.getDescription() );
         }

         return builder.newInstance();
      }

      public LinksValue index()
      {
         ProxyUsers.Data data = context.get( ProxyUsers.Data.class );
         Describable describable = context.get( Describable.class );

         TitledLinksBuilder linksBuilder = new TitledLinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( data.proxyUsers() );
         linksBuilder.addTitle( describable.getDescription() );

         return linksBuilder.newLinks();
      }

      public ProxyUserContext context( String id )
      {
         ProxyUsers.Data data = context.get( ProxyUsers.Data.class );
         for (ProxyUser proxyUser : data.proxyUsers() )
         {
            ProxyUserEntity entity = (ProxyUserEntity) proxyUser;
            if ( entity.identity().get().equals( id ) )
            {
               context.set( proxyUser );
            }
         }
         return subContext( ProxyUserContext.class);
      }

      public void delete() throws ResourceException
      {
         AccessPoint accessPoint = context.get( AccessPoint.class );
         AccessPoints accessPoints = context.get( AccessPoints.class );

         accessPoints.removeAccessPoint( accessPoint );
      }
   }
}