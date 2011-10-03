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

package se.streamsource.streamflow.web.rest.resource.organizations.filters;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.filter.ActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.filter.RuleValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.filters.ActionContext;
import se.streamsource.streamflow.web.context.administration.filters.FilterContext;
import se.streamsource.streamflow.web.context.administration.filters.FiltersContext;
import se.streamsource.streamflow.web.context.administration.filters.RuleContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;

/**
 * TODO
 */
public class ActionResource
   extends CommandQueryResource
{
   public ActionResource()
   {
      super(ActionContext.class);
   }

   public Form index()
   {
      ActionValue action = context(ActionContext.class).index();
      Form form = new Form();
      if (action instanceof EmailActionValue)
      {
         EmailActionValue emailAction = (EmailActionValue) action;
         form.set("recipient", module.unitOfWorkFactory().currentUnitOfWork().get(Describable.class, emailAction.participant().get().identity()).getDescription());
      }

      return form;
   }
}
