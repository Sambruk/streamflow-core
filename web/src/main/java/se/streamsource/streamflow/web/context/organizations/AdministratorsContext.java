/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.domain.structure.role.Role;
import se.streamsource.streamflow.web.domain.structure.role.Roles;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.dci.infrastructure.web.context.Context;
import se.streamsource.streamflow.dci.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.dci.infrastructure.web.context.SubContexts;

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
   public LinksValue possibleusers( StringDTO query );
   public LinksValue possiblegroups( StringDTO query );

   abstract class Mixin
      extends ContextMixin
      implements AdministratorsContext
   {
      @Structure
      Module module;

      public LinksValue administrators()
      {
         RolePolicy.Data policy = context.role(RolePolicy.Data.class );

         OwningOrganization org = context.role(OwningOrganization.class);
         Roles organization = org.organization().get();
         Role adminRole = organization.getAdministratorRole();

         return new LinksBuilder(module.valueBuilderFactory()).rel( "administrator" ).addDescribables( policy.participantsWithRole(adminRole )).newLinks();
      }

      public void addadministrator( EntityReferenceDTO participantId )
      {
         UnitOfWork unitOfWork = module.unitOfWorkFactory().currentUnitOfWork();
         Participant participant = unitOfWork.get( Participant.class, participantId.entity().get().identity() );
         RolePolicy role = context.role( RolePolicy.class );

         OwningOrganization org = ((OwningOrganization)role);
         OrganizationEntity organization = (OrganizationEntity) org.organization().get();
         Role adminRole = organization.getAdministratorRole();

         role.grantRole( participant, adminRole );
      }

      public LinksValue possibleusers( StringDTO query )
      {
         OrganizationQueries organization = context.role(OrganizationQueries.class);

         Role adminRole = context.role( Roles.class).getAdministratorRole();

         Query<UserEntity> users = organization.findUsersByUsername( query.string().get() ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         users = users.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.command("addadministrator");

         RolePolicy.Data policy = context.role(RolePolicy.Data.class);

         for (UserEntity user : users)
         {
            if (!policy.participantHasRole(user, adminRole))
            {
               linksBuilder.addDescribable(user);
            }
         }

         return linksBuilder.newLinks();
      }


      public LinksValue possiblegroups( StringDTO query )
      {
         OrganizationQueries organization = context.role(OrganizationQueries.class);

         Query<GroupEntity> groups = organization.findGroupsByName( query.string().get() ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         groups.orderBy( orderBy( templateFor( Describable.Data.class ).description() ) );

         Role adminRole = context.role( Roles.class).getAdministratorRole();

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.command("addadministrator");

         RolePolicy.Data policy = context.role(RolePolicy.Data.class);

         for (GroupEntity grp : groups)
         {
            if (!policy.participantHasRole( grp, adminRole ))
            {
               linksBuilder.addDescribable( grp );
            }
         }

         return linksBuilder.newLinks();
      }

      public AdministratorContext context( String id )
      {
         Participant participant = module.unitOfWorkFactory().currentUnitOfWork().get( Participant.class, id );
         context.playRoles( participant, Participant.class);

         if(!context.role( RolePolicy.Data.class ).hasRoles( participant ))
         {
            throw new IllegalArgumentException(id+" is not an administrator");
         }

         return subContext( AdministratorContext.class );
      }
   }
}
