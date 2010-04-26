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
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.SubContexts;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(ParticipantsContext.Mixin.class)
public interface ParticipantsContext
   extends SubContexts<ParticipantContext>, IndexInteraction<LinksValue>, Interactions
{
   public void addparticipant( EntityReferenceDTO participantId);
   public LinksValue possibleusers();
   public LinksValue possiblegroups();

   abstract class Mixin
      extends InteractionsMixin
      implements ParticipantsContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      public LinksValue index()
      {
         return new LinksBuilder(module.valueBuilderFactory()).rel( "participant" ).addDescribables( context.get( Participants.Data.class ).participants()).newLinks();
      }

      public void addparticipant( EntityReferenceDTO participantId)
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         Participant participant = uow.get( Participant.class, participantId.entity().get().identity() );

         Participants participants = context.get(Participants.class);

         participants.addParticipant( participant );
      }

      public LinksValue possibleusers()
      {
         OwningOrganization org = context.get(OwningOrganization.class);
         OrganizationEntity organization = (OrganizationEntity) org.organization().get();
         Participants.Data participants = context.get(Participants.Data.class);

         Query<UserEntity> users = organization.findUsersByUsername( "*" ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         users = users.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.command("addparticipant");

         for (UserEntity user : users)
         {
            if (!participants.participants().contains( user ))
            {
               String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
               linksBuilder.addDescribable( user, group );
            }
         }

         return linksBuilder.newLinks();
      }

      public LinksValue possiblegroups()
      {
         OwningOrganization org = context.get(OwningOrganization.class);

         Query<GroupEntity> groups = ((OrganizationQueries) org.organization().get()).findGroupsByName( "*" ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         groups.orderBy( orderBy( templateFor( Describable.Data.class ).description() ) );

         GroupEntity group = context.get(GroupEntity.class);

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.command("addparticipant");

         for (GroupEntity grp : groups)
         {
            if (!group.participants().contains( grp )
                  && !group.equals(grp))
            {
               String grouping = "" + Character.toUpperCase( grp.getDescription().charAt( 0 ) );
               linksBuilder.addDescribable( grp, grouping );
            }
         }

         return linksBuilder.newLinks();
      }

      public ParticipantContext context( String id )
      {
         context.set(uowf.currentUnitOfWork().get( Participant.class, id ));
         return subContext( ParticipantContext.class );
      }
   }
}
