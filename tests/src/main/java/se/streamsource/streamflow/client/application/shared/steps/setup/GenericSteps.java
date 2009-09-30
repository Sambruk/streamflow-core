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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GenericSteps
        extends Steps
    implements EventListener
{
    public List<DomainEvent> events;

    @Structure
    UnitOfWorkFactory uowf;

    public void setThrowable(Throwable throwable)
    {
        this.throwable = throwable;
    }

    private Throwable throwable;

    @Then("events $commaSeparatedList occurred")
    public void eventsOccured(String commaSeparatedList)
    {
        ensureThat(events != null);
        List<String> expectedEvents = new ArrayList<String>();
        Collections.addAll(expectedEvents, commaSeparatedList.split(","));
        for (DomainEvent event: events)
        {
            if (!expectedEvents.remove(event.name().get()))
            {
                ensureThat(event.name().get(), CoreMatchers.equalTo(""));
            }
        }
        ensureThat(expectedEvents.isEmpty());
    }

    @Then("no events occurred")
    public void noEvents()
    {
        ensureThat(events, CoreMatchers.nullValue());
    }

    @Then("$exceptionName is thrown")
    public void exceptionThrown(String exceptionName)
    {
        ensureThat(throwable, CoreMatchers.notNullValue());
        ensureThat(throwable.getClass().getSimpleName(), equalTo(exceptionName));
        throwable = null;
    }


    @Then("exception is not thrown")
    public void noExceptionThrown()
    {
        ensureThat(throwable, CoreMatchers.nullValue());
    }

    public void notifyEvent(DomainEvent event)
    {
        if (events == null)
        {
            events = new ArrayList<DomainEvent>();
            UnitOfWork uow = uowf.currentUnitOfWork();
            uow.addUnitOfWorkCallback(new UnitOfWorkCallback()
            {

                public void beforeCompletion() throws UnitOfWorkCompletionException
                {
                    events = null;
                }

                public void afterCompletion(UnitOfWorkStatus status) { }
            });
        }

        events.add(event);
    }

    public void clearEvents()
    {
        events = null;
    }
}
