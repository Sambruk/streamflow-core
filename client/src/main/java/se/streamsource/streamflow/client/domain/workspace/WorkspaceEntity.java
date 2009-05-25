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

package se.streamsource.streamflow.client.domain.workspace;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * JAVADOC
 */
@Concerns(WorkspaceEntity.WorkspaceLifecycleConcern.class)
@Mixins(WorkspaceEntity.WorkVisitableMixin.class)
public interface WorkspaceEntity
        extends Workspace, EntityComposite
{
    interface WorkspaceState
    {
        @Aggregated
        Association<Inbox> inbox();

        @Aggregated
        ManyAssociation<Work> work();

        @Aggregated
        ManyAssociation<Context> contexts();
    }

    class WorkVisitableMixin
            implements WorkVisitable
    {
        @This
        WorkspaceState state;

        public void visitWork(WorkVisitor visitor)
        {
            for (Work work : state.work())
            {
                if (work instanceof Task)
                {
                    Task task = (Task) work;
                    visitor.visit(task);
                } else if (work instanceof Project)
                {
                    Project project = (Project) work;
                    visitor.visit(project);
                }
            }
        }
    }

    class WorkspaceLifecycleConcern
            extends ConcernOf<Lifecycle>
            implements Lifecycle
    {
        @Structure
        UnitOfWorkFactory uowf;

        @This
        WorkspaceState state;

        public void create() throws LifecycleException
        {
            // Create new SharedInbox
            Inbox inbox = uowf.currentUnitOfWork().newEntity(Inbox.class);
            state.inbox().set(inbox);
        }

        public void remove() throws LifecycleException
        {
        }
    }
}
