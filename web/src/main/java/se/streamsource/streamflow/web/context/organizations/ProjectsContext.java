/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.Value;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.organization.Projects;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.IndexContext;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(ProjectsContext.Mixin.class)
public interface ProjectsContext
   extends SubContexts<ProjectContext>, IndexContext<LinksValue>, Context
{
   void createproject( StringDTO name );

   abstract class Mixin
      extends ContextMixin
      implements ProjectsContext
   {
      public LinksValue index()
      {
         Projects.Data projectsState = context.role(Projects.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "project" ).addDescribables( projectsState.projects() ).newLinks();
      }

      public void createproject( StringDTO name )
      {
         Projects projects = context.role(Projects.class);

         projects.createProject( name.string().get() );
      }

      public ProjectContext context( String id )
      {
         context.playRoles(module.unitOfWorkFactory().currentUnitOfWork().get( Project.class, id));
         return subContext( ProjectContext.class );
      }
   }
}
