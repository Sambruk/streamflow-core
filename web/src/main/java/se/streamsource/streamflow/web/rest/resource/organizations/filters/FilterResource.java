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

import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.web.context.administration.filters.FilterContext;

/**
 * TODO
 */
public class FilterResource
   extends CommandQueryResource
{
   public FilterResource()
   {
      super(FilterContext.class);
   }

   public Form index()
   {
      FilterValue filter = context(FilterContext.class).index();
      Form form = new Form();
      form.set("name", filter.name().get());
      form.set("enabled", filter.enabled().get()+"");
      form.set("matching", filter.matching().get().name());
      return form;
   }

   public void update(Form form)
   {
      FilterContext context = context(FilterContext.class);
      ValueBuilder<FilterValue> filter = context.index().buildWith();
      filter.prototype().name().set(form.getFirstValue("name"));
      filter.prototype().enabled().set(Boolean.parseBoolean(form.getFirstValue("enabled")));
      filter.prototype().matching().set(FilterValue.MatchingEnum.valueOf(form.getFirstValue("matching")));
      context.update(filter.newInstance());
   }

   @SubResource
   public void rules()
   {
      subResource(RulesResource.class);
   }

   @SubResource
   public void actions()
   {
      subResource(ActionsResource.class);
   }
}
