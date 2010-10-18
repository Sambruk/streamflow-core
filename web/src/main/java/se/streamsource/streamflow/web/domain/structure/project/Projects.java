/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.interaction.gtd.ChangesOwner;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;

/**
 * JAVADOC
 */
@Mixins(Projects.Mixin.class)
public interface Projects
{
   Project createProject( String name );

   boolean removeProject( Project project );

   @ChangesOwner
   void addProject( Project project );

   interface Data
   {
      @Aggregated
      ManyAssociation<Project> projects();

      Project createdProject( DomainEvent event, String id );

      void removedProject( DomainEvent event, Project project );

      void addedProject( DomainEvent event, Project project );

      void mergeProjects( Projects projects );

      Project getProjectByName( String name );
   }

   abstract class Mixin
         implements Projects, Data
   {
      @This
      OrganizationalUnit ou;

      @Service
      IdentityGenerator idgen;

      @Structure
      UnitOfWorkFactory uowf;

      @This Data data;

      public Project createProject( String name )
      {
         String id = idgen.generate( Identity.class );

         Project project = createdProject( DomainEvent.CREATE, id );
         addProject( project );
         project.changeDescription( name );

         return project;
      }

      public Project createdProject( DomainEvent event, String id )
      {
         EntityBuilder<Project> builder = uowf.currentUnitOfWork().newEntityBuilder( Project.class, id );
         builder.instanceFor( OwningOrganizationalUnit.Data.class).organizationalUnit().set( ou );
         return builder.newInstance();
      }

      public void mergeProjects( Projects projects )
      {
         while (data.projects().count() > 0)
         {
            Project project = data.projects().get( 0 );
            removeProject( project );
            projects.addProject( project );
         }
      }

      public void addProject( Project project )
      {
         if (!data.projects().contains( project ))
         {
            data.addedProject( DomainEvent.CREATE, project );
         }
      }

      public boolean removeProject( Project project )
      {
         if (!data.projects().contains( project ))
            return false;

         data.removedProject( DomainEvent.CREATE, project );
         project.removeEntity();              
         return true;
      }

      public Project getProjectByName( String name )
      {
         return Describable.Mixin.getDescribable( data.projects(), name );
      }

   }


}