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
package se.streamsource.streamflow.web.context.administration.filters;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.api.administration.filter.ActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;

/**
 * TODO
 */
public class ActionContext
   implements IndexContext<ActionValue>, DeleteContext
{
   @Structure
   Module module;

   @Uses
   Filters filters;

   @Uses
   FilterValue filter;

   @Uses
   ActionValue action;

   @Uses
   Integer index;

   public ActionValue index()
   {
      return action;
   }

   public void update(ActionValue action)
   {
      int filterIdx = filters.indexOf(filter);

      ValueBuilder<FilterValue> builder = filter.buildWith();
      builder.prototype().actions().get().set(index, action);

      filters.updateFilter(filterIdx, builder.newInstance());
   }

   public void delete()
   {
      int filterIdx = filters.indexOf(filter);

      int actionIdx = filter.actions().get().indexOf(action);
      ValueBuilder<FilterValue> builder = filter.buildWith();
      builder.prototype().actions().get().remove(actionIdx);

      filters.updateFilter(filterIdx, builder.newInstance());
   }
}
