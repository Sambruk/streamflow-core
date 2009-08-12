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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
public class EventTest
    extends AbstractQi4jTest
{
    @Service
    EventReplay replayer;

    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addObjects(EventTest.class);
        module.addEntities(TestEntity.class);
        module.addValues(DomainEvent.class);
        module.addServices(MemoryEntityStoreService.class, EventRecorderService.class, UuidIdentityGeneratorService.class, FileConfiguration.class);
    }

    @Test
    public void testRecord() throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        TestEntity entity = uow.newEntity(TestEntity.class, "123");
        entity.doStuff("Foo");
        uow.complete();
    }

    @Test
    public void testReplay() throws Exception
    {
        {
            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
            TestEntity entity = uow.newEntity(TestEntity.class, "123");
            uow.complete();
        }

        {
            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
            Object entity = uow.get(Object.class, "123");
            uow.complete();
        }

        objectBuilderFactory.newObjectBuilder(EventTest.class).injectTo(this);

        String json = "{\"identity\":\"cf905e2e-41ea-4a42-8d52-fa9caa9ec326-0\",\"by\":\"anonymous\",\"entity\":\"123\",\"entityType\":\"se.streamsource.streamflow.web.infrastructure.event.TestEntity\",\"name\":\"stuffDone\",\"on\":\"2009-08-12T15:27:56.365Z\",\"parameters\":\"{\\\"param1\\\":\\\"Foo\\\"}\"}";

        DomainEvent event = valueBuilderFactory.newValueFromJSON(DomainEvent.class, json);

        replayer.replayEvent(event);
    }

}
