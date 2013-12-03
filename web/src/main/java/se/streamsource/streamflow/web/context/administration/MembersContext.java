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

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

/**
 * JAVADOC
 */
public class MembersContext
   implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      Members.Data members = role(Members.Data.class);

      return new LinksBuilder( module.valueBuilderFactory() ).rel( "member" ).addDescribables( members.members() ).newLinks();
   }

   public void addmember( EntityValue memberId)
   {
      UnitOfWork unitOfWork = module.unitOfWorkFactory().currentUnitOfWork();
      Member member = unitOfWork.get( Member.class, memberId.entity().get() );

      Members members = role(Members.class);

      members.addMember( member );
   }

   public LinksValue possibleusers()
   {
      OwningOrganization org = role(OwningOrganization.class);
      OrganizationEntity organization = (OrganizationEntity) org.organization().get();
      Members.Data members = role(Members.Data.class);

      Query<UserEntity> users = organization.findUsersByUsername( "*" ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
      users = users.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
      linksBuilder.command("addmember");

      for (UserEntity user : users)
      {
         if (!members.members().contains( user ))
         {
            String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
            linksBuilder.addDescribable( user, group );
         }
      }

      return linksBuilder.newLinks();
   }

   public LinksValue possiblegroups()
   {
      OrganizationQueries org = role(OrganizationQueries.class);

      final Members.Data members = role(Members.Data.class);

      final LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
      linksBuilder.command("addmember");

      org.visitOrganization( new OrganizationVisitor()
      {
         @Override
         public boolean visitGroup( Group grp )
         {
            if (!members.members().contains( (Member) grp )
                  && !members.equals(grp))
            {
               String group = "" + Character.toUpperCase( grp.getDescription().charAt( 0 ) );
               linksBuilder.addDescribable( grp, group );
            }

            return true;
         }
      }, new OrganizationQueries.ClassSpecification( Organization.class, OrganizationalUnits.class, Groups.class));

      return linksBuilder.newLinks();
   }
}
