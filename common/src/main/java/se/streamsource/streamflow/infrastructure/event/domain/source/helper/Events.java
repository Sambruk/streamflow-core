/*
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

package se.streamsource.streamflow.infrastructure.event.domain.source.helper;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.specification.Specification;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayer;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionVisitor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.qi4j.api.util.Classes.methodsOf;
import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.map;

/**
 * Helper methods for working with Iterables of DomainEvents and TransactionDomainEvents.
 */
public class Events
{
   public static Iterable<DomainEvent> events( Iterable<TransactionDomainEvents> transactions )
   {
      List<Iterable<DomainEvent>> events = new ArrayList<Iterable<DomainEvent>>();
      for (TransactionDomainEvents transactionDomain : transactions)
      {
         events.add( transactionDomain.events().get() );
      }

      Iterable<DomainEvent>[] iterables = (Iterable<DomainEvent>[]) new Iterable[events.size()];
      return Iterables.<DomainEvent>flatten( events.<Iterable<DomainEvent>>toArray( iterables ) );
   }

   public static Iterable<DomainEvent> events( TransactionDomainEvents... transactionDomains )
   {
      List<Iterable<DomainEvent>> events = new ArrayList<Iterable<DomainEvent>>();
      for (TransactionDomainEvents transactionDomain : transactionDomains)
      {
         events.add( transactionDomain.events().get() );
      }

      Iterable<DomainEvent>[] iterables = (Iterable<DomainEvent>[]) new Iterable[events.size()];
      return Iterables.<DomainEvent>flatten( events.<Iterable<DomainEvent>>toArray( iterables ) );
   }

   public static TransactionVisitor adapter( final EventVisitor eventVisitor )
   {
      return new TransactionVisitor()
      {
         public boolean visit( TransactionDomainEvents transactionDomain )
         {
            for (DomainEvent domainEvent : transactionDomain.events().get())
            {
               if (!eventVisitor.visit( domainEvent ))
                  return false;
            }
            return true;
         }
      };
   }

   public static boolean matches( Specification<DomainEvent> specification, Iterable<TransactionDomainEvents> transactions )
   {
      return filter( specification, events( transactions ) ).iterator().hasNext();
   }

   // Common specifications

   public static Specification<DomainEvent> withNames( final Iterable<String> names )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            for (String name : names)
            {
               if (event.name().get().equals( name ))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<DomainEvent> withNames( final String... names )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            for (String name : names)
            {
               if (event.name().get().equals( name ))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<DomainEvent> withNames( final Class eventClass )
   {
      return Events.withNames( map(new Function<Method, String>()
         {
            public String map( Method method )
            {
               return method.getName();
            }
         }, methodsOf(eventClass )));
   }

   public static Specification<DomainEvent> afterDate( final Date afterDate )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            return event.on().get().after( afterDate );
         }
      };
   }

   public static Specification<DomainEvent> beforeDate( final Date beforeDate )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            return event.on().get().before( beforeDate );
         }
      };
   }

   public static Specification<DomainEvent> withUsecases( final String... names )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            for (String name : names)
            {
               if (event.usecase().get().equals( name ))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<DomainEvent> onEntities( final String... entities )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            for (String entity : entities)
            {
               if (event.entity().get().equals( entity ))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<DomainEvent> onEntities( final Iterable<? extends LinkValue> links )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            for (LinkValue link : links)
            {
               if (event.entity().get().equals( link.id().get() ))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<DomainEvent> onEntityTypes( final String... entityTypes )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            for (String entityType : entityTypes)
            {
               if (event.entityType().get().equals( entityType ))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<DomainEvent> by( final String... by )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            for (String user : by)
            {
               if (event.by().get().equals( user ))
                  return true;
            }
            return false;
         }
      };
   }

   public static Specification<DomainEvent> paramIs( final String name, final String value )
   {
      return new Specification<DomainEvent>()
      {
         public boolean satisfiedBy( DomainEvent event )
         {
            return EventParameters.getParameter( event, name ).equals( value );
         }
      };
   }

   public static EventVisitor playEvents( final DomainEventPlayer player, final Object eventHandler, final UnitOfWorkFactory uowf, final Usecase usecase )
   {
      return new EventVisitor()
      {
         public boolean visit( DomainEvent event )
         {
            UnitOfWork uow = uowf.newUnitOfWork( usecase );
            try
            {
               player.playEvent( event, eventHandler );
               uow.complete();
               return true;
            } catch (Exception e)
            {
               uow.discard();
               
               return false;
            }
         }
      };
   }
}
