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

package se.streamsource.streamflow.web.infrastructure.event;

import org.hamcrest.*;
import org.junit.*;
import org.qi4j.api.common.*;
import org.qi4j.api.concern.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.service.importer.*;
import org.qi4j.test.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.factory.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;
import se.streamsource.streamflow.infrastructure.time.*;

/**
 * JAVADOC
 */
public abstract class AbstractEventStoreTest
      extends AbstractQi4jTest
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.layer().application().setMode( Application.Mode.test );

      new EntityTestAssembler().assemble( module );
      module.values( TransactionDomainEvents.class, DomainEvent.class );
      module.services( DomainEventFactoryService.class );
      module.objects( getClass(), TimeService.class );
      module.entities( TestEntity.class );
      module.importedServices( TimeService.class ).importedBy( NewObjectImporter.class );
   }

   @Service
   EventSource eventSource;

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
      entity.somethingHappened( null, "foo" );
      uow.complete();
   }

   @Test
   public void getAllEvents() throws UnitOfWorkCompletionException
   {
      final int[] count = new int[1];

      eventSource.transactionsAfter( 0, new TransactionVisitor()
      {
         public boolean visit( TransactionDomainEvents transactionDomain )
         {
            count[0]++;
            System.out.println( transactionDomain.toJSON() );

            return true;
         }
      } );

      Assert.assertThat( count[0], CoreMatchers.equalTo( 10 ) );
   }

   @Test
   public void getHalfOfEvents() throws UnitOfWorkCompletionException
   {
      final int[] count = new int[1];

      eventSource.transactionsAfter( 0, new TransactionVisitor()
      {
         public boolean visit( TransactionDomainEvents transactionDomain )
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
      eventSource.transactionsAfter( 0, timestamp = new TransactionTimestampFilter( 0, new TransactionVisitor()
      {
         int count = 0;

         public boolean visit( TransactionDomainEvents transactionDomain )
         {
            count++;

            return count < 5;
         }
      } ) );

      final long lastTimeStamp = timestamp.lastTimestamp();

      final int[] count = new int[1];
      eventSource.transactionsAfter( lastTimeStamp, new TransactionVisitor()
      {
         public boolean visit( TransactionDomainEvents transactionDomain )
         {
            Assert.assertThat( transactionDomain.timestamp().get(), CoreMatchers.not( lastTimeStamp ) );

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
      void somethingHappened( @Optional DomainEvent event, String parameter1 );

      abstract class TestMixin
            implements TestEntity
      {
         public void somethingHappened( DomainEvent event, String parameter1 )
         {
         }
      }
   }
}