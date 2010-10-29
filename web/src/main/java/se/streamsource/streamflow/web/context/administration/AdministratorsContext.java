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
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.domain.structure.role.Role;
import se.streamsource.streamflow.web.domain.structure.role.Roles;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
public class AdministratorsContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      RolePolicy policy = RoleMap.role( RolePolicy.class );

      OwningOrganization org = RoleMap.role( OwningOrganization.class );
      Roles organization = org.organization().get();
      Role adminRole = organization.getAdministratorRole();

      return new LinksBuilder( module.valueBuilderFactory() ).rel( "administrator" ).addDescribables( policy.participantsWithRole( adminRole ) ).newLinks();
   }

   public void addadministrator( EntityValue participantId )
   {
      UnitOfWork unitOfWork = module.unitOfWorkFactory().currentUnitOfWork();
      Participant participant = unitOfWork.get( Participant.class, participantId.entity().get() );
      RolePolicy rolePolicy = RoleMap.role( RolePolicy.class );

      OwningOrganization org = ((OwningOrganization) rolePolicy);
      OrganizationEntity organization = (OrganizationEntity) org.organization().get();
      Role adminRole = organization.getAdministratorRole();

      rolePolicy.grantRole( participant, adminRole );
   }

   public LinksValue possibleusers()
   {
      OrganizationQueries organization = RoleMap.role( OrganizationQueries.class );

      Role adminRole = RoleMap.role( Roles.class ).getAdministratorRole();

      Query<UserEntity> users = organization.findUsersByUsername( "*" ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
      users = users.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "addadministrator" );

      RolePolicy policy = RoleMap.role( RolePolicy.class );

      for (UserEntity user : users)
      {
         if (!policy.participantHasRole( user, adminRole ))
         {
            String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
            linksBuilder.addDescribable( user, group );
         }
      }

      return linksBuilder.newLinks();
   }

   public LinksValue possiblegroups()
   {
      final LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "addadministrator" );

      final Role adminRole = RoleMap.role( Roles.class ).getAdministratorRole();
      final RolePolicy policy = RoleMap.role( RolePolicy.class );

      OrganizationQueries organization = RoleMap.role( OrganizationQueries.class );

      organization.visitOrganization( new OrganizationVisitor()
      {
         @Override
         public boolean visitGroup( Group grp )
         {
            if (!policy.participantHasRole( grp, adminRole ))
            {
               String group = "" + Character.toUpperCase( grp.getDescription().charAt( 0 ) );
               linksBuilder.addDescribable( grp, group );
            }

            return true;
         }
      }, new OrganizationQueries.ClassSpecification( OrganizationalUnits.class, Groups.class ) );

      return linksBuilder.newLinks();
   }
}
