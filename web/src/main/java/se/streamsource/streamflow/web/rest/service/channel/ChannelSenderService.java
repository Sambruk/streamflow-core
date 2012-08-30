/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.rest.service.channel;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.web.application.channel.ChannelMessageValue;
import se.streamsource.streamflow.web.application.channel.ChannelSender;

@Mixins( ChannelSenderService.Mixin.class )
public interface ChannelSenderService
   extends ChannelSender, ServiceComposite
{
   void sentChannelMessage( ChannelMessageValue message );

   abstract class Mixin
      implements ChannelSenderService
   {

      @This
      ChannelSender channelSender;

      public void sentChannelMessage( ChannelMessageValue message )
      {
         channelSender.sentChannelMessage( null, message );
      }
   }
}