/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;

/**
 * JAVADOC
 */
@Mixins(GroupsContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface GroupsContext
      extends IndexContext<Iterable<Group>>, Context
{
   public void creategroup( @MaxLength(50) StringValue name );

   abstract class Mixin
         implements GroupsContext
   {
      @Structure
      Module module;

      public Iterable<Group> index()
      {
         Groups.Data groups = RoleMap.role( Groups.Data.class );

         return groups.groups();
      }

      public void creategroup( StringValue name )
      {
         Groups groups = RoleMap.role( Groups.class );
         groups.createGroup( name.string().get() );
      }
   }
}
