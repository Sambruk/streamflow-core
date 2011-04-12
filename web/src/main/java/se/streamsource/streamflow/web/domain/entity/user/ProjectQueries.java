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

package se.streamsource.streamflow.web.domain.entity.user;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.web.domain.structure.group.*;
import se.streamsource.streamflow.web.domain.structure.project.*;

import java.util.*;

/**
 * JAVADOC
 */
@Mixins(ProjectQueries.Mixin.class)
public interface ProjectQueries
{
   /**
    * Return all projects that this participant has access to.
    * This includes projects that this participant is transitively
    * a member of through groups.
    *
    * @return all projects that this participant is a member of
    */
   Iterable<Project> allProjects();

   class Mixin
      implements ProjectQueries
   {
      @This
      Memberships.Data memberships;

      @This
      Participant participant;

      public Iterable<Project> allProjects()
      {
         List<Project> projects = new ArrayList<Project>();
         // List my own
         for (Members members : memberships.projects())
         {
            if (!projects.contains( members )
                  && !((Removable.Data) members).removed().get())
               projects.add( (Project) members );
         }

         // Get group projects
         for (Participants group : participant.allGroups())
         {
            ProjectQueries groupProjects = (ProjectQueries) group;
            for (Project groupProject : groupProjects.allProjects())
            {
               if (!projects.contains( groupProject )
                     && !((Removable.Data) groupProject).removed().get())
                  projects.add( groupProject );
            }
         }

         return projects;
      }
   }
}
