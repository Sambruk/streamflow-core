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

import junit.framework.Assert;
import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * JAVADOC
 */
public class EventPropertyChangeMixinTest
        extends AbstractQi4jTest
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addValues(DomainEvent.class, TransactionEvents.class);
        module.addEntities(TestEntity.class);
        module.addServices( EventsService.class, MemoryEventStoreService.class );
        new EntityTestAssembler().assemble(module);
    }

    @Test
    public void testEventMixin()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        TestEntity entity = uow.newEntity(TestEntity.class);
        entity.changeFoo("New foo");
        Assert.assertEquals("New foo", entity.foo().get());
        entity.changeFoo("New erfoo");
        Assert.assertEquals("New erfoo", entity.foo().get());
        uow.discard();
    }

    interface TestDomain
    {
        void changeFoo(String foo);

        interface TestDomainState
        {
            @UseDefaults
            Property<String> foo();

            void fooChanged(DomainEvent event, String newFoo);
        }
    }

    @Concerns(EventCreationConcern.class)
    @SideEffects(EventNotificationSideEffect.class)
    @Mixins({EventPropertyChangedMixin.class, CommandPropertyChangeMixin.class})
    interface TestEntity
            extends TestDomain, TestDomain.TestDomainState, EntityComposite
    {
    }
}
