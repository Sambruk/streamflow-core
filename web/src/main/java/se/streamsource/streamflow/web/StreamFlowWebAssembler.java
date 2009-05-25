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

import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.rest.assembly.RestAssembler;
import se.streamsource.streamflow.domain.CommonDomainAssembler;
import se.streamsource.streamflow.web.application.organization.OrganizationAssembler;
import se.streamsource.streamflow.web.application.security.SecurityAssembler;
import se.streamsource.streamflow.web.domain.WebDomainAssembler;
import se.streamsource.streamflow.web.infrastructure.configuration.ConfigurationAssembler;
import se.streamsource.streamflow.web.infrastructure.domain.EntityFinderAssembler;
import se.streamsource.streamflow.web.infrastructure.domain.EntityStoreAssembler;
import se.streamsource.streamflow.web.infrastructure.domain.EntityTypeRegistryAssembler;
import se.streamsource.streamflow.web.resource.ServerResourceAssembler;
import se.streamsource.streamflow.web.rest.StreamFlowRestAssembler;

/**
 * JAVADOC
 */
public class StreamFlowWebAssembler
        implements ApplicationAssembler
{
    public ApplicationAssembly assemble(ApplicationAssemblyFactory applicationFactory) throws AssemblyException
    {
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
        assembly.setName("StreamFlow web");
        LayerAssembly domainInfrastructureLayer = assembly.newLayerAssembly("Domain infrastructure");
        LayerAssembly domainLayer = assembly.newLayerAssembly("Domain");
        LayerAssembly appInfrastructureLayer = assembly.newLayerAssembly("Application infrastructure");
        LayerAssembly appLayer = assembly.newLayerAssembly("Application");
        LayerAssembly webLayer = assembly.newLayerAssembly("Web");

        webLayer.uses(appLayer, domainLayer, domainInfrastructureLayer);
        appLayer.uses(domainLayer, appInfrastructureLayer, domainInfrastructureLayer);
        domainLayer.uses(domainInfrastructureLayer);

        assembleWebLayer(webLayer);

        assembleApplicationLayer(appLayer);

        assembleDomainLayer(domainLayer);

        assembleDomainInfrastructureLayer(domainInfrastructureLayer);

        return assembly;
    }

    private void assembleDomainInfrastructureLayer(LayerAssembly domainInfrastructureLayer) throws AssemblyException
    {
        new ConfigurationAssembler().assemble(domainInfrastructureLayer.newModuleAssembly("Configuration"));
        new EntityStoreAssembler().assemble(domainInfrastructureLayer.newModuleAssembly("Entity Store"));
        new EntityFinderAssembler().assemble(domainInfrastructureLayer.newModuleAssembly("Entity Finder"));
        new EntityTypeRegistryAssembler().assemble(domainInfrastructureLayer.newModuleAssembly("Entity Type Registry"));
    }

    private void assembleWebLayer(LayerAssembly webLayer) throws AssemblyException
    {
        ModuleAssembly restModule = webLayer.newModuleAssembly("REST");
        new StreamFlowRestAssembler().assemble(restModule);
        new RestAssembler().assemble(restModule);
        new ServerResourceAssembler().assemble(restModule);
    }

    private void assembleApplicationLayer(LayerAssembly appLayer) throws AssemblyException
    {
        new SecurityAssembler().assemble(appLayer.newModuleAssembly("Security"));
        new OrganizationAssembler().assemble(appLayer.newModuleAssembly("Organization"));
    }

    private void assembleDomainLayer(LayerAssembly domainLayer) throws AssemblyException
    {
        new CommonDomainAssembler().assemble(domainLayer);
        new WebDomainAssembler().assemble(domainLayer);
    }
}
