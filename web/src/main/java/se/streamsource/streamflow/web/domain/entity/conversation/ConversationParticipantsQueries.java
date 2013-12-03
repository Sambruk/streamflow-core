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
package se.streamsource.streamflow.web.domain.entity.conversation;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.web.domain.entity.organization.OrganizationParticipationsQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

/**
 * JAVADOC
 */
@Mixins(ConversationParticipantsQueries.Mixin.class)
public interface ConversationParticipantsQueries
{
   List<ConversationParticipant> possibleParticipants( Owner owner );

   class Mixin
         implements ConversationParticipantsQueries
   {
      @This
      ConversationParticipants.Data participants;

      @Structure
      Module module;

      public List<ConversationParticipant> possibleParticipants( Owner owner )
      {
         List<ConversationParticipant> list = new ArrayList<ConversationParticipant>();
         if (owner instanceof OwningOrganization)
         {
            OwningOrganization owningOrg = (OwningOrganization) owner;
            OrganizationParticipationsQueries organization = (OrganizationParticipationsQueries) owningOrg.organization().get();

            Query<User> users = organization.users().newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
            users = users.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

            for (User user : users)
            {
               if (!participants.participants().contains( user ))
               {
                  list.add( user );
               }
            }
         } else if( owner instanceof User)
         {
            // for all organizations a user belongs to get all users
            // and add them to a list without duplicates except users already participating.
            for (Organization org : ((OrganizationParticipations.Data) owner).organizations().toList())
            {
               OrganizationParticipations.Data userOrgs = QueryExpressions.templateFor( OrganizationParticipations.Data.class );
               Query<User> query = module.queryBuilderFactory().
                     newQueryBuilder( User.class ).
                     where( QueryExpressions.contains( userOrgs.organizations(), org ) ).
                     newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

               for (User user : query)
               {
                  if (!participants.participants().contains( user ) && !list.contains( user ))
                  {
                     list.add( user );
                  }
               }

            }
         } else
         {
            OrganizationalUnitEntity oue = (OrganizationalUnitEntity) ((OwningOrganizationalUnit.Data) owner).organizationalUnit().get();
            Organization org = oue.organization().get();


            OrganizationParticipations.Data userOrgs = QueryExpressions.templateFor( OrganizationParticipations.Data.class );
            Query<User> query = module.queryBuilderFactory().
                  newQueryBuilder( User.class ).
                  where( QueryExpressions.contains( userOrgs.organizations(), org ) ).
                  newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

            for (User user : query)
            {
               if (!participants.participants().contains( user ))
               {
                  list.add( user );
               }
            }

         }
         return list;
      }
   }
}
