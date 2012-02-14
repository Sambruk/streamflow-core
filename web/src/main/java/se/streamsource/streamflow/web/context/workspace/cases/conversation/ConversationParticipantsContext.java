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
package se.streamsource.streamflow.web.context.workspace.cases.conversation;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.conversation.ExternalEmailValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationParticipantsQueries;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;
import se.streamsource.streamflow.web.domain.structure.user.Users;

import java.util.List;

/**
 * JAVADOC
 */
public class ConversationParticipantsContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      return new LinksBuilder( module.valueBuilderFactory() ).rel( "participant" ).addDescribables( RoleMap.role( ConversationParticipants.Data.class ).participants() ).newLinks();
   }

   public void addexternalparticipant( ExternalEmailValue mailUser )
   {

      ConversationParticipant participant = module.unitOfWorkFactory().currentUnitOfWork()
            .get( Users.class, UsersEntity.USERS_ID ).createEmailUser(  mailUser.emailAddress().get() );

      RoleMap.role( ConversationParticipants.class ).addParticipant( participant );
   }

   public void addparticipant( EntityValue participantId )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      ConversationParticipant participant = uow.get( ConversationParticipant.class, participantId.entity().get() );

      ConversationParticipants participants = RoleMap.role( ConversationParticipants.class );

      participants.addParticipant( participant );
   }

   public LinksValue possibleparticipants()
   {
      Ownable.Data ownable = RoleMap.role( Ownable.Data.class );
      Owner owner = ownable.owner().get();
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "addparticipant" );

      if (owner != null)
      {
         List<ConversationParticipant> possibleParticipants = RoleMap.role( ConversationParticipantsQueries.class ).possibleParticipants( owner );

         for (ConversationParticipant possibleParticipant : possibleParticipants)
         {
            String group = "" + Character.toUpperCase( possibleParticipant.getDescription().charAt( 0 ) );
            linksBuilder.addDescribable( possibleParticipant, group );
         }
      }

      return linksBuilder.newLinks();
   }
}