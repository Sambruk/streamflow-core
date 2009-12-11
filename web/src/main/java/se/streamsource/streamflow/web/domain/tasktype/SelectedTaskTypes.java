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

package se.streamsource.streamflow.web.domain.tasktype;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(SelectedTaskTypes.Mixin.class)
public interface SelectedTaskTypes
{
    void addSelectedTaskType( TaskType taskType );
    void removeSelectedTaskType( TaskType taskType );

    interface Data
    {
        ManyAssociation<TaskType> selectedTaskTypes();

        ListValue possibleTaskTypes(ManyAssociation<TaskType> taskTypes);

        void selectedTaskTypeAdded( DomainEvent event, TaskType taskType );
        void selectedTaskTypeRemoved( DomainEvent event, TaskType taskType );
    }

    abstract class Mixin
        implements SelectedTaskTypes, Data
    {
        @Structure
        ValueBuilderFactory vbf;

        public void addSelectedTaskType( TaskType taskType )
        {
            selectedTaskTypeAdded(DomainEvent.CREATE, taskType );
        }

        public void removeSelectedTaskType( TaskType taskType )
        {
            selectedTaskTypeRemoved(DomainEvent.CREATE, taskType );
        }

        public void selectedTaskTypeAdded( DomainEvent event, TaskType taskType )
        {
            selectedTaskTypes().add( taskType );
        }

        public void selectedTaskTypeRemoved( DomainEvent event, TaskType taskType )
        {
            selectedTaskTypes().remove( taskType );
        }

        public ListValue possibleTaskTypes( ManyAssociation<TaskType> taskTypes )
        {
            ListValueBuilder builder = new ListValueBuilder(vbf);

            for (TaskType taskType : taskTypes)
            {
                if (!selectedTaskTypes().contains( taskType ))
                {
                    builder.addDescribable( taskType );
                }
            }

            return builder.newList();
        }
    }
}