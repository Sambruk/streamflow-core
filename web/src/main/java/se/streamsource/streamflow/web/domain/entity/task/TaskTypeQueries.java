/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.entity.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.Specification;
import se.streamsource.streamflow.web.domain.entity.tasktype.TaskTypesQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.tasktype.SelectedTaskTypes;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskTypes;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JAVADOC
 */

@Mixins(TaskTypeQueries.Mixin.class)
public interface TaskTypeQueries
{
   void taskTypes( LinksBuilder builder);

   List<Project> possibleProjects();

   List<User> possibleUsers();

   class Mixin
         implements TaskTypeQueries
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Ownable.Data ownable;

      @This
      TypedTask.Data typedTask;

      @This
      Assignable assignable;

      public void taskTypes( LinksBuilder builder)
      {
         Owner owner = ownable.owner().get();
         if (owner instanceof OrganizationParticipations)
         {
            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) owner;

            for (Organization organization : orgs.organizations())
            {
               TaskTypes.Data taskTypesList = (TaskTypes.Data) organization;
               for (TaskType taskType : taskTypesList.taskTypes())
               {
                  builder.addDescribable( taskType, organization );
               }
            }
         } else if (owner instanceof OwningOrganizationalUnit.Data)
         {
            if (assignable.isAssigned())
            {
               // Show only task types from project
               SelectedTaskTypes.Data selectedTaskTypes = (SelectedTaskTypes.Data) owner;
               Describable describableOwner = (Describable) owner;
               for (TaskType taskType : selectedTaskTypes.selectedTaskTypes())
               {
                  builder.addDescribable( taskType, describableOwner );
               }
            } else
            {
               OwningOrganizationalUnit.Data ouOwner = (OwningOrganizationalUnit.Data) owner;
               OrganizationalUnit ou = ouOwner.organizationalUnit().get();
               Organization org = ((OwningOrganization) ou).organization().get();

               TaskTypesQueries taskTypesQueries = (TaskTypesQueries) org;
               taskTypesQueries.taskTypes( builder, new Specification<TaskType>()
               {
                  public boolean valid( TaskType instance )
                  {
                     return true;
                  }
               });
            }
         }
      }

      public List<Project> possibleProjects()
      {


         Owner owner = ownable.owner().get();
         if (owner instanceof OrganizationParticipations)
         {
            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) owner;

            List<Project> projects = new ArrayList<Project>( );

            for (Organization organization : orgs.organizations())
            {
               TaskTypesQueries taskTypesQueries = (TaskTypesQueries) organization;
               QueryBuilder<Project> builder = taskTypesQueries.possibleProjects( typedTask.taskType().get() );
               Query<Project> query = builder.newQuery( uowf.currentUnitOfWork() );
               for (Project project : query)
               {
                  projects.add( project );
               }
            }

            return projects;
         } else if (owner instanceof OwningOrganizationalUnit.Data)
         {
            OwningOrganizationalUnit.Data ouOwner = (OwningOrganizationalUnit.Data) owner;
            OrganizationalUnit ou = ouOwner.organizationalUnit().get();
            TaskTypesQueries org = (TaskTypesQueries) ((OwningOrganization) ou).organization().get();

            List<Project> projects = new ArrayList<Project>( );
            QueryBuilder<Project> builder = org.possibleProjects( typedTask.taskType().get() );
            Query<Project> query = builder.newQuery( uowf.currentUnitOfWork() );
            for (Project project : query)
            {
               projects.add( project );
            }

            return projects;
         } else
         {
            return Collections.emptyList();
         }
      }

      public List<User> possibleUsers()
      {
         Owner owner = ownable.owner().get();
         if (owner instanceof OrganizationParticipations)
         {
            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) owner;

            List<User> users = new ArrayList<User>( );

            for (Organization organization : orgs.organizations())
            {
               addUsers( users, organization );
            }

            return users;
         } else if (owner instanceof OwningOrganizationalUnit.Data)
         {
            OwningOrganizationalUnit.Data ouOwner = (OwningOrganizationalUnit.Data) owner;
            OrganizationalUnit ou = ouOwner.organizationalUnit().get();
            Organization org = ((OwningOrganization) ou).organization().get();

            List<User> users = new ArrayList<User>( );

            addUsers(users, org);

            return users;
         } else
         {
            return Collections.emptyList();
         }
      }

      private void addUsers( List<User> lvb, Organization organization )
      {
         OrganizationParticipations.Data userOrgs = QueryExpressions.templateFor(OrganizationParticipations.Data.class);
         Query<User> query = qbf.newQueryBuilder( User.class ).where( QueryExpressions.contains(userOrgs.organizations(), organization )).newQuery( uowf.currentUnitOfWork() );

         for (User user : query)
         {
            lvb.add( user );
         }
      }
   }
}
