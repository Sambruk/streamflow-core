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
package se.streamsource.streamflow.web.context.workspace.cases.tasks;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.dci.api.Context;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTask;
import se.streamsource.streamflow.web.rest.service.mail.MailSenderService;

/**
 *
 */
@Mixins(DoubleSignatureTaskContext.Mixin.class)
public interface DoubleSignatureTaskContext
   extends Context
{

   void sendemail();

   String caseid();
   
   abstract class Mixin
      implements DoubleSignatureTaskContext
   {
      @Optional
      @Service
      MailSenderService mailSender;

      public void sendemail()
      {
         mailSender.sentEmail( role( DoubleSignatureTask.Data.class ).email().get() );
      }
      
      public String caseid() {
         return ((CaseId.Data)role( DoubleSignatureTask.Data.class ).caze().get()).caseId().get();
      }
      
   }
}
