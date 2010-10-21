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

package se.streamsource.streamflow.web.context;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationModelSPI;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.ModuleSPI;
import org.restlet.data.Reference;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventCollector;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.test.StreamflowWebContextTestAssembler;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
      if (TransientComposite.class.isAssignableFrom( contextClass))
         return transientBuilderFactory.newTransient( contextClass );
      else
         return objectBuilderFactory.newObject( contextClass );
   }

   protected static <T> T playRole(T oldEntity)
   {
      if (oldEntity == null)
         throw new NullPointerException("No entity used to play role");

      T entity = unitOfWorkFactory.currentUnitOfWork().get( oldEntity );
      RoleMap.current().set( entity );
      return entity;
   }

   protected static <T> T playRole(Class<T> roleClass, String id)
   {
      T entity = unitOfWorkFactory.currentUnitOfWork().get( roleClass, id );
      RoleMap.current().set( entity );
      return entity;
   }

   protected static <T> T playRole(Class<T> roleClass, LinkValue link)
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
      return value( StringValue.class, "{'string':'"+value+"'}");
   }

   protected static EntityValue entityValue( String value )
   {
      return value( EntityValue.class, "{'entity':'"+value+"'}");
   }

   protected static EntityValue entityValue( LinkValue link )
   {
      return value( EntityValue.class, "{'entity':'"+link.id().get()+"'}");
   }

   protected static boolean valueContains( ValueComposite value, String jsonFragment)
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
      Assert.assertThat( eventNames.toArray( new String[eventNames.size()] ), CoreMatchers.equalTo( expectedEvents ) );

      clearEvents();
   }

   protected static void clearEvents()
   {
      eventCollector.events().clear();
   }

   protected static Reference reference(String ref)
   {
      Reference baseRef = new Reference( ref );
      return new Reference(baseRef, baseRef.getPath());
   }

   protected static Actor user(String name)
   {
      return unitOfWorkFactory.currentUnitOfWork().get( Actor.class, name );
   }

   protected static Locale language(String language)
   {
      return new Locale(language);
   }

   protected static <T> T subContext( SubContexts subContexts, String name )
   {
      if (subContexts instanceof IndexContext)
      {
         IndexContext<LinksValue> index = (IndexContext<LinksValue>) subContexts;
         LinkValue link = findLink(index.index(), name);
         return (T)subContexts.context( link.id().get() );
      } else
      {
         return (T) subContexts.context( name );
      }
   }

   protected static LinkValue findLink( LinksValue links, String name )
   {
      String names = null;
      for (LinkValue linkValue : links.links().get())
      {
         if (linkValue.text().get().equals(name))
         {
            return linkValue;
         } else
         {
            if (names == null)
               names = linkValue.text().get();
            else
               names+=","+linkValue.text().get();
         }
      }

      if (names == null)
         throw new IllegalArgumentException("No link found named "+name+". List was empty");
      else
         throw new IllegalArgumentException("No link found named "+name+". Available names:"+names);
   }

   protected static <T> T findDescribable( Iterable<T> iterable , String name )
   {
      String names = null;
      for (T item : iterable)
      {
         String itemName = ((Describable)item).getDescription();
         if (itemName.equals(name))
         {
            return item;
         } else
         {
            if (names == null)
               names = itemName;
            else
               names+=","+itemName;
         }
      }

      if (names == null)
         throw new IllegalArgumentException("No link found named "+name+". List was empty");
      else
         throw new IllegalArgumentException("No link found named "+name+". Available names:"+names);
   }


}
