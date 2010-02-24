/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.role.Role;
import se.streamsource.streamflow.web.domain.structure.role.Roles;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(RolesContext.Mixin.class)
public interface RolesContext
   extends SubContexts<RoleContext>, Context
{
   LinksValue roles();
   void createrole( StringDTO name );

   abstract class Mixin
      extends ContextMixin
      implements RolesContext
   {
      public LinksValue roles()
      {
         Roles.Data roles = context.role( Roles.Data.class );

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "role" ).addDescribables( roles.roles() ).newLinks();
      }

      public void createrole( StringDTO name )
      {
         Roles roles = context.role(Roles.class);

         roles.createRole( name.string().get() );
      }

      public RoleContext context( String id )
      {
         context.playRoles( module.unitOfWorkFactory().currentUnitOfWork().get( Role.class, id ));

         return subContext( RoleContext.class );
      }
   }
}
