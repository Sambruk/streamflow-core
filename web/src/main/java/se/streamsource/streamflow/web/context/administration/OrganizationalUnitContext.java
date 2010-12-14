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

package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.domain.organization.MergeOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.MoveOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.OpenProjectExistsException;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;

/**
 * JAVADOC
 */
public class OrganizationalUnitContext
      implements DeleteContext
{
   @Structure
   ValueBuilderFactory vbf;
   
   @Structure
   UnitOfWorkFactory uowf;

   public LinksValue possiblemoveto()
   {
      final OrganizationalUnit thisUnit = RoleMap.role( OrganizationalUnit.class );

      final LinksBuilder links = new LinksBuilder(vbf);
      links.command( "move" );
      OrganizationQueries queries = RoleMap.role(OrganizationQueries.class);
      queries.visitOrganization( new OrganizationVisitor()
      {
         @Override
         public boolean visitOrganization( Organization org )
         {
            links.addDescribable( org );

            return super.visitOrganization( org );
         }

         

         @Override
         public boolean visitOrganizationalUnit( OrganizationalUnit ou )
         {
            if (!ou.equals(thisUnit))
               links.addDescribable( ou );

            return true;
         }
      }, new OrganizationQueries.ClassSpecification( Organization.class, OrganizationalUnits.class, OrganizationalUnit.class));

      return links.newLinks();
   }

   public void move( EntityValue moveValue ) throws ResourceException
   {
      OrganizationalUnitRefactoring ou = RoleMap.role( OrganizationalUnitRefactoring.class );
      OrganizationalUnits toEntity = uowf.currentUnitOfWork().get( OrganizationalUnits.class, moveValue.entity().get() );

      try
      {
         ou.moveOrganizationalUnit( toEntity );
      } catch (MoveOrganizationalUnitException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
      }
   }

   public LinksValue possiblemergewith()
   {
      final OrganizationalUnit thisUnit = RoleMap.role( OrganizationalUnit.class );

      final LinksBuilder links = new LinksBuilder(vbf);
      links.command( "merge" );
      OrganizationQueries queries = RoleMap.role(OrganizationQueries.class);
      queries.visitOrganization( new OrganizationVisitor()
      {
         @Override
         public boolean visitOrganizationalUnit( OrganizationalUnit ou )
         {
            if (!ou.equals(thisUnit))
               links.addDescribable( ou );

            return true;
         }
      }, new OrganizationQueries.ClassSpecification( OrganizationalUnits.class, OrganizationalUnit.class));

      return links.newLinks();
   }

   public void merge( EntityValue moveValue ) throws ResourceException
   {
      OrganizationalUnitRefactoring ou = RoleMap.role( OrganizationalUnitRefactoring.class );
      OrganizationalUnit toEntity = uowf.currentUnitOfWork().get( OrganizationalUnit.class, moveValue.entity().get() );

      try
      {
         ou.mergeOrganizationalUnit( toEntity );
      } catch (MergeOrganizationalUnitException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
      }
   }

   public void delete() throws ResourceException
   {
      OrganizationalUnitRefactoring ou = RoleMap.role( OrganizationalUnitRefactoring.class );

      try
      {
         ou.deleteOrganizationalUnit();

      } catch (OpenProjectExistsException pe)
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, pe.getMessage() );
      }
   }
}
