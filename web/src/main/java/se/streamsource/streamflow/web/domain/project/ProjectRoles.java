/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Concerns(ProjectRoles.DescribeCreatedRoleConcern.class)
@Mixins(ProjectRoles.Mixin.class)
public interface ProjectRoles
{
   ProjectRoleEntity createProjectRole( String name );

   boolean removeProjectRole( ProjectRole projectRole );

   void addProjectRole( ProjectRole projectRole );

   void mergeProjectRoles( ProjectRoles projectRoles );

   interface Data
   {
      @Aggregated
      ManyAssociation<ProjectRole> projectRoles();

      ProjectRoleEntity createdProjectRole( DomainEvent event, String id );

      void addedProjectRole( DomainEvent event, ProjectRoleEntity role );

      void removedProjectRole( DomainEvent event, ProjectRole role );
   }

   abstract class Mixin
         implements ProjectRoles, Data
   {

      public void mergeProjectRoles( ProjectRoles projectRoles )
      {
         while (this.projectRoles().count() > 0)
         {
            ProjectRole role = this.projectRoles().get( 0 );
            removeProjectRole( role );
            projectRoles.addProjectRole( role );
         }
      }

      public void addProjectRole( ProjectRole projectRole )
      {
         if (projectRoles().contains( projectRole ))
         {
            return;
         }
         addedProjectRole( DomainEvent.CREATE, (ProjectRoleEntity) projectRole );
      }
   }


   abstract class DescribeCreatedRoleConcern
         extends ConcernOf<ProjectRoles>
         implements ProjectRoles
   {
      public ProjectRoleEntity createProjectRole( String name )
      {
         ProjectRoleEntity role = next.createProjectRole( name );
         role.changeDescription( name );
         return role;
      }
   }
}
