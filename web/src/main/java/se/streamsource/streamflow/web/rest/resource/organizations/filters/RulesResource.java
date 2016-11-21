/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
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
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.filter.RuleValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.filters.RulesContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.label.Label;

/**
 * TODO
 */
public class RulesResource
      extends CommandQueryResource
      implements SubResources
{
   public RulesResource()
   {
      super(RulesContext.class);
   }

   public LinksValue index()
   {
      Iterable<RuleValue> rules = context(RulesContext.class).index();
      LinksBuilder links = new LinksBuilder(module.valueBuilderFactory());
      int idx = 0;
      for (RuleValue rule : rules)
      {
         if (rule instanceof LabelRuleValue)
         {
            String id = ((LabelRuleValue) rule).label().get().identity();
            Describable describable = module.unitOfWorkFactory().currentUnitOfWork().get(Describable.class, id);
            links.addLink(describable.getDescription(), idx + "", "labelrule", idx + "/", "");
         }
         idx++;
      }
      return links.newLinks();
   }

   public LinksValue possiblelabels()
   {
      return context(RulesContext.class).possibleLabels();
   }

   public void createlabel(@Name("entity") Label label)
   {
      context(RulesContext.class).createLabel(label);
   }

   public void resource(String segment) throws ResourceException
   {
      findList(RoleMap.role(FilterValue.class).rules().get(), segment);

      subResource(RuleResource.class);
   }
}
