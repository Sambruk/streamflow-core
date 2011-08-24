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

package se.streamsource.streamflow.web.rest.resource.workspace;

import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.workspace.PerspectiveContext;
import se.streamsource.streamflow.web.context.workspace.PerspectivesContext;
import se.streamsource.streamflow.web.domain.entity.user.PerspectiveEntity;
import se.streamsource.streamflow.web.domain.structure.user.Perspective;

/**
 * JAVADOC
 */
public class WorkspacePerspectivesResource
        extends CommandQueryResource
        implements SubResources
{
   public WorkspacePerspectivesResource()
   {
      super(PerspectivesContext.class);
   }

   public LinksValue index() throws Throwable
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      Iterable<Perspective> perspectives = context(PerspectivesContext.class).index();
      builder.addDescribables(perspectives);

      return builder.newLinks();
   }

   public void resource(String segment) throws ContextNotFoundException
   {
      setRole(PerspectiveEntity.class, segment);
      subResourceContexts(PerspectiveContext.class, DescribableContext.class);
   }
}
