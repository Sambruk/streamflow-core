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

package se.streamsource.streamflow.web.domain.entity.caze;

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
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypesQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Projects;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JAVADOC
 */

@Mixins(CaseTypeQueries.Mixin.class)
public interface CaseTypeQueries
{
   void possibleCaseTypes( LinksBuilder builder);

   List<Project> possibleProjects();

   List<User> possibleUsers();

   class Mixin
         implements CaseTypeQueries
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      CreatedOn created; 

      @This
      Ownable.Data ownable;

      @This
      TypedCase.Data typedCase;

      @This
      Assignable assignable;

      public void possibleCaseTypes( final LinksBuilder builder)
      {
         Owner owner = ownable.owner().get();
         if (owner == null)
         {
            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) created.createdBy().get();

            for (final Organization organization : orgs.organizations())
            {
               OrganizationQueries orgQueries = (OrganizationQueries) organization;

               // Find out what case-types have been selected
               final Set<CaseType> selectedCaseTypes = new HashSet<CaseType>( );
               orgQueries.visitOrganization( new OrganizationVisitor()
               {
                  @Override
                  public boolean visitSelectedCaseType( CaseType caseType )
                  {
                     selectedCaseTypes.add( caseType );

                     return super.visitSelectedCaseType( caseType );
                  }
               }, new OrganizationQueries.ClassSpecification(
                     OrganizationalUnits.class,
                     OrganizationalUnit.class,
                     Projects.class,
                     SelectedCaseTypes.class
                     ));

               orgQueries.visitOrganization( new OrganizationVisitor()
               {
                  Describable owner;
                  StringBuilder string = new StringBuilder();

                  @Override
                  public boolean visitOrganization( Organization org )
                  {
                     owner = org;
                     return super.visitOrganization( org );
                  }

                  @Override
                  public boolean visitOrganizationalUnit( OrganizationalUnit ou )
                  {
                     owner = ou;
                     return super.visitOrganizationalUnit( ou );
                  }

                  @Override
                  public boolean visitProject( Project project )
                  {
                     owner = project;
                     return super.visitProject( project );
                  }

                  @Override
                  public boolean visitCaseType( CaseType caseType )
                  {
                     if (selectedCaseTypes.contains( caseType ))
                     {
                        // Build up list of labels as classes
                        string.setLength( 0 );
                        SelectedLabels.Data selectedLabels = (SelectedLabels.Data) caseType;
                        for (Label label : selectedLabels.selectedLabels())
                        {
                           string.append( label.getDescription() ).append( ' ' );
                        }

                        builder.addDescribable( caseType,  owner.getDescription(), string.toString() );
                     }

                     return super.visitCaseType( caseType );
                  }
               }, new OrganizationQueries.ClassSpecification(
                     Organization.class,
                     OrganizationalUnits.class,
                     OrganizationalUnit.class,
                     Projects.class,
                     Project.class,
                     CaseTypes.class,
                     CaseType.class
                     ));
            }
         } else
         {
            // Show only Case types from owning project
            SelectedCaseTypes.Data selectedCaseTypes = (SelectedCaseTypes.Data) owner;
            Describable describableOwner = (Describable) owner;
            StringBuilder string = new StringBuilder();
            for (CaseType caseType : selectedCaseTypes.selectedCaseTypes())
            {
               // Build up list of labels as classes
               string.setLength( 0 );
               SelectedLabels.Data selectedLabels = (SelectedLabels.Data) caseType;
               for (Label label : selectedLabels.selectedLabels())
               {
                  string.append( label.getDescription() ).append( ' ' );
               }

               builder.addDescribable( caseType, describableOwner.getDescription(), string.toString() );
            }
         }
      }

      public List<Project> possibleProjects()
      {
         Owner owner = ownable.owner().get();

         if (owner == null)
         {
            Creator creator = created.createdBy().get();

            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) creator;

            List<Project> projects = new ArrayList<Project>( );

            for (Organization organization : orgs.organizations())
            {
               CaseTypesQueries caseTypesQueries = (CaseTypesQueries) organization;
               QueryBuilder<Project> builder = caseTypesQueries.possibleProjects( typedCase.caseType().get() );
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
            CaseTypesQueries org = (CaseTypesQueries) ((OwningOrganization) ou).organization().get();

            List<Project> projects = new ArrayList<Project>( );
            QueryBuilder<Project> builder = org.possibleProjects( typedCase.caseType().get() );
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
