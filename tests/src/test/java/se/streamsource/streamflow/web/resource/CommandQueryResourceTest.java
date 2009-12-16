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

package se.streamsource.streamflow.web.resource;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.DomainEventFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.MemoryEventStoreService;
import se.streamsource.streamflow.infrastructure.event.TimeService;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.TransactionEventAdapter;
import se.streamsource.streamflow.infrastructure.event.source.TransactionHandler;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.application.security.AccessPolicy;
import se.streamsource.streamflow.web.infrastructure.event.CommandEventListenerService;
import se.streamsource.streamflow.web.infrastructure.event.EventSourceService;
import se.streamsource.streamflow.web.rest.ResourceFinder;

import java.io.IOException;
import java.io.Writer;
import java.security.AccessControlContext;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * JAVADOC
 */
public class CommandQueryResourceTest
      extends AbstractQi4jTest
{
   Component component;
   public Context context;
   public Client restlet;

   public void assemble( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      new EntityTestAssembler().assemble( moduleAssembly );
      moduleAssembly.addServices( CommandEventListenerService.class, MemoryEventStoreService.class, EventSourceService.class, DomainEventFactoryService.class );
      moduleAssembly.importServices( TransactionHandler.class );
      moduleAssembly.importServices( AccessPolicy.class, TimeService.class ).importedBy( NewObjectImporter.class );
      moduleAssembly.addObjects( TimeService.class, ResourceFinder.class, TestTransactionHandler.class, TestAccessPolicy.class, CommandQueryClient.class, TestServerResource.class );
      moduleAssembly.addValues( StringDTO.class, TransactionEvents.class, DomainEvent.class );
      moduleAssembly.addEntities( TestEntity.class );

      moduleAssembly.layerAssembly().applicationAssembly().setMetaInfo( new TestTransactionHandler() );
   }

   @Before
   public void init() throws Exception
   {
      ResourceFinder finder = objectBuilderFactory.newObject( ResourceFinder.class );
      finder.setTargetClass( TestServerResource.class );

      component = new Component();
      component.getServers().add( Protocol.HTTP, 8888 );
      component.getDefaultHost().attach( "/test", finder );
      component.start();

      context = new Context();
      restlet = new Client( context, Protocol.HTTP );

   }

   @After
   public void shutdown() throws Exception
   {
      if (component != null)
         component.stop();
   }

   @Test
   public void testValueQuery() throws ResourceException
   {
      CommandQueryClient clientResource = objectBuilderFactory.newObjectBuilder( CommandQueryClient.class ).use( new Client(Protocol.HTTP), new Reference( "http://localhost:8888/test" ) ).newInstance();

      StringDTO dto = clientResource.query( "testQuery", StringDTO.class );

      Assert.assertThat( "Test", CoreMatchers.equalTo( dto.string().get() ) );
   }

   @Test
   public void testRepresentationCommand() throws ResourceException
   {
      CommandQueryClient clientResource = objectBuilderFactory.newObjectBuilder( CommandQueryClient.class ).use( new Client(Protocol.HTTP), new Reference( "http://localhost:8888/test" ) ).newInstance();

      Representation rep = new WriterRepresentation( MediaType.TEXT_PLAIN )
      {
         public void write( Writer writer ) throws IOException
         {
            writer.write( "Test" );
         }
      };
      clientResource.postCommand( "testCommandStream", rep );
   }

   public static class TestServerResource
         extends CommandQueryServerResource
   {
      @Structure
      ValueBuilderFactory vbf;

      @Service
      EventListener listener;

      @Service
      DomainEventFactory def;

      @Structure
      UnitOfWorkFactory uowf;

      // Queries

      public StringDTO testQuery()
      {
         return vbf.newValueFromJSON( StringDTO.class, "{\"string\":\"Test\"}" );
      }

      // Commands

      public void testCommandStream( Representation representation ) throws ResourceException, IOException
      {
         String text = representation.getText();
         System.out.println( text );
         TestEntity test = uowf.currentUnitOfWork().newEntity( TestEntity.class );
         listener.notifyEvent( def.createEvent( test, "testCommandStream", new Object[0] ) );
      }
   }

   public static class TestTransactionHandler
         implements TransactionHandler
   {
      public boolean handleTransaction( TransactionEvents transaction )
      {
         new TransactionEventAdapter( new EventHandler()
         {
            public boolean handleEvent( DomainEvent event )
            {
               System.out.println( event );
               return true;
            }
         } ).handleTransaction( transaction );
         return true;
      }
   }

   public static class TestAccessPolicy
         implements AccessPolicy
   {
      public AccessControlContext getAccessControlContext( List<Principal> subject, Object securedObject )
      {
         PermissionCollection permissions = null;
         permissions = new AllPermission().newPermissionCollection();
         permissions.add( new AllPermission() );

         Principal[] principals = new Principal[]{};
         ProtectionDomain[] domains = new ProtectionDomain[]{new ProtectionDomain( null, permissions, securedObject.getClass().getClassLoader(), principals )};


         return new AccessControlContext( domains );
      }
   }

   public interface TestEntity
         extends EntityComposite
   {

   }
}
