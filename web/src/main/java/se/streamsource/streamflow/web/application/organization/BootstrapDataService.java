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

package se.streamsource.streamflow.web.application.organization;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.web.domain.Specification;
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
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.role.PermissionsEnum;
import se.streamsource.streamflow.web.domain.structure.role.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      public void activate() throws Exception
      {
         UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "Bootstrap data" ) );
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

               ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder( ContactValue.class );
               contactBuilder.prototype().name().set( "Administrator" );
               ContactValue contact = contactBuilder.newInstance();
               admin.updateContact( contact );
            }


            Query<OrganizationEntity> orgs = organizations.organizations().newQuery( uow );

            if (orgs.count() == 0)
            {
               // Create default organization
               Organization ou = organizations.createOrganization( "Organization" );
               uow.complete();
               uow = uowf.newUnitOfWork( newUsecase( "Bootstrap data" ) );
               orgs = uow.get( organizations ).organizations().newQuery( uow );
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

               }, new Specification<Class>()
               {
                  public boolean valid( Class instance )
                  {
                     return true;
                  }
               });
            }

            uow.complete();

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