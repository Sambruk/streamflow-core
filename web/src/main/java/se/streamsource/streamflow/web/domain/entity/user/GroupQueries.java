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
package se.streamsource.streamflow.web.domain.entity.user;

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.injection.scope.This;

import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.group.Participation;

/**
 * JAVADOC
 */
public interface GroupQueries
{
   Iterable<Group> allGroups();

   class Mixin
      implements GroupQueries
   {
      @This
      Participation.Data state;

      public Iterable<Group> allGroups()
      {
         List<Group> groups = new ArrayList<Group>();
         for (Group group : state.groups())
         {
            if (!groups.contains( group ))
               groups.add( group );

            // Add transitively
            Participation participation = (Participation) group;
            for (Participants group1 : participation.allGroups())
            {
               if (!groups.contains( group1 ))
                  groups.add( group );
            }
         }

         return groups;
      }

   }
}
