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
import se.streamsource.dci.api.DeleteContext;
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
public class RuleContext
   implements IndexContext<RuleValue>, DeleteContext
{
   @Structure
   Module module;

   @Uses
   Filters filters;

   @Uses
   FilterValue filter;

   @Uses
   RuleValue rule;

   @Uses
   Integer index;

   public RuleValue index()
   {
      return rule;
   }

   public void update(RuleValue rule)
   {
      int filterIdx = filters.indexOf(filter);

      ValueBuilder<FilterValue> builder = filter.buildWith();
      builder.prototype().rules().get().set(index, rule);

      filters.updateFilter(filterIdx, builder.newInstance());
   }

   public void delete()
   {
      int filterIdx = filters.indexOf(filter);

      int ruleIdx = filter.rules().get().indexOf(rule);
      ValueBuilder<FilterValue> builder = filter.buildWith();
      builder.prototype().rules().get().remove(ruleIdx);

      filters.updateFilter(filterIdx, builder.newInstance());
   }
}
