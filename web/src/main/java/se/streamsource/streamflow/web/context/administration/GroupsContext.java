/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.Role;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;

/**
 * JAVADOC
 */
@Mixins(GroupsContext.Mixin.class)
public interface GroupsContext
      extends IndexContext<Iterable<Group>>, Context
{
   public Group create( @MaxLength(50) @Name("name") String name );

   abstract class Mixin
         implements GroupsContext
   {
      @Structure
      Module module;

      GroupsAdmin groups;

      void bind(@Uses Groups.Events groups)
      {
         this.groups = new GroupsAdmin(groups);
      }

      public Iterable<Group> index()
      {
         return groups.index();
      }

      public Group create( String name )
      {
         return groups.create(name);
      }

      class GroupsAdmin
         extends Role<Groups.Events>
      {
         GroupsAdmin(Groups.Events self)
         {
            super(self);
         }

         public Iterable<Group> index()
         {
            return ((Groups.Data) self).groups();
         }

         Group create(String name)
         {
            Group group = self.createdGroup(null, module.serviceFinder().<IdentityGenerator>findService(IdentityGenerator.class).get().generate(GroupEntity.class));
            group.changeDescription(name);
            self.addedGroup(null, group);
            return group;
         }
      }
   }
}
