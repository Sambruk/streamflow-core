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

package se.streamsource.streamflow.web.infrastructure.domain;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.assembly.RdfFactoryService;
import org.qi4j.index.rdf.assembly.RdfQueryService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.library.rdf.repository.NativeRepositoryService;

/**
 * JAVADOC
 */
public class EntityFinderAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
        if (mode.equals(Application.Mode.development) || mode.equals(Application.Mode.test))
        {
            // In-memory store
            module.addServices(MemoryRepositoryService.class).instantiateOnStartup().visibleIn(Visibility.application).identifiedBy("rdf-repository");
        } else if (mode.equals(Application.Mode.production))
        {
            // Native storage
            module.addServices(NativeRepositoryService.class).visibleIn(Visibility.application).instantiateOnStartup().identifiedBy("rdf-repository");
        }

        module.addObjects(EntityStateSerializer.class, EntityTypeSerializer.class);
        module.addServices(RdfQueryService.class).instantiateOnStartup().visibleIn(Visibility.application);
        module.addServices(RdfFactoryService.class);
    }
}
