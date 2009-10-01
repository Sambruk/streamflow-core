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

package se.streamsource.streamflow.web.domain.project;

import static org.qi4j.api.common.Visibility.application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * JAVADOC
 */
public class ProjectAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {
        module.addEntities(
                ProjectRoleEntity.class,
                ProjectEntity.class).visibleIn(application);

        module.addValues(PermissionValue.class).visibleIn(application);
    }
}