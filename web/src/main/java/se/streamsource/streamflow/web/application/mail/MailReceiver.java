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

package se.streamsource.streamflow.web.application.mail;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.mixin.NoopMixin;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.factory.ApplicationEventCreationConcern;
import se.streamsource.streamflow.infrastructure.event.application.factory.ApplicationEventCreator;

/**
 * Factory for receiving emails. The events will be created by the ReceiveMailService, and services
 * can then listen for them.
 */
@Mixins(NoopMixin.class)
@Concerns(ApplicationEventCreationConcern.class)
public interface MailReceiver
   extends ApplicationEventCreator
{
   void receivedEmail( @Optional ApplicationEvent event, EmailValue email);
}
