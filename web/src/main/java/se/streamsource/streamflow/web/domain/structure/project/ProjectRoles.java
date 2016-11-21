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
package se.streamsource.streamflow.web.domain.structure.project;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(ProjectRoles.Mixin.class)
public interface ProjectRoles
{
   ProjectRole createProjectRole(String name );

   boolean removeProjectRole( ProjectRole projectRole );

   void addProjectRole( ProjectRole projectRole );

   void mergeProjectRoles( ProjectRoles projectRoles );

   interface Data
   {
      @Aggregated
      ManyAssociation<ProjectRole> projectRoles();

      ProjectRole createdProjectRole( @Optional DomainEvent event, String id );

      void addedProjectRole( @Optional DomainEvent event, ProjectRole role );

      void removedProjectRole( @Optional DomainEvent event, ProjectRole role );
   }

   class Mixin
         implements ProjectRoles
   {
      @Service
      IdentityGenerator idGen;

      @This
      Data data;

      public ProjectRole createProjectRole( String name )
      {
         ProjectRole role = data.createdProjectRole( null, idGen.generate( Identity.class ));
         role.changeDescription( name );
         return role;
      }

      public void mergeProjectRoles( ProjectRoles projectRoles )
      {
         while (data.projectRoles().count() > 0)
         {
            ProjectRole role = data.projectRoles().get( 0 );
            removeProjectRole( role );
            projectRoles.addProjectRole( role );
         }
      }

      public void addProjectRole( ProjectRole projectRole )
      {
         if (data.projectRoles().contains( projectRole ))
         {
            return;
         }
         data.addedProjectRole( null, projectRole );
      }

      public boolean removeProjectRole( ProjectRole projectRole )
      {
         if (!data.projectRoles().contains( projectRole ))
         {
            return false;
         }

         data.removedProjectRole( null, projectRole );

         return true;
      }

   }
}
