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

package se.streamsource.streamflow.web;

import org.qi4j.bootstrap.*;
import org.qi4j.runtime.bootstrap.ApplicationAssemblyImpl;
import se.streamsource.streamflow.domain.CommonDomainAssembler;
import se.streamsource.streamflow.web.application.management.ManagementAssembler;
import se.streamsource.streamflow.web.application.organization.OrganizationAssembler;
import se.streamsource.streamflow.web.application.security.SecurityAssembler;
import se.streamsource.streamflow.web.application.statistics.StatisticsAssembler;
import se.streamsource.streamflow.web.configuration.ConfigurationAssembler;
import se.streamsource.streamflow.web.domain.WebDomainAssembler;
import se.streamsource.streamflow.web.infrastructure.domain.EntityFinderAssembler;
import se.streamsource.streamflow.web.infrastructure.domain.EventStoreAssembler;
import se.streamsource.streamflow.web.infrastructure.domain.ServerEntityStoreAssembler;
import se.streamsource.streamflow.web.infrastructure.event.EventAssembler;
import se.streamsource.streamflow.web.infrastructure.database.DatabaseAssembler;
import se.streamsource.streamflow.web.resource.ServerResourceAssembler;
import se.streamsource.streamflow.web.rest.StreamFlowRestAssembler;

/**
 * JAVADOC
 */
public class StreamFlowWebAssembler
        implements ApplicationAssembler
{
    private Object[] serviceObjects;

    public StreamFlowWebAssembler(Object... serviceObjects)
    {
        this.serviceObjects = serviceObjects;
    }

    public ApplicationAssembly assemble(ApplicationAssemblyFactory applicationFactory) throws AssemblyException
    {
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
        assembly.setName("StreamFlow web");
        if (assembly instanceof ApplicationAssemblyImpl)
        {
            ((ApplicationAssemblyImpl) assembly).setVersion("0.1");
        }
        LayerAssembly configurationLayer = assembly.layerAssembly("Configuration");
        LayerAssembly domainInfrastructureLayer = assembly.layerAssembly("Domain infrastructure");
        LayerAssembly domainLayer = assembly.layerAssembly("Domain");
        LayerAssembly appLayer = assembly.layerAssembly("Application");
        LayerAssembly webLayer = assembly.layerAssembly("Web");

        webLayer.uses(appLayer, domainLayer, domainInfrastructureLayer);
        appLayer.uses(domainLayer, domainInfrastructureLayer, configurationLayer);
        domainLayer.uses(domainInfrastructureLayer);
        domainInfrastructureLayer.uses(configurationLayer);

        assembleWebLayer(webLayer);

        assembleApplicationLayer(appLayer);

        assembleDomainLayer(domainLayer);

        assembleDomainInfrastructureLayer(domainInfrastructureLayer);

        assembleConfigurationLayer(configurationLayer);

        for (Object serviceObject : serviceObjects)
        {
            assembly.setMetaInfo(serviceObject);
        }

        return assembly;
    }

    private void assembleConfigurationLayer(LayerAssembly configurationlayer) throws AssemblyException
    {
        new ConfigurationAssembler().assemble(configurationlayer.moduleAssembly("Configuration"));
    }

    private void assembleDomainInfrastructureLayer(LayerAssembly domainInfrastructureLayer) throws AssemblyException
    {
        new DatabaseAssembler().assemble(domainInfrastructureLayer.moduleAssembly("Database"));
        new EventStoreAssembler().assemble(domainInfrastructureLayer.moduleAssembly("Event Store"));
        new ServerEntityStoreAssembler().assemble(domainInfrastructureLayer.moduleAssembly("Entity Store"));
        new EntityFinderAssembler().assemble(domainInfrastructureLayer.moduleAssembly("Entity Finder"));
        new EventAssembler().assemble(domainInfrastructureLayer.moduleAssembly("Events"));
    }

    private void assembleWebLayer(LayerAssembly webLayer) throws AssemblyException
    {
        ModuleAssembly restModule = webLayer.moduleAssembly("REST");
        new StreamFlowRestAssembler().assemble(restModule);
        new ServerResourceAssembler().assemble(restModule);
    }

    private void assembleApplicationLayer(LayerAssembly appLayer) throws AssemblyException
    {
        new ManagementAssembler().assemble(appLayer.moduleAssembly("Management"));
        new SecurityAssembler().assemble(appLayer.moduleAssembly("Security"));
        new OrganizationAssembler().assemble(appLayer.moduleAssembly("Organization"));
        new StatisticsAssembler().assemble(appLayer.moduleAssembly("Statistics"));
    }

    protected void assembleDomainLayer(LayerAssembly domainLayer) throws AssemblyException
    {
        new CommonDomainAssembler().assemble(domainLayer);
        new WebDomainAssembler().assemble(domainLayer);
    }
}
