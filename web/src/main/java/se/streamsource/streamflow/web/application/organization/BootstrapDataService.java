/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.application.organization;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.role.PermissionsEnum;
import se.streamsource.streamflow.web.domain.structure.role.Role;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Ensure that the most basic entities are always created. This includes:
 * 1) an UsersEntity
 * 2) an OrganizationsEntity
 * 3) an Organization
 * 4) an Administrator role
 * 5) an Administrator user
 */
@Mixins(BootstrapDataService.Mixin.class)
public interface BootstrapDataService
      extends ServiceComposite, Activatable
{
   class Mixin
         implements Activatable
   {
      final Logger logger = LoggerFactory.getLogger( getClass().getName() );
      @Structure
      Module module;

      public void activate() throws Exception
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(newUsecase("Bootstrap data"));
         RoleMap.newCurrentRoleMap();
         try
         {
            UsersEntity users;
            try
            {
               // Check if users entity exists
               users = uow.get( UsersEntity.class, UsersEntity.USERS_ID );
            } catch (NoSuchEntityException e)
            {
               // Create bootstrap data
               users = uow.newEntity( UsersEntity.class, UsersEntity.USERS_ID );
            }

            OrganizationsEntity organizations;
            try
            {
               // Check if organizations entity exists
               organizations = uow.get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
            } catch (NoSuchEntityException e)
            {
               // Create bootstrap data
               organizations = uow.newEntity( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
            }

            // Check if admin exists
            UserEntity admin;
            try
            {
               admin = uow.get( UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME );
            } catch (NoSuchEntityException e)
            {
               // Create admin
               admin = (UserEntity) users.createUser( UserEntity.ADMINISTRATOR_USERNAME, UserEntity.ADMINISTRATOR_USERNAME );

               ValueBuilder<ContactDTO> contactBuilder = module.valueBuilderFactory().newValueBuilder(ContactDTO.class);
               contactBuilder.prototype().name().set( "Administrator" );
               ContactDTO contact = contactBuilder.newInstance();
               admin.updateContact( contact );
            }


            Query<OrganizationEntity> orgs = organizations.organizations().newQuery( uow );

            long orgCount = orgs.count();
            if (orgCount == 0)
            {
               // Create default organization
               organizations.createOrganization( "Organization" );
               uow.complete();
               uow = module.unitOfWorkFactory().newUnitOfWork(newUsecase("Bootstrap data"));
               orgs = uow.get( organizations ).organizations().newQuery( uow );
               admin = uow.get(admin);
            } else if (orgCount == 1)
            {
               // Set association to the one org
               organizations.organization().set(orgs.find());
            } else if (orgCount > 1)
            {
               logger.warn("Multiple organizations exist. Removing extra");
               OrganizationEntity realOrg = null;
               for (OrganizationEntity organizationEntity : orgs)
               {
                  if (!organizationEntity.getDescription().equals("Organization"))
                  {
                     if (realOrg != null)
                        logger.warn("Multiple organizations have changed name. Cannot determine which one to remove!");
                     realOrg = organizationEntity;
                  }
               }

               // Set association to the one org
               organizations.organization().set(realOrg);

               // Now remove all extra orgs
               for (OrganizationEntity org : orgs)
               {
                  if (!org.equals(realOrg))
                  {
                     logger.warn("Removing extra organization with id:"+org);
                     org.removeEntity();
                  }
               }

               uow.complete();
               uow = module.unitOfWorkFactory().newUnitOfWork(newUsecase("Bootstrap data"));
               orgs = uow.get(organizations).organizations().newQuery( uow );
               admin = uow.get(admin);
            }

            for (OrganizationEntity org : orgs)
            {
               Role administrator;
               if (org.roles().count() == 0)
               {
                  // Administrator role
                  administrator = org.createRole( "Administrator" );
               } else
               {
                  administrator = org.roles().get( 0 );
               }

               administrator.addPermission( PermissionsEnum.administrator.name() );

               // Administrator should be member of all organizations
               if (!admin.organizations().contains( org ))
               {
                  admin.join( org );
               }

               // Assign admin role to administrator
               org.grantRole( admin, administrator );

               // Check that ownership is correctly set
               org.visitOrganization( new OrganizationVisitor()
               {
                  @Override
                  public boolean visitOrganization( Organization org )
                  {
                     OrganizationEntity organization = (OrganizationEntity) org;

                     fixOwnership(organization, organization.organizationalUnits());
                     fixOwnership(organization, organization.forms());
                     fixOwnership(organization, organization.roles());
                     fixOwnership(organization, organization.caseTypes());

                     return true;
                  }

                  @Override
                  public boolean visitOrganizationalUnit( OrganizationalUnit ou )
                  {
                     OrganizationalUnitEntity orgUnit = (OrganizationalUnitEntity) ou;

                     fixOwnership(orgUnit, orgUnit.organizationalUnits());
                     fixOwnership(orgUnit, orgUnit.forms());
                     fixOwnership(orgUnit, orgUnit.groups());
                     fixOwnership(orgUnit, orgUnit.projects());
                     fixOwnership(orgUnit, orgUnit.caseTypes());

                     return true;
                  }

                  @Override
                  public boolean visitProject( Project project )
                  {
                     ProjectEntity proj = (ProjectEntity) project;

                     fixOwnership(proj, proj.forms());
                     fixOwnership(proj, proj.caseTypes());

                     return true;
                  }

                  @Override
                  public boolean visitCaseType( CaseType caseType )
                  {
                     CaseTypeEntity ct = (CaseTypeEntity) caseType;

                     fixOwnership(ct, ct.forms());

                     return true;
                  }

                  private void fixOwnership( Owner owner, ManyAssociation<? extends Ownable> ownables )
                  {
                     for (Ownable ownable : ownables)
                     {
                        fixOwner( ownable, owner );
                     }
                  }

                  private void fixOwner( Ownable ownable, Owner owner )
                  {
                     if (!ownable.isOwnedBy( owner ))
                     {
                        logger.info( "Changed owner of "+ownable+" to "+owner );
                        ownable.changeOwner( owner );
                     }
                  }

               }, Specifications.<Class>TRUE());
            }

            uow.complete();
            logger.info( "Bootstrap of domain model complete" );

         } catch (Exception e)
         {
            logger.warn( "BootstrapDataService failed to start!" );
            e.printStackTrace();
            uow.discard();
         }
      }

      public void passivate() throws Exception
      {
      }
   }
}