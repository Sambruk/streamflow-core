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

package se.streamsource.streamflow.web.context.conversation;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsQueries;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(ConversationParticipantsContext.Mixin.class)
public interface ConversationParticipantsContext
   extends SubContexts<ConversationParticipantContext>, IndexContext<LinksValue>, Context
{
   public void addparticipant( EntityReferenceDTO participantId);
   public LinksValue possibleparticipants();

   abstract class Mixin
      extends ContextMixin
      implements ConversationParticipantsContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      public LinksValue index()
      {
         return new LinksBuilder(module.valueBuilderFactory()).rel( "participant" ).addDescribables( context.role( ConversationParticipants.Data.class ).participants()).newLinks();
      }

      public void addparticipant( EntityReferenceDTO participantId)
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         ConversationParticipant participant = uow.get( ConversationParticipant.class, participantId.entity().get().identity() );

         ConversationParticipants participants = context.role(ConversationParticipants.class);

         participants.addParticipant( participant );
      }

      public LinksValue possibleparticipants()
      {

         //OwningOrganization org = context.role(OwningOrganization.class);
         //TODO how to get from one context tree to another?
         OrganizationsQueries org = uowf.currentUnitOfWork().get( OrganizationsQueries.class, OrganizationsEntity.ORGANIZATIONS_ID );
         Query<OrganizationEntity> query = org.organizations().newQuery( uowf.currentUnitOfWork() );

         OrganizationEntity organization = query.find();

         ConversationParticipants.Data participants = context.role(ConversationParticipants.Data.class);

         Query<User> users = organization.users( ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         users = users.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.command("addparticipant");

         for (User user : users)
         {
            if (!participants.participants().contains( user ))
            {
               linksBuilder.addDescribable( user );
            }
         }

         return linksBuilder.newLinks();
      }


      public ConversationParticipantContext context( String id )
      {
         context.playRoles(uowf.currentUnitOfWork().get( ConversationParticipant.class, id ));
         return subContext( ConversationParticipantContext.class );
      }
   }
}