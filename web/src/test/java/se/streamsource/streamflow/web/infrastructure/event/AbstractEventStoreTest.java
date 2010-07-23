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

package se.streamsource.streamflow.web.infrastructure.event;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.event.EventCreationConcern;
import se.streamsource.streamflow.infrastructure.event.TimeService;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.TransactionTimestampFilter;

/**
 * JAVADOC
 */
public abstract class AbstractEventStoreTest
      extends AbstractQi4jTest
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.layerAssembly().applicationAssembly().setMode( Application.Mode.test );

      new EntityTestAssembler().assemble( module );
      module.addValues( TransactionEvents.class, DomainEvent.class );
      module.addServices( DomainEventFactoryService.class );
      module.addObjects( getClass(), TimeService.class );
      module.addEntities( TestEntity.class );
      module.importServices( TimeService.class ).importedBy( NewObjectImporter.class );
   }

   @Service
   EventStore eventStore;

   @Before
   public void initStore() throws UnitOfWorkCompletionException
   {
      objectBuilderFactory.newObjectBuilder( AbstractEventStoreTest.class ).injectTo( (this) );


      for (int i = 0; i < 10; i++)
         addEvent();
   }

   protected void addEvent()
         throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      TestEntity entity = uow.newEntity( TestEntity.class );
      entity.somethingHappened( DomainEvent.CREATE, "foo" );
      uow.complete();
   }

   @Test
   public void getAllEvents() throws UnitOfWorkCompletionException
   {
      final int[] count = new int[1];

      eventStore.transactionsAfter( 0, new TransactionVisitor()
      {
         public boolean visit( TransactionEvents transaction )
         {
            count[0]++;
            System.out.println( transaction.toJSON() );

            return true;
         }
      } );

      Assert.assertThat( count[0], CoreMatchers.equalTo( 10 ) );
   }

   @Test
   public void getHalfOfEvents() throws UnitOfWorkCompletionException
   {
      final int[] count = new int[1];

      eventStore.transactionsAfter( 0, new TransactionVisitor()
      {
         public boolean visit( TransactionEvents transaction )
         {
            count[0]++;

            return count[0] < 5;
         }
      } );

      Assert.assertThat( count[0], CoreMatchers.equalTo( 5 ) );
   }

   @Test
   public void getEventsAfterDate()
   {
      TransactionTimestampFilter timestamp;
      eventStore.transactionsAfter( 0, timestamp = new TransactionTimestampFilter( 0, new TransactionVisitor()
      {
         int count = 0;

         public boolean visit( TransactionEvents transaction )
         {
            count++;

            return count < 5;
         }
      } ) );

      final long lastTimeStamp = timestamp.lastTimestamp();

      final int[] count = new int[1];
      eventStore.transactionsAfter( lastTimeStamp, new TransactionVisitor()
      {
         public boolean visit( TransactionEvents transaction )
         {
            Assert.assertThat( transaction.timestamp().get(), CoreMatchers.not( lastTimeStamp ) );

            count[0]++;

            return true;
         }
      } );

      Assert.assertThat( count[0], CoreMatchers.equalTo( 5 ) );
   }

   @Concerns(EventCreationConcern.class)
   @Mixins(TestEntity.TestMixin.class)
   public interface TestEntity
         extends EntityComposite
   {
      void somethingHappened( DomainEvent event, String parameter1 );

      abstract class TestMixin
            implements TestEntity
      {
         public void somethingHappened( DomainEvent event, String parameter1 )
         {
         }
      }
   }
}