/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.web.domain.structure.casetype.PriorityOnCase;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * Contains priority definitions.
 */
@Concerns( {Priorities.RemovePriorityConcern.class} )
@Mixins( Priorities.Mixin.class )
public interface Priorities
{
   public Priority createPriority();
   
   public boolean removePriority( Priority priority );

   public void changePriorityOrder( Priority priority, int direction );
   
   interface Data
   {
      ManyAssociation<Priority> prioritys();
   }

   abstract class Mixin
      implements Priorities, Data
   {
      @This
      Data data;
      
      @Structure
      Module module;

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
                     templateFor( PriorityOnCase.Data.class ).priorityDefault(),
                     priority ) )
               .newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

         if( query.count() > 0 )
            throw new IllegalStateException( ErrorResources.priority_remove_failed_default_exist.toString() );

         return next.removePriority( priority );
      }
   }
}
