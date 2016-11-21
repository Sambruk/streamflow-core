/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.structure.organization;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;

import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.casetype.PriorityOnCase;

/**
 * Contains priority definitions.
 */
@Concerns( {Priorities.RemovePriorityConcern.class} )
@Mixins( {Priorities.Mixin.class} )
public interface Priorities
{
   Priority createPriority();

   void addPriority( Priority priority );
   
   boolean removePriority( Priority priority );

   void changePriorityOrder( Priority priority, int direction );
   
   interface Data
   {
      @Aggregated
      ManyAssociation<Priority> prioritys();
   }

   interface Event
   {
      Priority createdPriority( @Optional DomainEvent event, String id );
      void addedPriority( @Optional DomainEvent event, Priority priority );
      void removedPriority( @Optional DomainEvent event, Priority priority );
   }

   abstract class Mixin
      implements Priorities, Event
   {
      @This
      Data data;

      @This
      Event event;
      
      @Structure
      Module module;

      @Service
      IdentityGenerator idgen;

      public Priority createPriority()
      {

         Priority priority = createdPriority( null, idgen.generate( Identity.class ) );
         addPriority( priority );

         return priority;
      }

      public void addPriority( Priority priority )
      {
         if( !data.prioritys().contains( priority ) )
            event.addedPriority( null, priority );
      }

      public boolean removePriority( Priority priority )
      {
         if (data.prioritys().contains( priority ))
         {

            event.removedPriority( null, priority );
            priority.removeEntity();
            return true;
         } else
            return false;
      }

      public void changePriorityOrder( final Priority priority, final int direction )
      {
         // check bounds first - we may not move priorities outside the priority range
         Integer oldPriority = ((PrioritySettings.Data)priority).priority().get();
         int movingTo = oldPriority.intValue() + direction;
         if( movingTo >= 0 && movingTo <= (data.prioritys().count() - 1) )
         {
            Priority move = Iterables.first( Iterables.filter( new Specification<Priority>()
            {
               public boolean satisfiedBy( Priority item )
               {
                  return ((PrioritySettings.Data)item).priority().get().compareTo( new Integer( ((PrioritySettings.Data)priority).priority().get().intValue() + direction ) ) == 0;
               }
            }, data.prioritys().toList() ) );
            priority.changePriority( ((PrioritySettings.Data)move).priority().get() );
            move.changePriority( new Integer( ((PrioritySettings.Data)move).priority().get().intValue() + (direction * -1) ) );
         }
      }

      public Priority createdPriority( @Optional DomainEvent event, String id )
      {
         EntityBuilder<Priority> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Priority.class, id );
         builder.instanceFor( PrioritySettings.Data.class ).priority().set( data.prioritys().count() );
         return builder.newInstance();
      }
   }

   abstract class RemovePriorityConcern
      extends ConcernOf<Priorities>
      implements Priorities
   {
      @Structure
      Module module;

      @This
      Data data;

      public boolean removePriority(Priority priority)
      {
         Query<PriorityOnCase> query = module.queryBuilderFactory().newQueryBuilder( PriorityOnCase.class )
               .where( eq(
                     templateFor( PriorityOnCase.Data.class ).defaultPriority(),
                     priority ) )
               .newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

         if( query.count() > 0 )
            throw new IllegalStateException( ErrorResources.priority_remove_failed_default_exist.toString() );

         final Integer removedPriority = ((PrioritySettings.Data)priority).priority().get();
         boolean result = next.removePriority( priority );

         Iterable<Priority> updatePriorities= Iterables.filter( new Specification<Priority>()
         {
            public boolean satisfiedBy( Priority item )
            {
               return removedPriority.compareTo( ((PrioritySettings.Data)item).priority().get() ) == -1;
            }
         }, data.prioritys().toList() );

         for( Priority movePrio : updatePriorities )
         {
            movePrio.changePriority( ((PrioritySettings.Data)movePrio).priority().get() - 1 );
         }

         return result;
      }
   }
}
