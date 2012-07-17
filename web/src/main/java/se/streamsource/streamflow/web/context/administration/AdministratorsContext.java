/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.domain.structure.role.Role;
import se.streamsource.streamflow.web.domain.structure.role.Roles;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
public class AdministratorsContext
      implements IndexContext<Iterable<Participant>>
{
   @Structure
   Module module;

   public Iterable<Participant> index()
   {
      RolePolicy policy = RoleMap.role( RolePolicy.class );

      OwningOrganization org = RoleMap.role( OwningOrganization.class );
      Roles organization = org.organization().get();
      Role adminRole = organization.getAdministratorRole();

      return policy.participantsWithRole(adminRole);
   }

   public void addadministrator( @Name("entity") Participant participant )
   {
      RolePolicy rolePolicy = RoleMap.role( RolePolicy.class );

      OwningOrganization org = ((OwningOrganization) rolePolicy);
      OrganizationEntity organization = (OrganizationEntity) org.organization().get();
      Role adminRole = organization.getAdministratorRole();

      rolePolicy.grantRole( participant, adminRole );
   }

   public Iterable<? extends User> possibleusers()
   {
      OrganizationQueries organization = RoleMap.role(OrganizationQueries.class);

      final Role adminRole = RoleMap.role(Roles.class).getAdministratorRole();

      final Query<? extends User> users = organization.
            findUsersByUsername( "*" ).
            newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).
            orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

      final RolePolicy policy = RoleMap.role( RolePolicy.class );

      return Iterables.filter(new Specification<User>()
      {
         public boolean satisfiedBy(User user)
         {
            return !policy.participantHasRole(user, adminRole);
         }
      }, users);
   }

   public Iterable<? extends Group> possiblegroups()
   {
      final Role adminRole = RoleMap.role( Roles.class ).getAdministratorRole();
      final RolePolicy policy = RoleMap.role( RolePolicy.class );

      OrganizationQueries organization = RoleMap.role( OrganizationQueries.class );

      final List<Group> groups = new ArrayList<Group>();
      organization.visitOrganization( new OrganizationVisitor()
      {
         @Override
         public boolean visitGroup( Group grp )
         {
            if (!policy.participantHasRole( grp, adminRole ))
            {
               groups.add(grp);
            }

            return true;
         }
      }, new OrganizationQueries.ClassSpecification( OrganizationalUnits.class, Groups.class ) );

      return groups;
   }
}
