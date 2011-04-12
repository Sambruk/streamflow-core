/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import org.restlet.data.*;
import org.restlet.resource.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.domain.organization.*;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.*;
import se.streamsource.streamflow.web.domain.structure.form.*;
import se.streamsource.streamflow.web.domain.structure.group.*;
import se.streamsource.streamflow.web.domain.structure.label.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.*;

/**
 * JAVADOC
 */
public class OrganizationalUnitContext
      implements DeleteContext, InteractionValidation
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

   @RequiresValid("delete")
   public void delete() throws ResourceException
   {
      OrganizationalUnitRefactoring ou = RoleMap.role( OrganizationalUnitRefactoring.class );

      ou.deleteOrganizationalUnit();
   }

   public boolean isValid( String name )
   {
      if (name.equals("delete"))
      {
         // OU has to be empty first
         return isEmpty(RoleMap.role(OrganizationalUnit.class));
      } else
         return false;
   }

   private boolean isEmpty( OrganizationalUnit organizationalUnit )
   {
      if (((Projects.Data)organizationalUnit).projects().count() > 0)
      {
         return false;
      }

      if (((Groups.Data)organizationalUnit).groups().count() > 0)
      {
         return false;
      }

      if (((Forms.Data)organizationalUnit).forms().count() > 0)
      {
         return false;
      }

      if (((Labels.Data)organizationalUnit).labels().count() > 0)
      {
         return false;
      }

      if (((OrganizationalUnits.Data)organizationalUnit).organizationalUnits().count() > 0)
      {
         return false;
      }

      return true;
   }
}
