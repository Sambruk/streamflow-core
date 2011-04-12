/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import se.streamsource.streamflow.domain.contact.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.web.domain.entity.*;
import se.streamsource.streamflow.web.domain.entity.gtd.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.interaction.profile.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;
import se.streamsource.streamflow.web.domain.structure.conversation.*;
import se.streamsource.streamflow.web.domain.structure.form.*;
import se.streamsource.streamflow.web.domain.structure.group.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

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
         description().set(identity.identity().get());
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

      public boolean login(String password)
      {
         if (data.disabled().get())
            return false;

         boolean correct = data.isCorrectPassword(password);

         return correct;
      }
   }
}
