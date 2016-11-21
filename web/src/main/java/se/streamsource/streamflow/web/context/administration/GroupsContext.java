/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImporterService;

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

      public Iterable<Group> index()
      {
         Groups.Data groups = RoleMap.role( Groups.Data.class );
         return groups.groups();
      }

      @ServiceAvailable( service = LdapImporterService.class, availability = false )
      public Group create( String name )
      {
         Groups groups = RoleMap.role( Groups.class );
         return groups.createGroup( name );
      }
   }
}
