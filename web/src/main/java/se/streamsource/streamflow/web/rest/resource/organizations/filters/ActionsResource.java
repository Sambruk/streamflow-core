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
package se.streamsource.streamflow.web.rest.resource.organizations.filters;

import org.qi4j.api.constraint.Name;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.filter.ActionValue;
import se.streamsource.streamflow.api.administration.filter.CloseActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailNotificationActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.filters.ActionsContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.project.Member;

/**
 * TODO
 */
public class ActionsResource
   extends CommandQueryResource
   implements SubResources
{
   public ActionsResource()
   {
      super(ActionsContext.class);
   }

   public LinksValue index()
   {
      Iterable<ActionValue> actions = context(ActionsContext.class).index();
      LinksBuilder links = new LinksBuilder(module.valueBuilderFactory());
      int idx = 0;
      for (ActionValue action : actions)
      {
         if (action instanceof EmailActionValue)
         {
            String id = ((EmailActionValue) action).participant().get().identity();
            Describable describable = module.unitOfWorkFactory().currentUnitOfWork().get(Describable.class, id);
            links.addLink(describable.getDescription(), idx + "", "emailaction", idx + "/", "");
         }
         else if (action instanceof EmailNotificationActionValue)
         {
            String id = ((EmailNotificationActionValue) action).participant().get().identity();
            Describable describable = module.unitOfWorkFactory().currentUnitOfWork().get(Describable.class, id);
            links.addLink(describable.getDescription(), idx + "", "emailnotificationaction", idx + "/", "");
         }
         else if (action instanceof CloseActionValue)
         {
            links.addLink("", idx + "", "closeaction", idx + "/", "");
         }
         idx++;
      }
      return links.newLinks();
   }

   public LinksValue possiblerecipients()
   {
      final LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).command("createemail");

      for (Member member : context(ActionsContext.class).possibleRecipients())
      {
         builder.addDescribable(member);
      }

      return builder.newLinks();
   }

   public LinksValue possiblenotificationrecipients()
   {
      final LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).command("createemailnotification");

      for (Member member : context(ActionsContext.class).possibleNotificationRecipients())
      {
         builder.addDescribable(member);
      }

      return builder.newLinks();
   }

   public void createemail(@Name("entity") Member recipient)
   {
      context(ActionsContext.class).createEmail(recipient);
   }
   
   public void createemailnotification(@Name("entity") Member recipient)
   {
      context(ActionsContext.class).createEmailNotification(recipient);
   }

   public void resource(String segment) throws ResourceException
   {
      findList(RoleMap.role(FilterValue.class).actions().get(), segment);

      subResource(ActionResource.class);
   }
}
