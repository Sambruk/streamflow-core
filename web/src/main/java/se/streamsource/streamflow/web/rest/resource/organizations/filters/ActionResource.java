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
package se.streamsource.streamflow.web.rest.resource.organizations.filters;

import org.restlet.data.Form;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.api.administration.filter.ActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.web.context.administration.filters.ActionContext;
import se.streamsource.streamflow.web.domain.Describable;

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
