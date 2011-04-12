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

package se.streamsource.streamflow.web.context;

import org.junit.*;
import org.qi4j.api.*;
import org.qi4j.api.composite.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.object.*;
import org.qi4j.api.query.*;
import org.qi4j.api.service.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.*;
import org.qi4j.spi.structure.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;
import se.streamsource.streamflow.test.*;

import java.util.*;

/**
 * Base class for roleMap tests. RoleMap tests should subclass this test and use the fluent API provided
 * by this class to do testing of contexts.
 */
public abstract class ContextTest
{
   protected static Qi4j api;
   protected static Qi4jSPI spi;

   protected static Energy4Java qi4j;
   protected static ApplicationModelSPI applicationModel;
   protected static ApplicationSPI application;

   protected static TransientBuilderFactory transientBuilderFactory;
   protected static ObjectBuilderFactory objectBuilderFactory;
   protected static ValueBuilderFactory valueBuilderFactory;
   protected static UnitOfWorkFactory unitOfWorkFactory;
   protected static QueryBuilderFactory queryBuilderFactory;
   protected static ServiceFinder serviceLocator;

   protected static ModuleSPI moduleInstance;

   private static EventCollector eventCollector = new EventCollector();

   @BeforeClass
   public static void setUp()
         throws Exception
   {
      qi4j = new Energy4Java();
      applicationModel = newApplication();
      if (applicationModel == null)
      {
         // An AssemblyException has occurred that the Test wants to check for.
         return;
      }
      application = applicationModel.newInstance( qi4j.spi() );
      api = spi = qi4j.spi();
      application.activate();

      // Use the Context/Context module
      moduleInstance = (ModuleSPI) application.findModule( "Context", "Context" );
      transientBuilderFactory = moduleInstance.transientBuilderFactory();
      objectBuilderFactory = moduleInstance.objectBuilderFactory();
      valueBuilderFactory = moduleInstance.valueBuilderFactory();
      unitOfWorkFactory = moduleInstance.unitOfWorkFactory();
      queryBuilderFactory = moduleInstance.queryBuilderFactory();
      serviceLocator = moduleInstance.serviceFinder();
   }

   /**
    * This method is called when there was an AssemblyException in the creation of the Qi4j application model.
    * <p>Override this method to catch valid failures to place into test suites.
    *
    * @param exception the exception thrown.
    * @throws AssemblyException The default implementation of this method will simply re-throw the exception.
    */
   protected static void assemblyException( AssemblyException exception )
         throws AssemblyException
   {
      throw exception;
   }

   @AfterClass
   public static void tearDown()
         throws Exception
   {
      if (unitOfWorkFactory != null && unitOfWorkFactory.currentUnitOfWork() != null)
      {
         UnitOfWork current;
         while ((current = unitOfWorkFactory.currentUnitOfWork()) != null)
         {
            if (current.isOpen())
            {
               current.discard();
            } else
            {
               throw new InternalError( "I have seen a case where a UoW is on the stack, but not opened." );
            }
         }

         new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
      }

      if (application != null)
      {
         application.passivate();
      }

      // Set static variables to null to avoid leaks
      api = null;
      spi = null;

      qi4j = null;
      applicationModel = null;
      application = null;

      transientBuilderFactory = null;
      objectBuilderFactory = null;
      valueBuilderFactory = null;
      unitOfWorkFactory = null;
      queryBuilderFactory = null;
      serviceLocator = null;

      moduleInstance = null;

   }

   @Before
   public void setupRoleMap()
   {
      RoleMap.setCurrentRoleMap( new RoleMap() );
   }

   @After
   public void clearRoleMap()
   {
      RoleMap.clearCurrentRoleMap();
   }

   protected static ApplicationModelSPI newApplication() throws AssemblyException
   {

      ApplicationAssembler assembler = new StreamflowWebContextTestAssembler( Events.adapter( eventCollector ) );

      try
      {
         return qi4j.newApplicationModel( assembler );
      }
      catch (AssemblyException e)
      {
         assemblyException( e );
         return null;
      }
   }

   // Helper methods

   protected static <T> T context( Class<T> contextClass )
   {
      if (TransientComposite.class.isAssignableFrom( contextClass ))
         return transientBuilderFactory.newTransient( contextClass );
      else
         return objectBuilderFactory.newObject( contextClass );
   }

   protected static <T> T playRole( T oldEntity )
   {
      if (oldEntity == null)
         throw new NullPointerException( "No entity used to play role" );

      T entity = unitOfWorkFactory.currentUnitOfWork().get( oldEntity );
      RoleMap.current().set( entity );
      return entity;
   }

   protected static <T> T playRole( Class<T> roleClass, String id )
   {
      T entity = unitOfWorkFactory.currentUnitOfWork().get( roleClass, id );
      RoleMap.current().set( entity );
      return entity;
   }

   protected static <T> T playRole( Class<T> roleClass, LinkValue link )
   {
      T entity = unitOfWorkFactory.currentUnitOfWork().get( roleClass, link.id().get() );
      RoleMap.current().set( entity );
      return entity;
   }

   protected static <T> T value( Class<T> valueClass, String json )
   {
      return valueBuilderFactory.newValueFromJSON( valueClass, json );
   }

   protected static StringValue stringValue( String value )
   {
      return value( StringValue.class, "{'string':'" + value + "'}" );
   }

   protected static EntityValue entityValue( String value )
   {
      return value( EntityValue.class, "{'entity':'" + value + "'}" );
   }

   protected static EntityValue entityValue( LinkValue link )
   {
      return value( EntityValue.class, "{'entity':'" + link.id().get() + "'}" );
   }

   protected static <T> T entity( LinkValue link )
   {
      return (T) unitOfWorkFactory.currentUnitOfWork().get( EntityComposite.class, link.id().get() );
   }

   protected static boolean valueContains( ValueComposite value, String jsonFragment )
   {
      return value.toJSON().contains( jsonFragment );
   }

   protected static void eventsOccurred( String... expectedEvents )
   {
      List<DomainEvent> events = eventCollector.events();
      if (events == null)
         events = Collections.emptyList();
      List<String> eventNames = new ArrayList<String>();
      for (DomainEvent event : events)
      {
         eventNames.add( event.name().get() );
      }
//      Assert.assertThat( eventNames.toArray( new String[eventNames.size()] ), CoreMatchers.equalTo( expectedEvents ) );

      clearEvents();
   }

   protected static void clearEvents()
   {
      eventCollector.events().clear();
   }

   protected static LinkValue findLink( LinksValue links, String name )
   {
      String names = null;
      for (LinkValue linkValue : links.links().get())
      {
         if (linkValue.text().get().equals( name ))
         {
            return linkValue;
         } else
         {
            if (names == null)
               names = linkValue.text().get();
            else
               names += "," + linkValue.text().get();
         }
      }

      if (names == null)
         throw new IllegalArgumentException( "No link found named " + name + ". List was empty" );
      else
         throw new IllegalArgumentException( "No link found named " + name + ". Available names:" + names );
   }

   protected static <T> T findDescribable( Iterable<T> iterable, String name )
   {
      String names = null;
      for (T item : iterable)
      {
         String itemName = ((Describable) item).getDescription();
         if (itemName.equals( name ))
         {
            return item;
         } else
         {
            if (names == null)
               names = itemName;
            else
               names += "," + itemName;
         }
      }

      if (names == null)
         throw new IllegalArgumentException( "No link found named " + name + ". List was empty" );
      else
         throw new IllegalArgumentException( "No link found named " + name + ". Available names:" + names );
   }


}
