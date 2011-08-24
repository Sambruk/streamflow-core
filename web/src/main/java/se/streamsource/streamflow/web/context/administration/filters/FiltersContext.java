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

package se.streamsource.streamflow.web.context.administration.filters;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.api.administration.filter.AssignActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.filter.RuleValue;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;

import java.util.ArrayList;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * TODO
 */
public class FiltersContext
   implements IndexContext<Iterable<FilterValue>>
{
   @Structure
   Module module;

   @Uses
   Filters filters;

   public Iterable<FilterValue> index()
   {
      return ((Filters.Data)filters).filters().get();
   }

   public void create(@Name("name") String name)
   {
      ValueBuilder<FilterValue> builder = module.valueBuilderFactory().newValueBuilder(FilterValue.class);
      FilterValue filter = builder.prototype();
      filter.rules().set(new ArrayList<RuleValue>());
      filter.name().set(name);
      filter.enabled().set(true);

      // TODO This is for testing
      ValueBuilder<LabelRuleValue> ruleBuilder = module.valueBuilderFactory().newValueBuilder(LabelRuleValue.class);

      QueryBuilder<Label> queryBuilder = module.queryBuilderFactory().newQueryBuilder(Label.class).
            where(eq(templateFor(Describable.Data.class).description(), "Via webben"));
      Label label = queryBuilder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).find();
      if (label != null)
      {
         ruleBuilder.prototype().label().set(EntityReference.getEntityReference(label));
         filter.rules().get().add(ruleBuilder.newInstance());
      }

      QueryBuilder<Assignee> assigneeQuery = module.queryBuilderFactory().newQueryBuilder(Assignee.class).where(eq(templateFor(Describable.Data.class).description(), "Administrator"));
      Assignee assignee = assigneeQuery.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).find();
      if (assignee != null)
      {
         ValueBuilder<AssignActionValue> actionBuilder = module.valueBuilderFactory().newValueBuilder(AssignActionValue.class);
         actionBuilder.prototype().assignee().set(EntityReference.getEntityReference(assignee));
         filter.actions().get().add(actionBuilder.newInstance());

         ValueBuilder<EmailActionValue> emailBuilder = module.valueBuilderFactory().newValueBuilder(EmailActionValue.class);
         emailBuilder.prototype().participant().set(EntityReference.getEntityReference(assignee));
         filter.actions().get().add(emailBuilder.newInstance());
      }

      filter = builder.newInstance();

      filters.addFilter(filter);
   }
}
