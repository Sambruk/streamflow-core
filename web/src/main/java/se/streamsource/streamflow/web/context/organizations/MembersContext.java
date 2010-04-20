/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
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
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.*;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.dci.api.SubContexts;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(MembersContext.Mixin.class)
public interface MembersContext
   extends SubContexts<MemberContext>, IndexInteraction<LinksValue>, Interactions
{
   public void addmember( EntityReferenceDTO memberId);

   public LinksValue possibleusers( StringValue query );

   public LinksValue possiblegroups( StringValue query );

   abstract class Mixin
      extends InteractionsMixin
      implements MembersContext
   {
      public LinksValue index()
      {
         Members.Data members = context.get(Members.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "member" ).addDescribables( members.members() ).newLinks();
      }

      public void addmember( EntityReferenceDTO memberId)
      {
         UnitOfWork unitOfWork = module.unitOfWorkFactory().currentUnitOfWork();
         Member member = unitOfWork.get( Member.class, memberId.entity().get().identity() );

         Members members = context.get(Members.class);

         members.addMember( member );
      }

      public LinksValue possibleusers( StringValue query )
      {
         OwningOrganization org = context.get(OwningOrganization.class);
         OrganizationEntity organization = (OrganizationEntity) org.organization().get();
         Members.Data members = context.get(Members.Data.class);

         Query<UserEntity> users = organization.findUsersByUsername( query.string().get() ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         users = users.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.command("addmember");

         for (UserEntity user : users)
         {
            if (!members.members().contains( user ))
            {
               linksBuilder.addDescribable( user );
            }
         }

         return linksBuilder.newLinks();
      }

      public LinksValue possiblegroups( StringValue query )
      {
         OwningOrganization org = context.get(OwningOrganization.class);

         Query<GroupEntity> groups = ((OrganizationQueries)org.organization().get()).findGroupsByName( query.string().get() ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         groups.orderBy( orderBy( templateFor( Describable.Data.class ).description() ) );

         Members.Data members = context.get(Members.Data.class);

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.command("addmember");

         for (GroupEntity grp : groups)
         {
            if (!members.members().contains( grp )
                  && !members.equals(grp))
            {
               linksBuilder.addDescribable( grp );
            }
         }

         return linksBuilder.newLinks();
      }

      public MemberContext context( String id )
      {
         context.set( module.unitOfWorkFactory().currentUnitOfWork().get(Member.class, id ));
         return subContext( MemberContext.class );
      }
   }
}
