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
package se.streamsource.streamflow.web.domain.entity.user;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.interaction.security.Authentication;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.MessageReceiver;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;
import se.streamsource.streamflow.web.domain.structure.group.Participation;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.domain.structure.user.Perspectives;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

/**
 * JAVADOC
 */
@Mixins({UserEntity.LifecycleMixin.class, UserEntity.AuthenticationMixin.class})
public interface UserEntity
      extends DomainEntity,

      // Interactions
      Drafts,
      Actor,
      Authentication,
      MessageRecipient,
      Perspectives,

      // Structure
      User,
      Contactable,
      ConversationParticipant,
      OrganizationParticipations,
      Submitter,

      // Queries
      DraftsQueries,
      AssignmentsQueries,
      OverviewQueries,
      InboxQueries,
      ProjectQueries,
      SearchCaseQueries,

      // Data
      Drafts.Data, 
      Contactable.Data,
      OrganizationParticipations.Data,
      Describable.Data,
      Participation.Data,
      UserAuthentication.Data,
      MessageRecipient.Data,
      MessageReceiver.Data,
      Perspectives.Data
{
   public static final String ADMINISTRATOR_USERNAME = "administrator";

   abstract class LifecycleMixin
         extends Describable.Mixin
         implements Lifecycle
   {
      @This
      Identity identity;

      public void create() throws LifecycleException
      {
         description().set( identity.identity().get() );
      }

      public void remove() throws LifecycleException
      {
      }
   }

   class AuthenticationMixin
         implements Authentication
   {
      @This
      UserAuthentication.Data data;

      public boolean login( String password )
      {
         if (data.disabled().get())
            return false;

         boolean correct = data.isCorrectPassword( password );

         return correct;
      }
   }
}
