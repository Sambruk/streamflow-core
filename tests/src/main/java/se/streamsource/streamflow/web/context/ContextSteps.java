/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.InteractionContext;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContexts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * JAVADOC
 */
public class ContextSteps
      extends Steps
{
   InteractionContext context = new InteractionContext();

   @Structure
   TransientBuilderFactory tbf;

   @Structure
   ValueBuilderFactory vbf;

   Context current;

   Object result;

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

   @When("interaction $name with $parameters")
   public void whenInteraction( String name, String parameters ) throws InvocationTargetException, IllegalAccessException
   {
      Method commandMethod = getMethod( name );

      Object parameter = vbf.newValueFromJSON( commandMethod.getParameterTypes()[0], parameters );
      commandMethod.invoke( current, parameter );
   }

   @When("interaction $name without parameters")
   public void whenInteraction( String name ) throws InvocationTargetException, IllegalAccessException
   {
      Method commandMethod = getMethod( name );
      commandMethod.invoke( current );
   }

   @Then("result is $result")
   public void thenResult( String result )
   {
      if (!result.toString().equals(result))
         throw new IllegalArgumentException( result.toString() + " was not " + result );
   }

   @Then("result contains $result")
   public void thenResultContains( String result )
   {
      if (result.toString().indexOf( result ) == -1)
         throw new IllegalArgumentException( result.toString() + " does not contain " + result );
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
