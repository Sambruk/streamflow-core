/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.context.administration;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.UpdateContext;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.organization.FormOnRemove;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * The context representing the mandatory form to be filled out on removing a case.
 */
public class FormOnRemoveContext
   implements IndexContext<LinkValue>, UpdateContext<String>
{
   @Structure
   Module module;

   @Uses
   FormOnRemove formOnRemove;

   @Uses
   FormOnRemove.Data formOnRemoveData;

   public LinkValue index()
   {
      if( formOnRemoveData.formOnRemove().get() != null )
      {
      ValueBuilder<LinkValue> builder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
      builder.prototype().href().set("update?entity=null");
      builder.prototype().id().set(  formOnRemoveData.formOnRemove().get().toString());
      builder.prototype().text().set(formOnRemoveData.formOnRemove().get().getDescription());
      return builder.newInstance();
      } else
         return null;

   }

   public void update( @Name( "entity" ) String value )
   {
      formOnRemove.changeFormOnRemove( "null".equals( value ) ? null
            : module.unitOfWorkFactory().currentUnitOfWork().get( Form.class, value ) );
   }

   public LinksValue possibleforms()
   {
      OrganizationQueries organizationQueries = role( OrganizationQueries.class );

      final LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "update" );

      organizationQueries.visitOrganization( new OrganizationVisitor()
      {

         Describable owner;

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
            owner = caseType;

            return super.visitCaseType( caseType );
         }

         @Override
         public boolean visitForm( Form form )
         {

            if( !form.equals( formOnRemoveData.formOnRemove().get() ))
               builder.addDescribable( form, owner );

            return true;
         }

      }, new OrganizationQueries.ClassSpecification(
            Organization.class,
            OrganizationalUnits.class,
            OrganizationalUnit.class,
            Projects.class,
            Project.class,
            CaseTypes.class,
            CaseType.class,
            Forms.class ) );

      return builder.newLinks();
   }
}
