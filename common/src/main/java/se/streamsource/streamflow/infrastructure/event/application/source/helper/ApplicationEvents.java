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

package se.streamsource.streamflow.infrastructure.event.application.source.helper;

import org.qi4j.api.io.*;
import org.qi4j.api.specification.*;
import org.qi4j.api.util.*;
import se.streamsource.streamflow.infrastructure.event.application.*;
import se.streamsource.streamflow.infrastructure.event.application.replay.*;

import java.lang.reflect.*;
import java.util.*;

import static org.qi4j.api.util.Classes.*;

/**
 * Helper methods for working with Iterables of DomainEvents and TransactionDomainEvents.
 */
public class ApplicationEvents
{
   public static Iterable<ApplicationEvent> events(Iterable<TransactionApplicationEvents> transactions)
   {
      List<Iterable<ApplicationEvent>> events = new ArrayList<Iterable<ApplicationEvent>>();
      for (TransactionApplicationEvents transactionDomain : transactions)
      {
         events.add(transactionDomain.events().get());
      }

      Iterable<ApplicationEvent>[] iterables = (Iterable<ApplicationEvent>[]) new Iterable[events.size()];
      return Iterables.<ApplicationEvent>flatten(events.<Iterable<ApplicationEvent>>toArray(iterables));
   }

   public static Iterable<ApplicationEvent> events(TransactionApplicationEvents... transactionDomains)
   {
      List<Iterable<ApplicationEvent>> events = new ArrayList<Iterable<ApplicationEvent>>();
      for (TransactionApplicationEvents transactionDomain : transactionDomains)
      {
         events.add(transactionDomain.events().get());
      }

      Iterable<ApplicationEvent>[] iterables = (Iterable<ApplicationEvent>[]) new Iterable[events.size()];
      return Iterables.<ApplicationEvent>flatten(events.<Iterable<ApplicationEvent>>toArray(iterables));
   }

   public static boolean matches(Specification<ApplicationEvent> specification, Iterable<TransactionApplicationEvents> transactions)
   {
      return Iterables.filter(specification, events(transactions)).iterator().hasNext();
   }

   // Common specifications

   public static Specification<ApplicationEvent> withNames(final Iterable<String> names)
   {
      return new Specification<ApplicationEvent>()
      {
         public boolean satisfiedBy(ApplicationEvent event)
         {
            for (String name : names)
            {
               if (event.name().get().equals(name))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<ApplicationEvent> withNames(final String... names)
   {
      return new Specification<ApplicationEvent>()
      {
         public boolean satisfiedBy(ApplicationEvent event)
         {
            for (String name : names)
            {
               if (event.name().get().equals(name))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<ApplicationEvent> withNames(final Class eventClass)
   {
      return ApplicationEvents.withNames(Iterables.map(new Function<Method, String>()
      {
         public String map(Method method)
         {
            return method.getName();
         }
      }, methodsOf(eventClass)));
   }

   public static Specification<ApplicationEvent> afterDate(final Date afterDate)
   {
      return new Specification<ApplicationEvent>()
      {
         public boolean satisfiedBy(ApplicationEvent event)
         {
            return event.on().get().after(afterDate);
         }
      };
   }

   public static Specification<ApplicationEvent> beforeDate(final Date beforeDate)
   {
      return new Specification<ApplicationEvent>()
      {
         public boolean satisfiedBy(ApplicationEvent event)
         {
            return event.on().get().before(beforeDate);
         }
      };
   }

   public static Specification<ApplicationEvent> withUsecases(final String... names)
   {
      return new Specification<ApplicationEvent>()
      {
         public boolean satisfiedBy(ApplicationEvent event)
         {
            for (String name : names)
            {
               if (event.usecase().get().equals(name))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<ApplicationEvent> paramIs(final String name, final String value)
   {
      return new Specification<ApplicationEvent>()
      {
         public boolean satisfiedBy(ApplicationEvent event)
         {
            return ApplicationEventParameters.getParameter(event, name).equals(value);
         }
      };
   }

   public static Output<TransactionApplicationEvents, ApplicationEventReplayException> playEvents(final ApplicationEventPlayer player, final Object eventHandler)
   {
      final Specification<ApplicationEvent> specification = ApplicationEvents.withNames(eventHandler.getClass());

      return new Output<TransactionApplicationEvents, ApplicationEventReplayException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom(Sender<? extends TransactionApplicationEvents, SenderThrowableType> sender) throws ApplicationEventReplayException, SenderThrowableType
         {
            sender.sendTo(new Receiver<TransactionApplicationEvents, ApplicationEventReplayException>()
            {
               public void receive(TransactionApplicationEvents item) throws ApplicationEventReplayException
               {
                  for (ApplicationEvent applicationEvent : events(item))
                  {
                     if (specification.satisfiedBy(applicationEvent))
                        player.playEvent(applicationEvent, eventHandler);
                  }
               }
            });
         }
      };
   }
}
