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

import org.restlet.data.Form;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.filter.RuleValue;
import se.streamsource.streamflow.web.context.administration.filters.RuleContext;
import se.streamsource.streamflow.web.domain.structure.label.Label;

/**
 * TODO
 */
public class RuleResource
   extends CommandQueryResource
{
   public RuleResource()
   {
      super(RuleContext.class);
   }

   public Form index()
   {
      RuleValue rule = context(RuleContext.class).index();
      Form form = new Form();
      if (rule instanceof LabelRuleValue)
      {
         LabelRuleValue labelRule = (LabelRuleValue) rule;
         form.set("label", module.unitOfWorkFactory().currentUnitOfWork().get(Label.class, labelRule.label().get().identity()).getDescription());
      }

      return form;
   }
}
