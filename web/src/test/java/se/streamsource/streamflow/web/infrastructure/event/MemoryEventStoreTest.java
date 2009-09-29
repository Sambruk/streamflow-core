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

package se.streamsource.streamflow.web.infrastructure.event;

import org.junit.Test;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventCreationConcern;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.EventNotificationSideEffect;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

/**
 * JAVADOC
 */
public class MemoryEventStoreTest
    extends AbstractQi4jTest
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addServices(MemoryEventStoreService.class);
        module.addObjects(getClass());
    }

    @Override
    protected void initApplication(Application app) throws Exception
    {
        objectBuilderFactory.newObjectBuilder(MemoryEventStoreTest.class).injectTo((this));
    }

    @Service
    EventStore eventStore;

    @Service
    EventSource source;

    @Service
    EventListener listener;

    @Test
    public void testEventStore() throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        TestEntity entity = uow.newEntity(TestEntity.class);
        entity.somethingHappened(DomainEvent.CREATE, "foo");
        uow.complete();

        Iterable<TransactionEvents> transactions = eventStore.events(null, Integer.MAX_VALUE);

        for (TransactionEvents transaction : transactions)
        {
            System.out.println(transaction.toJSON());
        }
    }

    @Concerns(EventCreationConcern.class)
    @SideEffects(EventNotificationSideEffect.class)
    interface TestEntity
    {
        void somethingHappened(DomainEvent event, String parameter1);
    }
}
