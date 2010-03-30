/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.test;

import org.hamcrest.CoreMatchers;
import org.jbehave.Ensure;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.junit.Assert;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventCollector;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.MemoryEventStoreService;
import se.streamsource.streamflow.infrastructure.event.source.TransactionEventAdapter;
import se.streamsource.streamflow.web.context.RootContext;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.InteractionContext;
import se.streamsource.dci.context.SubContexts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.*;


public class GenericSteps
      extends Steps
{
   EventCollector eventCollector;

   InteractionContext context = new InteractionContext();

   Object current;

   Object result;

   @Structure
   UnitOfWorkFactory uowf;

   @Service
   MemoryEventStoreService eventService;

   @Structure
   TransientBuilderFactory tbf;

   @Structure
   ValueBuilderFactory vbf;
   public Object previous;

   public void init( @Service EventSource eventSource )
   {
      eventSource.registerListener( new TransactionEventAdapter(eventCollector = new EventCollector()));
   }

   public void setThrowable( Throwable throwable )
   {
      this.throwable = throwable;
   }

   private Throwable throwable;

   @Then("events $commaSeparatedList occurred")
   public void eventsOccured( String expectedEvents )
   {
      if (throwable != null)
      {
         throwable.printStackTrace();
         Assert.fail( "Exception was thrown" );
      }

      Ensure.ensureThat( throwable, CoreMatchers.nullValue() );

      List<DomainEvent> events = eventCollector.events();
      if (events == null)
         events = Collections.emptyList();
      String eventNames = "";
      String comma = "";
      for (DomainEvent event : events)
      {
         eventNames += comma + event.name().get();
         comma = ",";
      }
      Ensure.ensureThat( eventNames, CoreMatchers.equalTo( expectedEvents ) );

      clearEvents();
   }

   @Then("no events occurred")
   public void noEvents()
   {
      Ensure.ensureThat( eventService.getEvents(), CoreMatchers.nullValue() );
   }

   @Then("$exceptionName is thrown")
   public void exceptionThrown( String exceptionName )
   {
      Ensure.ensureThat( throwable, CoreMatchers.notNullValue() );
      Ensure.ensureThat( throwable.getClass().getSimpleName(), equalTo( exceptionName ) );
      throwable = null;
   }


   @Then("exception is not thrown")
   public void noExceptionThrown()
   {
      Ensure.ensureThat( throwable, CoreMatchers.nullValue() );
   }

   @When("events are cleared")
   public void clearEvents()
   {
      eventCollector.events().clear();;
   }

   // Context steps -------------------------------------------------
   @Given("language $lang")
   public void givenLanguage( String lang )
   {
      context.playRoles( new Locale(lang) );
   }

   @Given("root context")
   public void givenRootContext()
   {
      current = tbf.newTransientBuilder( RootContext.class ).use( context ).newInstance();
   }

   @Given("subcontext $name")
   public void givenSubContext( String name ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
   {
      if (current instanceof SubContexts)
      {
         SubContexts subContexts = (SubContexts) current;
         current = subContexts.context( name );
      } else
      {
         Method contextMethod = current.getClass().getMethod( name );
         current = (Context) contextMethod.invoke( current );
      }
   }

   @Given("previous context")
   public void givenPreviousContext()
   {
      current = (Context) previous;
   }

   @Given("context for link nr $index")
   public void givenLinkWithIndex(int index)
   {
      LinkValue link = ((LinksValue)result).links().get().get( index );
      current = ((SubContexts)current).context( link.id().get() );
   }

   @Given("context for link named $name")
   public void givenLinkNamed(String name)
   {
      for (LinkValue linkValue : ((LinksValue) result).links().get())
      {
         if (linkValue.text().get().equals(name))
         {
            current = linkValue;
            break;
         }
      }

      throw new IllegalArgumentException("No link found named "+name);
   }

   @When("query $name with $parameters")
   public void whenQuery( String name, String parameters ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
   {
      Method queryMethod = getMethod( name );
      Object parameter = vbf.newValueFromJSON( queryMethod.getParameterTypes()[0], parameters );
      result = queryMethod.invoke( current, parameter );
   }

   @When("query $name without parameters")
   public void whenQuery( String name ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
   {
      Method queryMethod = getMethod( name );
      result = queryMethod.invoke( current );
   }

   @When("command $name with $parameters")
   public void whenCommand( String name, String parameters ) throws InvocationTargetException, IllegalAccessException, UnitOfWorkCompletionException
   {
      Method commandMethod = getMethod( name );

      Object parameter = vbf.newValueFromJSON( commandMethod.getParameterTypes()[0], parameters );
      commandMethod.invoke( current, parameter );
      uowf.currentUnitOfWork().apply();
   }

   @When("command $name without parameters")
   public void whenCommand( String name ) throws InvocationTargetException, IllegalAccessException, UnitOfWorkCompletionException
   {
      Method commandMethod = getMethod( name );
      previous = commandMethod.invoke( current );
      uowf.currentUnitOfWork().apply();
   }

   @Then("result is $result")
   public void thenResult( String result )
   {
      if (!this.result.toString().equals(result))
         throw new IllegalArgumentException( this.result.toString() + " was not " + result );
   }

   @Then("result contains $result")
   public void thenResultContains( String result )
   {
      if (this.result.toString().indexOf( result ) == -1)
         throw new IllegalArgumentException( this.result.toString() + " does not contain " + result );
   }

   private Method getMethod( String name )
   {
      for (Method method : current.getClass().getMethods())
      {
         if (method.getName().equals( name ))
            return method;
      }
      throw new IllegalArgumentException( "No method called " + name + " found in " + current.getClass().getName() );
   }
}
