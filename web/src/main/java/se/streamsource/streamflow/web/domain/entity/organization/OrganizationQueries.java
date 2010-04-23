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

package se.streamsource.streamflow.web.domain.entity.organization;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.spi.structure.ModuleSPI;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.Projects;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(OrganizationQueries.Mixin.class)
public interface OrganizationQueries
{
   public QueryBuilder<UserEntity> findUsersByUsername( String query );

   public QueryBuilder<GroupEntity> findGroupsByName( String query );

   public Query<ProjectEntity> findProjects( String query );

   public void visitOrganization(OrganizationVisitor visitor);

   class Mixin
         implements OrganizationQueries
   {

      @Structure
      ModuleSPI module;

      @This
      Organization org;

      public QueryBuilder<UserEntity> findUsersByUsername( String userName )
      {
         QueryBuilder<UserEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( UserEntity.class );
         queryBuilder = queryBuilder.where(
               and(
                     eq( templateFor( UserAuthentication.Data.class ).disabled(), false ),
                     contains( templateFor( UserEntity.class ).organizations(), org ) )
         );

         if (userName.length() > 0)
         {
            queryBuilder = queryBuilder.where(
                  matches( templateFor( UserEntity.class ).userName(), "^" + userName )
            );
         }

         return queryBuilder;
      }

      public QueryBuilder<GroupEntity> findGroupsByName( String query )
      {
         // TODO Ensure that the group is a member of this organization somehow
         QueryBuilder<GroupEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( GroupEntity.class );
         queryBuilder = queryBuilder.where(
               eq( templateFor( Removable.Data.class ).removed(), false ) );

         if (query.length() > 0)
         {
            queryBuilder = queryBuilder.where(
                  matches( templateFor( Describable.Data.class ).description(), "^" + query )
            );
         }

         return queryBuilder;
      }

      public Query<ProjectEntity> findProjects( String query )
      {
         final List<ProjectEntity> projects = new ArrayList<ProjectEntity>( );

         visitOrganization( new OrganizationVisitor()
         {
            @Override
            boolean visitProject( Project project )
            {
               projects.add( (ProjectEntity) project );

               return true;
            }
         });

         QueryBuilder<ProjectEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( ProjectEntity.class );
         queryBuilder = queryBuilder.where(
               and(
                  eq( templateFor( Removable.Data.class ).removed(), false ),
                  eq( templateFor( OwningOrganization.class,
                                   templateFor( OwningOrganizationalUnit.Data.class).organizationalUnit().get()).organization(), org)));

         if (query.length() > 0)
         {
            queryBuilder = queryBuilder.where(
                  matches( templateFor( Describable.Data.class ).description(), "^" + query )
            );
         }

         Query<ProjectEntity> projectsQuery = queryBuilder.newQuery( projects );
         projectsQuery.orderBy( orderBy( templateFor( Describable.Data.class ).description() ) );
         return projectsQuery;
      }

      public void visitOrganization( OrganizationVisitor visitor )
      {
         if (!visitor.visitOrganization( org ))
            return;

         // Visit items on Organization
         for (Label label : ((Labels.Data) org).labels())
         {
            if (!visitor.visitLabel( label ))
               return;
         }

         for (Form form : ((Forms.Data)org).forms())
         {
            if (!visitor.visitForm( form ))
               return;
         }

         for (CaseType caseType : ((CaseTypes.Data)org).caseTypes())
         {
            if (!visitor.visitCaseType( caseType ))
               return;
         }

         for (OrganizationalUnit organizationalUnit : ((OrganizationalUnits.Data) org).organizationalUnits())
         {
            if (!visitOu(organizationalUnit, visitor))
               return;
         }
      }

      private boolean visitOu( OrganizationalUnit organizationalUnit, OrganizationVisitor visitor )
      {
         if (!visitor.visitOrganizationalUnit( organizationalUnit ))
            return false;

         for (Label label : ((Labels.Data) organizationalUnit).labels())
         {
            if (!visitor.visitLabel( label ))
               return false;
         }

         for (Form form : ((Forms.Data)organizationalUnit).forms())
         {
            if (!visitor.visitForm( form ))
               return false;
         }

         for (CaseType caseType : ((CaseTypes.Data)organizationalUnit).caseTypes())
         {
            if (!visitor.visitCaseType( caseType ))
               return false;
         }

         for (Group group : ((Groups.Data) organizationalUnit).groups())
         {
            if (!visitor.visitGroup( group ))
               return false;
         }

         for (Project project : ((Projects.Data) organizationalUnit).projects())
         {
            if (!visitProject(project, visitor))
               return false;
         }

         for (OrganizationalUnit ou : ((OrganizationalUnits.Data) organizationalUnit).organizationalUnits())
         {
            if (!visitOu(ou, visitor))
               return false;
         }

         return true;
      }

      private boolean visitProject( Project project, OrganizationVisitor visitor )
      {
         if (!visitor.visitProject( project ))
            return false;

         for (Label label : ((Labels.Data) project).labels())
         {
            if (!visitor.visitLabel( label ))
               return false;
         }

         for (Form form : ((Forms.Data)project).forms())
         {
            if (!visitor.visitForm( form ))
               return false;
         }

         for (CaseType caseType : ((CaseTypes.Data)project).caseTypes())
         {
            if (!visitor.visitCaseType( caseType ))
               return false;
         }

         return true;
      }
   }
}
