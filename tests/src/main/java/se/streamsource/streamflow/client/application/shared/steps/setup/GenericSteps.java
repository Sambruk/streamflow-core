/**
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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
package se.streamsource.streamflow.client.application.shared.steps.setup;

import org.hamcrest.CoreMatchers;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.junit.Assert;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.MemoryEventStoreService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.jbehave.Ensure.*;


public class GenericSteps
      extends Steps
{
   @Structure
   UnitOfWorkFactory uowf;

   @Service
   MemoryEventStoreService eventService;

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

      ensureThat( throwable, CoreMatchers.nullValue() );

      List<DomainEvent> events = eventService.getEvents();
      if (events == null)
         events = Collections.emptyList();
      String eventNames = "";
      String comma = "";
      for (DomainEvent event : events)
      {
         eventNames += comma + event.name().get();
         comma = ",";
      }
      ensureThat( eventNames, CoreMatchers.equalTo( expectedEvents ) );

      clearEvents();
   }

   @Then("no events occurred")
   public void noEvents()
   {
      ensureThat( eventService.getEvents(), CoreMatchers.nullValue() );
   }

   @Then("$exceptionName is thrown")
   public void exceptionThrown( String exceptionName )
   {
      ensureThat( throwable, CoreMatchers.notNullValue() );
      ensureThat( throwable.getClass().getSimpleName(), equalTo( exceptionName ) );
      throwable = null;
   }


   @Then("exception is not thrown")
   public void noExceptionThrown()
   {
      ensureThat( throwable, CoreMatchers.nullValue() );
   }

   @When("events are cleared")
   public void clearEvents()
   {
      eventService.clearEvents();
   }
}
