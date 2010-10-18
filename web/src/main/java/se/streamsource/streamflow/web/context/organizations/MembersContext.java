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

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(MembersContext.Mixin.class)
public interface MembersContext
   extends SubContexts<MemberContext>, IndexContext<LinksValue>, Context
{
   public void addmember( EntityValue memberId);

   public LinksValue possibleusers();

   public LinksValue possiblegroups();

   abstract class Mixin
      extends ContextMixin
      implements MembersContext
   {
      public LinksValue index()
      {
         Members.Data members = roleMap.get(Members.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "member" ).addDescribables( members.members() ).newLinks();
      }

      public void addmember( EntityValue memberId)
      {
         UnitOfWork unitOfWork = module.unitOfWorkFactory().currentUnitOfWork();
         Member member = unitOfWork.get( Member.class, memberId.entity().get() );

         Members members = roleMap.get(Members.class);

         members.addMember( member );
      }

      public LinksValue possibleusers()
      {
         OwningOrganization org = roleMap.get(OwningOrganization.class);
         OrganizationEntity organization = (OrganizationEntity) org.organization().get();
         Members.Data members = roleMap.get(Members.Data.class);

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
         OrganizationQueries org = roleMap.get(OrganizationQueries.class);

         final Members.Data members = roleMap.get(Members.Data.class);

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
         }, new OrganizationQueries.ClassSpecification( OrganizationalUnits.class, Groups.class));

         return linksBuilder.newLinks();
      }

      public MemberContext context( String id )
      {
         roleMap.set( module.unitOfWorkFactory().currentUnitOfWork().get(Member.class, id ));
         return subContext( MemberContext.class );
      }
   }
}
