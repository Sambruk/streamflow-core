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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Reference;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventCollector;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.web.context.RootContext;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;


public class GenericSteps
      extends Steps
{
   EventCollector eventCollector;

   Context context = new Context();

   Object current;

   Object result;

   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   TransientBuilderFactory tbf;

   @Structure
   ValueBuilderFactory vbf;

   public void init( @Service EventSource eventSource, @Uses EventCollector eventCollector )
   {
      this.eventCollector = eventCollector;
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
   }

   @Then("no events occurred")
   public void noEvents()
   {
      Ensure.ensureThat( eventCollector.events().isEmpty(), CoreMatchers.is( true ) );
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

   // Interactions steps -------------------------------------------------
   @Given("language $lang")
   public void givenLanguage( String lang )
   {
      context.set( new Locale(lang) );
   }

   @Given("reference $ref")
   public void givenReference( String ref )
   {
      Reference baseRef = new Reference( ref );
      context.set( new Reference(baseRef, baseRef.getPath()) );
   }

   @Given("user $name")
   public void givenUser(String name)
   {
      Actor actor = uowf.currentUnitOfWork().get( Actor.class, name );
      context.set( actor );
   }

   @Given("root context")
   public void givenRootContext()
   {
      context = new Context();
      current = tbf.newTransientBuilder( RootContext.class ).use( context ).newInstance();
   }

   @Given("subcontext $name")
   public void givenSubContext( String name ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
   {
      for (String path : name.split( "/" ))
      {
         if (current instanceof SubContexts)
         {
            SubContexts subContexts = (SubContexts) current;

            if (current instanceof IndexInteraction)
            {
               IndexInteraction index = (IndexInteraction) current;
               LinksValue links = (LinksValue) index.index();
               LinkValue link = findLink( path, links );
               path = link.id().get();
            }

            current = subContexts.context( path );
         } else
         {
            Method contextMethod = current.getClass().getMethod( path );
            current = (Interactions) contextMethod.invoke( current );
         }
      }
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
      LinkValue linkValue = findLink( name, ((LinksValue) result) );
      current = ((SubContexts)current).context( linkValue.id().get() );
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
      uowf.currentUnitOfWork().complete();
   }

   @When("command $name without parameters")
   public void whenCommand( String name ) throws InvocationTargetException, IllegalAccessException, UnitOfWorkCompletionException
   {
      Method commandMethod = getMethod( name );
      commandMethod.invoke( current );
      uowf.currentUnitOfWork().complete();
   }

   @When("link command $name with $link")
   public void whenCommandWithLinkNamed(String name, String link) throws InvocationTargetException, UnitOfWorkCompletionException, IllegalAccessException
   {
      LinkValue linkValue = findLink( link, ((LinksValue) result) );

      whenCommand( name, "{\"entity\":\""+linkValue.id().get()+"\"}" );
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
      throw new IllegalArgumentException( "No method called " + name + " found in " + current.getClass().getInterfaces()[0].getName() );
   }

   private LinkValue findLink( String name, LinksValue links )
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
}
