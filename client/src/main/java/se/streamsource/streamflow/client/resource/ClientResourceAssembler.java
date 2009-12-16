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

package se.streamsource.streamflow.client.resource;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.resource.organizations.forms.FormDefinitionClientResource;
import se.streamsource.streamflow.client.resource.organizations.forms.FormDefinitionsClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionsClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.fields.ProjectFormDefinitionFieldClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.fields.ProjectFormDefinitionFieldsClientResource;
import se.streamsource.streamflow.client.resource.users.overview.OverviewClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.OverviewProjectClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.OverviewProjectsClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.assignments.OverviewProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.assignments.OverviewProjectAssignmentsTaskClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.waitingfor.OverviewProjectWaitingForClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.waitingfor.OverviewProjectWaitingForTaskClientResource;
import se.streamsource.streamflow.client.resource.users.search.SearchClientResource;
import se.streamsource.streamflow.client.resource.users.search.SearchTaskClientResource;

/**
 * JAVADOC
 */
public class ClientResourceAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      // /users
      module.addObjects( CommandQueryClient.class,

            ProjectFormDefinitionsClientResource.class,
            ProjectFormDefinitionClientResource.class,
            ProjectFormDefinitionFieldsClientResource.class,
            ProjectFormDefinitionFieldClientResource.class,

            OverviewClientResource.class,
            OverviewProjectsClientResource.class,
            OverviewProjectClientResource.class,
            OverviewProjectAssignmentsClientResource.class,
            OverviewProjectAssignmentsTaskClientResource.class,
            OverviewProjectWaitingForClientResource.class,
            OverviewProjectWaitingForTaskClientResource.class

      ).visibleIn( Visibility.application );

      // /organizations
      module.addObjects(

            FormDefinitionClientResource.class,
            FormDefinitionsClientResource.class,

            SearchClientResource.class,
            SearchTaskClientResource.class ).visibleIn( Visibility.application );

      module.addObjects( EventsClientResource.class ).visibleIn( Visibility.application );
   }
}
