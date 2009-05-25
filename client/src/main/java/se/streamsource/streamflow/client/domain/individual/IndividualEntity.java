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

package se.streamsource.streamflow.client.domain.individual;

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
import se.streamsource.streamflow.client.domain.workspace.WorkVisitable;
import se.streamsource.streamflow.client.domain.workspace.Workspace;

/**
 * JAVADOC
 */
@Concerns(IndividualEntity.LifecycleConcern.class)
@Mixins({
        IndividualEntity.WorkVisitableMixin.class})
public interface IndividualEntity
        extends Individual, EntityComposite
{
    interface IndividualState
    {
        @Aggregated
        ManyAssociation<Account> accounts();

        @Aggregated
        Association<Workspace> workspace();
    }

    class WorkVisitableMixin
            implements WorkVisitable
    {
        @This
        IndividualState state;

        public void visitWork(WorkVisitor visitor)
        {
            state.workspace().get().visitWork(visitor);
        }
    }

    class LifecycleConcern
            extends ConcernOf<Lifecycle>
            implements Lifecycle
    {
        @Structure
        UnitOfWorkFactory uowf;
        @This
        IndividualState state;

        public void create() throws LifecycleException
        {
            // Create new Workspace
            Workspace workspace = uowf.currentUnitOfWork().newEntity(Workspace.class);
            state.workspace().set(workspace);
        }

        public void remove() throws LifecycleException
        {
        }
    }
}
