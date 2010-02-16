/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.resource.organizations.projects;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.organization.Projects;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/organizationalunits/{ou}/projects
 */
public class ProjectsServerResource
      extends CommandQueryServerResource
{
   public ListValue projects()
   {
      String identity = getRequest().getAttributes().get( "ou" ).toString();
      Projects.Data projectsState = uowf.currentUnitOfWork().get( Projects.Data.class, identity );

      return new ListValueBuilder( vbf ).addDescribableItems( projectsState.projects() ).newList();
   }

   public void createProject( StringDTO name ) throws ResourceException
   {
      String identity = getRequest().getAttributes().get( "ou" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      Projects projects = uow.get( Projects.class, identity );

      checkPermission( projects );
      projects.createProject( name.string().get() );
   }
}