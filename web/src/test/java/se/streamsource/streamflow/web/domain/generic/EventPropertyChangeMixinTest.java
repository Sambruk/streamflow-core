/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.generic;

import junit.framework.Assert;
import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventCreationConcern;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.factory.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.time.Time;
import se.streamsource.streamflow.infrastructure.time.TimeService;
import se.streamsource.streamflow.web.infrastructure.event.MemoryEventStoreService;

/**
 * JAVADOC
 */
public class EventPropertyChangeMixinTest
      extends AbstractQi4jTest
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addValues( DomainEvent.class, TransactionEvents.class );
      module.addEntities( TestEntity.class );
      module.addObjects( TimeService.class );
      module.importServices( Time.class ).importedBy( NewObjectImporter.class );
      module.addServices( DomainEventFactoryService.class, MemoryEventStoreService.class );
      new EntityTestAssembler().assemble( module );
   }

   @Test
   public void testEventMixin()
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      TestEntity entity = uow.newEntity( TestEntity.class );
      entity.changeFoo( "New foo" );
      Assert.assertEquals( "New foo", entity.foo().get() );
      entity.changeFoo( "New erfoo" );
      Assert.assertEquals( "New erfoo", entity.foo().get() );
      uow.discard();
   }

   interface TestDomain
   {
      void changeFoo( String foo );

      interface TestDomainState
      {
         @UseDefaults
         Property<String> foo();

         void changedFoo( DomainEvent event, String newFoo );
      }
   }

   @Concerns(EventCreationConcern.class)
   @Mixins({EventPropertyChangedMixin.class, CommandPropertyChangeMixin.class})
   interface TestEntity
         extends TestDomain, TestDomain.TestDomainState, EntityComposite
   {
   }
}
