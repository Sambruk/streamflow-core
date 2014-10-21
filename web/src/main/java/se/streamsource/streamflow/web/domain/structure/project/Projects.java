/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.project;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.interaction.gtd.ChangesOwner;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;

/**
 * JAVADOC
 */
@Mixins(Projects.Mixin.class)
public interface Projects
   extends ProjectsQueries
{
   Project createProject( String name );

   boolean removeProject( Project project );

   @ChangesOwner
   void addProject( Project project );

   void mergeProjects( Projects projects );

   interface Data
   {
      @Aggregated
      ManyAssociation<Project> projects();

      Project createdProject( @Optional DomainEvent event, String id );

      void removedProject( @Optional DomainEvent event, Project project );

      void addedProject( @Optional DomainEvent event, Project project );
   }

   abstract class Mixin
         implements Projects, Data
   {
      @This
      OrganizationalUnit ou;

      @Service
      IdentityGenerator idgen;

      @Structure
      Module module;

      @This Data data;

      public Project createProject( String name )
      {
         String id = idgen.generate( Identity.class );

         Project project = createdProject( null, id );
         addProject( project );
         project.changeDescription( name );
         project.changeCaseTypeRequired( true );

         return project;
      }

      public Project createdProject( DomainEvent event, String id )
      {
         EntityBuilder<Project> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Project.class, id );
         builder.instanceFor( OwningOrganizationalUnit.Data.class).organizationalUnit().set( ou );
         return builder.newInstance();
      }

      public void mergeProjects( Projects projects )
      {
         while (data.projects().count() > 0)
         {
            Project project = data.projects().get( 0 );
            removedProject( null, project );
            projects.addProject( project );
         }
      }

      public void addProject( Project project )
      {
         if (!data.projects().contains( project ))
         {
            data.addedProject( null, project );
         }
      }

      public boolean removeProject( Project project )
      {
         if (data.projects().contains( project ))
         {

            data.removedProject( null, project );
            project.removeEntity();
            return true;
         } else
            return false;
      }
   }
}