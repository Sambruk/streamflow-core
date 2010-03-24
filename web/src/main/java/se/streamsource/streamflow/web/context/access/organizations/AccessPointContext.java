/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.access.organizations;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.domain.entity.user.ProxyUserEntity;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUsers;

/**
 * JAVADOC
 */
@Mixins(AccessPointContext.Mixin.class)
public interface AccessPointContext
   extends SubContexts<ProxyUserContext>, IndexContext<LinksValue>, Context
{
   abstract class Mixin
      extends ContextMixin
      implements AccessPointContext
   {
      public LinksValue index()
      {
         ProxyUsers.Data data = context.role( ProxyUsers.Data.class );
         Describable describable = context.role( Describable.class );

         TitledLinksBuilder linksBuilder = new TitledLinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( data.proxyUsers() );
         linksBuilder.addTitle( describable.getDescription() );

         return linksBuilder.newLinks();
      }

      public ProxyUserContext context( String id )
      {
         ProxyUsers.Data data = context.role( ProxyUsers.Data.class );
         for (ProxyUser proxyUser : data.proxyUsers() )
         {
            ProxyUserEntity entity = (ProxyUserEntity) proxyUser;
            if ( entity.identity().get().equals( id ) )
            {
               context.playRoles( proxyUser );
            }
         }
         return subContext( ProxyUserContext.class);
      }
   }
}