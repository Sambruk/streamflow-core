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

package se.streamsource.streamflow.infrastructure.event;

import org.junit.Test;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

/**
 * JAVADOC
 */
public class MemoryEventStoreTest
    extends AbstractQi4jTest
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        new EntityTestAssembler().assemble(module);
        module.addValues(TransactionEvents.class, DomainEvent.class);
        module.addServices( MemoryEventStoreService.class, EventsService.class);
        module.addObjects(getClass());
        module.addEntities(TestEntity.class);
    }

    @Service
    EventStore eventStore;

    @Service
    EventListener listener;

    @Test
    public void testEventStore() throws UnitOfWorkCompletionException
    {
        objectBuilderFactory.newObjectBuilder(MemoryEventStoreTest.class).injectTo((this));

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
    @Mixins(TestEntity.TestMixin.class)
    interface TestEntity
        extends EntityComposite
    {
        void somethingHappened(DomainEvent event, String parameter1);

        abstract class TestMixin
            implements TestEntity
        {
            public void somethingHappened(DomainEvent event, String parameter1)
            {
            }
        }
    }
}
