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

package se.streamsource.streamflow.web.rest.resource.organizations;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.FilterContext;
import se.streamsource.streamflow.web.context.administration.FiltersContext;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;

/**
 * TODO
 */
public class FiltersResource
   extends CommandQueryResource
   implements SubResources
{
   public FiltersResource()
   {
      super(FiltersContext.class);
   }

   public LinksValue index()
   {
      Iterable<FilterValue> filters = context(FiltersContext.class).index();
      LinksBuilder links = new LinksBuilder(module.valueBuilderFactory());
      int idx = 0;
      for (FilterValue filter : filters)
      {
         links.addLink(filter.name().get(), filter.name().get(), "filter", idx+"/", "");
         idx++;
      }
      return links.newLinks();
   }

   public void resource(String segment) throws ResourceException
   {
      findList(RoleMap.role(Filters.Data.class).filters().get(), segment);

      subResourceContexts(FilterContext.class);
   }
}
