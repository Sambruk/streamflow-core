/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.rest.service.mail;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.MailSender;

@Mixins( MailSenderService.Mixin.class )
public interface MailSenderService
   extends MailSender, ServiceComposite
{
   void sentEmail( EmailValue email );

   abstract class Mixin
      implements MailSenderService
   {

      @This
      MailSender mailSender;

      public void sentEmail( EmailValue email )
      {
         mailSender.sentEmail( null, email );
      }
   }
}