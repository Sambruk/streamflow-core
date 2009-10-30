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
import se.streamsource.streamflow.client.domain.individual.IndividualAssembler;
import se.streamsource.streamflow.client.infrastructure.application.RestletClientAssembler;
import se.streamsource.streamflow.client.infrastructure.configuration.ConfigurationAssembler;
import se.streamsource.streamflow.client.infrastructure.domain.ClientEntityStoreAssembler;
import se.streamsource.streamflow.client.infrastructure.events.ClientEventsAssembler;
import se.streamsource.streamflow.client.infrastructure.ui.UIInfrastructureAssembler;
import se.streamsource.streamflow.client.resource.ClientResourceAssembler;
import se.streamsource.streamflow.client.ui.UIAssembler;
import se.streamsource.streamflow.client.ui.administration.AdministrationAssembler;
import se.streamsource.streamflow.client.ui.events.ClientEventSubscriptionAssembler;
import se.streamsource.streamflow.client.ui.menu.MenuAssembler;
import se.streamsource.streamflow.client.ui.overview.OverviewAssembler;
import se.streamsource.streamflow.client.ui.search.SearchAssembler;
import se.streamsource.streamflow.client.ui.task.TaskAssembler;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceAssembler;
import se.streamsource.streamflow.domain.CommonDomainAssembler;
import se.streamsource.streamflow.resource.CommonResourceAssembler;

/**
 * JAVADOC
 */
public class StreamFlowClientAssembler
        implements ApplicationAssembler
{
    Object[] serviceObjects;

    public StreamFlowClientAssembler(Object... serviceObjects)
    {
        this.serviceObjects = serviceObjects;
    }

    public ApplicationAssembly assemble(ApplicationAssemblyFactory applicationFactory) throws AssemblyException
    {
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
        assembly.setName("StreamFlow client");
        assembly.setVersion("0.1");

        // Create layers
        LayerAssembly clientDomainInfrastructureLayer = assembly.layerAssembly("Client domain infrastructure");
        LayerAssembly clientDomainLayer = assembly.layerAssembly("Client domain");
        LayerAssembly uiLayer = assembly.layerAssembly("UI");

        // Define layer usage
        uiLayer.uses(clientDomainLayer,clientDomainInfrastructureLayer);
        clientDomainLayer.uses(clientDomainInfrastructureLayer);

        assembleUILayer(uiLayer);

        assembleClientDomainLayer(clientDomainLayer);
        assembleClientDomainInfrastructureLayer(clientDomainInfrastructureLayer);

        for (Object serviceObject : serviceObjects)
        {
            assembly.setMetaInfo(serviceObject);
        }

        return assembly;
    }

    protected void assembleUILayer(LayerAssembly uiLayer) throws AssemblyException
    {
        new SearchAssembler().assemble(uiLayer.moduleAssembly("Search"));
        new AdministrationAssembler().assemble(uiLayer.moduleAssembly("Administration"));
        new MenuAssembler().assemble(uiLayer.moduleAssembly("Menu"));
        new WorkspaceAssembler().assemble(uiLayer.moduleAssembly("Workspace"));
        new TaskAssembler().assemble(uiLayer.moduleAssembly("Tasks"));
        new OverviewAssembler().assemble(uiLayer.moduleAssembly("Overview"));
        new UIAssembler().assemble(uiLayer.moduleAssembly("UI View"));
        new UIInfrastructureAssembler().assemble(uiLayer.moduleAssembly("View infrastructure"));
        new RestletClientAssembler().assemble(uiLayer.moduleAssembly("REST Client"));
        new ClientEventSubscriptionAssembler().assemble(uiLayer.moduleAssembly("Client event subscribtion"));
    }

    protected void assembleClientDomainLayer(LayerAssembly domainLayer) throws AssemblyException
    {
        new CommonDomainAssembler().assemble(domainLayer);
        new IndividualAssembler().assemble(domainLayer.moduleAssembly("Individual"));

        ModuleAssembly module = domainLayer.moduleAssembly("REST domain model");
        new CommonResourceAssembler().assemble(module);
        new ClientResourceAssembler().assemble(module);
    }

    protected void assembleClientDomainInfrastructureLayer(LayerAssembly domainInfrastructureLayer) throws AssemblyException
    {
        new ConfigurationAssembler().assemble(domainInfrastructureLayer.moduleAssembly("Configuration"));
        new ClientEntityStoreAssembler().assemble(domainInfrastructureLayer.moduleAssembly("Client EntityStore"));
        new ClientEventsAssembler().assemble( domainInfrastructureLayer.moduleAssembly("Client events" ));
    }

}