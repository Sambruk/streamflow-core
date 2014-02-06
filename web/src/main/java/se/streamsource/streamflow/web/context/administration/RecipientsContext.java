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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.casetype.DueOnNotificationSettings;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;

/**
 * JAVADOC
 */
public class RecipientsContext
   implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      final LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).command("removerecipient");

      if (RoleMap.role(DueOnNotificationSettings.Data.class).notificationSettings().get() != null)
      {
         for (EntityReference recipient : RoleMap.role(DueOnNotificationSettings.Data.class).notificationSettings().get().additionalrecipients().get())
         {
            Member member = module.unitOfWorkFactory().currentUnitOfWork().get( Member.class, recipient.identity() );
            builder.addDescribable(member);
         }
      }
      return builder.newLinks();
   }

   public void addrecipient( EntityValue memberId)
   {
      RoleMap.role(DueOnNotificationSettings.class).addRecipient( EntityReference.parseEntityReference( memberId.entity().get()) );
   }

   public void removerecipient( EntityValue memberId)
   {
      RoleMap.role(DueOnNotificationSettings.class).removeRecipient( EntityReference.parseEntityReference( memberId.entity().get()) );
   }
   
   public LinksValue possiblerecipients()
   {
      final LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).command("addrecipient");

      for (Member member : RoleMap.role(Members.Data.class).members())
      {
         builder.addDescribable(member);
      }

      return builder.newLinks();
   }
   
}
