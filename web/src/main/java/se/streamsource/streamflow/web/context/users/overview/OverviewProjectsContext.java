/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.users.overview;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.ProjectQueries;
import se.streamsource.dci.api.SubContexts;

/**
 * JAVADOC
 */
@Mixins(OverviewProjectsContext.Mixin.class)
public interface OverviewProjectsContext
   extends SubContexts<OverviewProjectContext>, Interactions
{
   public LinksValue projects();

   abstract class Mixin
      extends InteractionsMixin
      implements OverviewProjectsContext
   {
      @Structure
      Module module;

      public LinksValue projects()
      {
         ProjectQueries participant = context.get(ProjectQueries.class);
         return new LinksBuilder(module.valueBuilderFactory()).addDescribables( participant.allProjects() ).newLinks();
      }

      public OverviewProjectContext context( String id )
      {
         context.set( module.unitOfWorkFactory().currentUnitOfWork().get( ProjectEntity.class, id ));
         return subContext( OverviewProjectContext.class );
      }
   }
}
