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

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * JAVADOC
 */
@Mixins(SelectedFormsContext.Mixin.class)
public interface SelectedFormsContext
   extends SubContexts<SelectedFormContext>, IndexContext<LinksValue>, Context
{
   public LinksValue possibleforms();

   public void addform( EntityValue caseTypeDTO );

   abstract class Mixin
      extends ContextMixin
      implements SelectedFormsContext
   {
      public LinksValue index()
      {
         SelectedForms.Data forms = roleMap.get(SelectedForms.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel("selectedform").addDescribables( forms.selectedForms() ).newLinks();
      }

      public LinksValue possibleforms()
      {
         OrganizationQueries organizationQueries = roleMap.get(OrganizationQueries.class);

         final SelectedForms.Data selectedForms = roleMap.get(SelectedForms.Data.class);

         final LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "addform" );

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
               if (!selectedForms.selectedForms().contains( form ))
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
               Forms.class));

         return builder.newLinks();
      }

      public void addform( EntityValue formDTO )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         SelectedForms selectedForms = roleMap.get(SelectedForms.class);
         Form form = uow.get( Form.class, formDTO.entity().get() );

         selectedForms.addSelectedForm( form );
      }

      public SelectedFormContext context( String id )
      {
         roleMap.set( module.unitOfWorkFactory().currentUnitOfWork().get( Form.class, id ));
         return subContext( SelectedFormContext.class );
      }
   }
}