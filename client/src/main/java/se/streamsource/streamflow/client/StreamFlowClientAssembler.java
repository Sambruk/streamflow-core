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

package se.streamsource.streamflow.client;

import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.domain.checklist.ChecklistAssembler;
import se.streamsource.streamflow.client.domain.individual.IndividualAssembler;
import se.streamsource.streamflow.client.domain.workspace.WorkspaceAssembler;
import se.streamsource.streamflow.client.infrastructure.application.RestletClientAssembler;
import se.streamsource.streamflow.client.infrastructure.domain.ClientEntityStoreAssembler;
import se.streamsource.streamflow.client.infrastructure.ui.UIInfrastructureAssembler;
import se.streamsource.streamflow.client.resource.ClientResourceAssembler;
import se.streamsource.streamflow.client.ui.UIAssembler;
import se.streamsource.streamflow.client.ui.administration.AdministrationAssembler;
import se.streamsource.streamflow.client.ui.menu.MenuAssembler;
import se.streamsource.streamflow.client.ui.navigator.NavigatorUIAssembler;
import se.streamsource.streamflow.client.ui.search.SearchUIAssembler;
import se.streamsource.streamflow.client.ui.shared.SharedUIAssembler;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceUIAssembler;
import se.streamsource.streamflow.domain.contact.ContactAssembler;
import se.streamsource.streamflow.resource.CommonResourceAssembler;

/**
 * JAVADOC
 */
public class StreamFlowClientAssembler
        implements ApplicationAssembler
{
    public ApplicationAssembly assemble(ApplicationAssemblyFactory applicationFactory) throws AssemblyException
    {
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
        assembly.setName("StreamFlow client");

        // Create layers
        LayerAssembly clientDomainInfrastructureLayer = assembly.newLayerAssembly("Client domain infrastructure");
        LayerAssembly clientDomainLayer = assembly.newLayerAssembly("Client domain");
        LayerAssembly uiLayer = assembly.newLayerAssembly("UI");

        // Define layer usage
        uiLayer.uses(clientDomainLayer);
        clientDomainLayer.uses(clientDomainInfrastructureLayer);

        assembleUILayer(uiLayer);

        assembleClientDomainLayer(clientDomainLayer);
        assembleClientDomainInfrastructureLayer(clientDomainInfrastructureLayer);

        return assembly;
    }

    protected void assembleUILayer(LayerAssembly uiLayer) throws AssemblyException
    {
        new AdministrationAssembler().assemble(uiLayer.newModuleAssembly("AdministrationQueries"));
        new MenuAssembler().assemble(uiLayer.newModuleAssembly("Menu view"));
        new NavigatorUIAssembler().assemble(uiLayer.newModuleAssembly("Navigator view"));
        new SearchUIAssembler().assemble(uiLayer.newModuleAssembly("Search view"));
        new SharedUIAssembler().assemble(uiLayer.newModuleAssembly("Shared view"));
        new WorkspaceUIAssembler().assemble(uiLayer.newModuleAssembly("Workspace view"));
        new UIAssembler().assemble(uiLayer.newModuleAssembly("UI View"));
        new UIInfrastructureAssembler().assemble(uiLayer.newModuleAssembly("View infrastructure"));
        new RestletClientAssembler().assemble(uiLayer.newModuleAssembly("REST Client"));
    }

/*
    protected void assembleApplicationLayer(LayerAssembly appLayer) throws AssemblyException
    {
        new NavigatorApplicationAssembler().assemble(appLayer.newModuleAssembly("Navigator"));
        new SharedApplicationAssembler().assemble(appLayer.newModuleAssembly("Shared"));

        new ContactAssembler().assemble(appLayer.newModuleAssembly("Contact"));
    }
*/

/*
    protected void assembleApplicationInfrastructureLayer(LayerAssembly appInfrastructureLayer) throws AssemblyException
    {
        new EntityFinderAssembler().assemble(appInfrastructureLayer.newModuleAssembly("Entity Finder"));
    }
*/

    protected void assembleClientDomainLayer(LayerAssembly domainLayer) throws AssemblyException
    {
        new ChecklistAssembler().assemble(domainLayer.newModuleAssembly("Checklist"));
        new IndividualAssembler().assemble(domainLayer.newModuleAssembly("Individual"));
        new WorkspaceAssembler().assemble(domainLayer.newModuleAssembly("Workspace"));

        new ContactAssembler().assemble(domainLayer.newModuleAssembly("Contact"));

        ModuleAssembly module = domainLayer.newModuleAssembly("REST domain model");
        new CommonResourceAssembler().assemble(module);
        new ClientResourceAssembler().assemble(module);
    }

    protected void assembleClientDomainInfrastructureLayer(LayerAssembly infrastructureLayer) throws AssemblyException
    {
        new ClientEntityStoreAssembler().assemble(infrastructureLayer.newModuleAssembly("Client EntityStore"));
    }

}