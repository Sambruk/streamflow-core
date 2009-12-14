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

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;

/**
 * JAVADOC
 */
@Mixins(Projects.Mixin.class)
public interface Projects
{
   ProjectEntity createProject( String name );

   boolean removeProject( Project project );

   void addProject( Project project );

   interface Data
   {
      @Aggregated
      ManyAssociation<Project> projects();

      ProjectEntity createdProject( DomainEvent event, String id );

      void removedProject( DomainEvent event, Project project );

      void addedProject( DomainEvent event, Project project );

      void mergeProjects( Projects projects );

      ProjectEntity getProjectByName( String name );
   }

   abstract class Mixin
         implements Projects, Data
   {
      @This
      OrganizationalUnitEntity ou;

      @Service
      IdentityGenerator idgen;

      @Structure
      UnitOfWorkFactory uowf;

      public ProjectEntity createProject( String name )
      {
         String id = idgen.generate( ProjectEntity.class );

         ProjectEntity project = createdProject( DomainEvent.CREATE, id );
         addedProject( DomainEvent.CREATE, project );
         project.changeDescription( name );

         return project;
      }

      public ProjectEntity createdProject( DomainEvent event, String id )
      {
         EntityBuilder<ProjectEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( ProjectEntity.class, id );
         builder.instance().organizationalUnit().set( ou );
         return builder.newInstance();
      }

      public void mergeProjects( Projects projects )
      {
         while (this.projects().count() > 0)
         {
            Project project = this.projects().get( 0 );
            removeProject( project );
            projects.addProject( project );
         }

      }

      public void addProject( Project project )
      {

         if (projects().contains( project ))
         {
            return;
         }
         addedProject( DomainEvent.CREATE, project );
      }

      public ProjectEntity getProjectByName( String name )
      {
         return (ProjectEntity) Describable.Mixin.getDescribable( projects(), name );
      }
   }


}