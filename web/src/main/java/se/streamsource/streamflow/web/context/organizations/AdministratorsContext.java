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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
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
import se.streamsource.dci.api.SubContexts;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(AdministratorsContext.Mixin.class)
public interface AdministratorsContext
   extends SubContexts<AdministratorContext>, Context
{
   public LinksValue administrators();

   public void addadministrator( EntityReferenceDTO participantId );
   public LinksValue possibleusers();
   public LinksValue possiblegroups();

   abstract class Mixin
      extends ContextMixin
      implements AdministratorsContext
   {
      @Structure
      Module module;

      public LinksValue administrators()
      {
         RolePolicy policy = roleMap.get(RolePolicy.class );

         OwningOrganization org = roleMap.get(OwningOrganization.class);
         Roles organization = org.organization().get();
         Role adminRole = organization.getAdministratorRole();

         return new LinksBuilder(module.valueBuilderFactory()).rel( "administrator" ).addDescribables( policy.participantsWithRole(adminRole )).newLinks();
      }

      public void addadministrator( EntityReferenceDTO participantId )
      {
         UnitOfWork unitOfWork = module.unitOfWorkFactory().currentUnitOfWork();
         Participant participant = unitOfWork.get( Participant.class, participantId.entity().get().identity() );
         RolePolicy role = roleMap.get( RolePolicy.class );

         OwningOrganization org = ((OwningOrganization)role);
         OrganizationEntity organization = (OrganizationEntity) org.organization().get();
         Role adminRole = organization.getAdministratorRole();

         role.grantRole( participant, adminRole );
      }

      public LinksValue possibleusers()
      {
         OrganizationQueries organization = roleMap.get(OrganizationQueries.class);

         Role adminRole = roleMap.get( Roles.class).getAdministratorRole();

         Query<UserEntity> users = organization.findUsersByUsername( "*" ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         users = users.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command("addadministrator");

         RolePolicy policy = roleMap.get(RolePolicy.class);

         for (UserEntity user : users)
         {
            if (!policy.participantHasRole(user, adminRole))
            {
               String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
               linksBuilder.addDescribable(user, group);
            }
         }

         return linksBuilder.newLinks();
      }

      public LinksValue possiblegroups()
      {
         final LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command("addadministrator");

         final Role adminRole = roleMap.get( Roles.class).getAdministratorRole();
         final RolePolicy policy = roleMap.get(RolePolicy.class);

         OrganizationQueries organization = roleMap.get(OrganizationQueries.class);

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
         }, new OrganizationQueries.ClassSpecification( OrganizationalUnits.class, Groups.class));

         return linksBuilder.newLinks();
      }

      public AdministratorContext context( String id )
      {
         Participant participant = module.unitOfWorkFactory().currentUnitOfWork().get( Participant.class, id );
         roleMap.set( participant, Participant.class);

         if(!roleMap.get( RolePolicy.class ).hasRoles( participant ))
         {
            throw new IllegalArgumentException(id+" is not an administrator");
         }

         return subContext( AdministratorContext.class );
      }
   }
}
