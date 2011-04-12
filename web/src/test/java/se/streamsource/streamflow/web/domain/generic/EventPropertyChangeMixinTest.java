/**
 *
 * Copyright 2009-2011 Streamsource AB
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
import org.qi4j.api.common.*;
import org.qi4j.api.concern.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.property.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.service.importer.*;
import org.qi4j.test.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.factory.*;
import se.streamsource.streamflow.infrastructure.time.*;
import se.streamsource.streamflow.web.infrastructure.event.*;

/**
 * JAVADOC
 */
public class EventPropertyChangeMixinTest
      extends AbstractQi4jTest
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.values( DomainEvent.class, TransactionDomainEvents.class );
      module.entities( TestEntity.class );
      module.objects( TimeService.class );
      module.importedServices( Time.class ).importedBy( NewObjectImporter.class );
      module.services( DomainEventFactoryService.class, MemoryEventStoreService.class );
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

         void changedFoo( @Optional DomainEvent event, String newFoo );
      }
   }

   @Concerns(EventCreationConcern.class)
   @Mixins({EventPropertyChangedMixin.class, CommandPropertyChangeMixin.class})
   interface TestEntity
         extends TestDomain, TestDomain.TestDomainState, EntityComposite
   {
   }
}
