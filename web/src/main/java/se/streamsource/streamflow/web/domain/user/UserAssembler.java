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

package se.streamsource.streamflow.web.domain.user;

import static org.qi4j.api.common.Visibility.*;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.project.MemberValue;
import se.streamsource.streamflow.web.domain.project.MembersValue;
import se.streamsource.streamflow.web.domain.project.PermissionValue;
import se.streamsource.streamflow.web.domain.project.RoleEntity;
import se.streamsource.streamflow.web.domain.project.SharedProjectEntity;

/**
 * JAVADOC
 */
public class UserAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {
        module.addEntities(GroupEntity.class,
                RoleEntity.class,
                SharedProjectEntity.class,
                UserEntity.class).visibleIn(application);

        module.addValues(PermissionValue.class, MembersValue.class, MemberValue.class).visibleIn(application);
    }
}
