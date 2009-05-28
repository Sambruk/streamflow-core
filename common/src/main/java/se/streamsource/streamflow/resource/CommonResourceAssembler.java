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

package se.streamsource.streamflow.resource;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.application.shared.inbox.NewSharedTaskCommand;
import se.streamsource.streamflow.domain.user.UserSpecification;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.resource.assignment.AssignedTaskValue;
import se.streamsource.streamflow.resource.assignment.AssignmentsTaskListValue;
import se.streamsource.streamflow.resource.inbox.InboxTaskListValue;
import se.streamsource.streamflow.resource.inbox.InboxTaskValue;
import se.streamsource.streamflow.resource.inbox.TasksQuery;
import se.streamsource.streamflow.resource.roles.DescriptionValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceValue;
import se.streamsource.streamflow.resource.user.RegisterUserCommand;

/**
 * JAVADOC
 */
public class CommonResourceAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        // Commands
        module.addValues(RegisterUserCommand.class,
                DescriptionValue.class,
                EntityReferenceValue.class,
                NewSharedTaskCommand.class).visibleIn(Visibility.application);

        // Queries
        module.addValues(UserSpecification.class, TasksQuery.class).visibleIn(Visibility.application);

        // Result values
        module.addValues(ListValue.class, ListItemValue.class,
                InboxTaskListValue.class,
                InboxTaskValue.class,
                AssignmentsTaskListValue.class,
                AssignedTaskValue.class,
                TreeValue.class, TreeNodeValue.class).visibleIn(Visibility.application);
    }
}
