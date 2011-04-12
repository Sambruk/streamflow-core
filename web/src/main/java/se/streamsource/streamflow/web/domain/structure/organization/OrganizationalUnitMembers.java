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

package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import se.streamsource.streamflow.web.domain.structure.group.*;
import se.streamsource.streamflow.web.domain.structure.project.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnitMembers.Mixin.class)
public interface OrganizationalUnitMembers
{
   /**
    * Check whether the user is a member/participant of the projects
    * and/or groups on this OU
    *
    * @param user
    * @return
    */
   boolean isMemberOrParticipant( User user );

   class Mixin
         implements OrganizationalUnitMembers
   {
      @This
      Projects.Data projects;

      @This
      Groups.Data groups;

      public boolean isMemberOrParticipant( User user )
      {
         // Check projects
         for (Project project : projects.projects())
         {
            if (user.isMember( project ))
               return true;
         }

         // Check groups
         for (Group group : groups.groups())
         {
            if (group.isParticipant( user ))
               return true;
         }

         return false;
      }
   }
}
