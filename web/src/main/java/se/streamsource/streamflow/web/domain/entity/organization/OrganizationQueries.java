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

package se.streamsource.streamflow.web.domain.entity.organization;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.util.Specification;
import org.qi4j.spi.structure.ModuleSPI;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(OrganizationQueries.Mixin.class)
public interface OrganizationQueries
{
   public QueryBuilder<UserEntity> findUsersByUsername( String query );

   public void visitOrganization(OrganizationVisitor visitor, Specification<Class> typeSelector);

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

      public void visitOrganization( OrganizationVisitor visitor, Specification<Class> typeSpecification)
      {
         if (typeSpecification.test( Organization.class ) && !visitor.visitOrganization( org ))
            return;

         // Visit items on Organization
         if (typeSpecification.test( Labels.class ))
            for (Label label : ((Labels.Data) org).labels())
            {
               if (!visitor.visitLabel( label ))
                  return;
            }

         if (typeSpecification.test( Forms.class ))
            for (Form form : ((Forms.Data)org).forms())
            {
               if (!visitor.visitForm( form ))
                  return;
            }

         if (typeSpecification.test( CaseTypes.class ))
            for (CaseType caseType : ((CaseTypes.Data)org).caseTypes())
            {
               if (!visitCaseType( caseType, visitor, typeSpecification ))
                  return;
            }

         if (typeSpecification.test( OrganizationalUnits.class ))
            for (OrganizationalUnit organizationalUnit : ((OrganizationalUnits.Data) org).organizationalUnits())
            {
               if (!visitOu(organizationalUnit, visitor, typeSpecification))
                  return;
            }
      }

      private boolean visitOu( OrganizationalUnit organizationalUnit, OrganizationVisitor visitor, Specification<Class> typeSpecification )
      {
         if (typeSpecification.test( OrganizationalUnit.class ) && !visitor.visitOrganizationalUnit( organizationalUnit ))
            return false;

         if (typeSpecification.test( Labels.class ))
            for (Label label : ((Labels.Data) organizationalUnit).labels())
            {
               if (!visitor.visitLabel( label ))
                  return false;
            }

         if (typeSpecification.test( Forms.class ))
            for (Form form : ((Forms.Data)organizationalUnit).forms())
            {
               if (!visitor.visitForm( form ))
                  return false;
            }

         if (typeSpecification.test( CaseTypes.class ))
            for (CaseType caseType : ((CaseTypes.Data)organizationalUnit).caseTypes())
            {
               if (!visitCaseType( caseType, visitor, typeSpecification ))
                  return false;
            }

         if (typeSpecification.test( Groups.class ))
            for (Group group : ((Groups.Data) organizationalUnit).groups())
            {
               if (!visitor.visitGroup( group ))
                  return false;
            }

         if (typeSpecification.test( Projects.class ))
            for (Project project : ((Projects.Data) organizationalUnit).projects())
            {
               if (!visitProject(project, visitor, typeSpecification))
                  return false;
            }

         for (OrganizationalUnit ou : ((OrganizationalUnits.Data) organizationalUnit).organizationalUnits())
         {
            if (!visitOu(ou, visitor, typeSpecification ))
               return false;
         }

         return true;
      }

      private boolean visitProject( Project project, OrganizationVisitor visitor, Specification<Class> typeSpecification)
      {
         if (typeSpecification.test( Project.class ))
            if (!visitor.visitProject( project ))
               return false;

         if (typeSpecification.test( Labels.class ))
            for (Label label : ((Labels.Data) project).labels())
            {
               if (!visitor.visitLabel( label ))
                  return false;
            }

         if (typeSpecification.test( Forms.class ))
            for (Form form : ((Forms.Data)project).forms())
            {
               if (!visitor.visitForm( form ))
                  return false;
            }

         if (typeSpecification.test( CaseTypes.class ))
            for (CaseType caseType : ((CaseTypes.Data)project).caseTypes())
            {
               if (!visitCaseType( caseType, visitor, typeSpecification ))
                  return false;
            }

         if (typeSpecification.test( SelectedCaseTypes.class ))
            for (CaseType caseType : ((SelectedCaseTypes.Data)project).selectedCaseTypes())
            {
               if (!visitor.visitSelectedCaseType( caseType ))
                  return false;
            }

         return true;
      }

      private boolean visitCaseType( CaseType caseType, OrganizationVisitor visitor, Specification<Class> typeSpecification )
      {
         if (typeSpecification.test( CaseType.class ) && !visitor.visitCaseType( caseType ))
            return false;

         if (typeSpecification.test( Labels.class ))
            for (Label label : ((Labels.Data) caseType).labels())
            {
               if (!visitor.visitLabel( label ))
                  return false;
            }

         if (typeSpecification.test( SelectedLabels.class ))
            for (Label label : ((SelectedLabels.Data) caseType).selectedLabels())
            {
               if (!visitor.visitSelectedLabel( label ))
                  return false;
            }

         if (typeSpecification.test( Forms.class ))
            for (Form form : ((Forms.Data)caseType).forms())
            {
               if (!visitor.visitForm( form ))
                  return false;
            }

         if (typeSpecification.test( SelectedForms.class ))
            for (Form form : ((SelectedForms.Data)caseType).selectedForms())
            {
               if (!visitor.visitSelectedForm( form ))
                  return false;
            }

         if (typeSpecification.test( Resolutions.class ))
            for (Resolution resolution : ((Resolutions.Data)caseType).resolutions())
            {
               if (!visitor.visitResolution( resolution ))
                  return false;
            }

         if (typeSpecification.test( SelectedResolutions.class ))
            for (Resolution resolution : ((SelectedResolutions.Data)caseType).selectedResolutions())
            {
               if (!visitor.visitSelectedResolution( resolution ))
                  return false;
            }

         return true;
      }
   }

   public static class ClassSpecification
      implements Specification<Class>
   {
      private Class[] classes;

      public ClassSpecification(Class... classes)
      {
         this.classes = classes;
      }

      public boolean test( Class instance )
      {
         for (int i = 0; i < classes.length; i++)
         {
            Class aClass = classes[i];
            if (aClass.equals(instance))
               return true;
         }
         return false;
      }
   }
}
