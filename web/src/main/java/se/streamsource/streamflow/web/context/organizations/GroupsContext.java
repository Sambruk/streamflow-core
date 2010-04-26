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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;

/**
 * JAVADOC
 */
@Mixins(GroupsContext.Mixin.class)
public interface GroupsContext
   extends SubContexts<GroupContext>, Interactions
{
   public LinksValue groups();
   public void creategroup( StringValue name );

   abstract class Mixin
      extends InteractionsMixin
      implements GroupsContext
   {
      @Structure
      Module module;

      public LinksValue groups()
      {
         Groups.Data groups = context.get(Groups.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "group" ).addDescribables( groups.groups() ).newLinks();
      }

      public void creategroup( StringValue name )
      {
         Groups groups = context.get(Groups.class);
         groups.createGroup( name.string().get() );
      }

      public GroupContext context( String id )
      {
         Group group = module.unitOfWorkFactory().currentUnitOfWork().get( GroupEntity.class, id );
         if (!context.get(Groups.Data.class).groups().contains( group ))
            throw new IllegalArgumentException("Invalid group");
         
         context.set( group, GroupEntity.class);

         return subContext( GroupContext.class );
      }
   }
}
