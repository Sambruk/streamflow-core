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

package se.streamsource.streamflow.client.infrastructure.application;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.RdfFactoryService;
import org.qi4j.library.rdf.entity.EntityStateParser;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.rest.client.SPARQLEntityFinderConfiguration;
import org.qi4j.rest.client.SPARQLEntityFinderService;
import org.qi4j.spi.entity.helpers.EntityTypeRegistryService;

/**
 * JAVADOC
 */
public class EntityFinderAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addObjects(EntityStateSerializer.class, EntityStateParser.class, EntityTypeSerializer.class);
        module.addEntities(SPARQLEntityFinderConfiguration.class);
        module.on(SPARQLEntityFinderConfiguration.class).to().sparqlUrl().set("http://localhost/streamflow-web/qi4j/query.rdf");
        module.addServices(RdfFactoryService.class);
        module.addServices(MemoryEntityStoreService.class, EntityTypeRegistryService.class);

        // Domain model finder
        module.addServices(SPARQLEntityFinderService.class)
                .visibleIn(Visibility.application);
    }
}