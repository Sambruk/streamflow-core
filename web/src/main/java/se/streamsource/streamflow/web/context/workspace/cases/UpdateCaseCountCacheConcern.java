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

package se.streamsource.streamflow.web.context.workspace.cases;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

/**
 * Updates the cache of casecounts.
 */
public abstract class UpdateCaseCountCacheConcern
   extends ConcernOf<CaseActionsContext>
   implements CaseActionsContext
{
   Caching caching;

   @Structure
   UnitOfWorkFactory uowf;

   public void init(@Optional @Service CachingService cache)
   {
      caching = new Caching(cache, Caches.CASECOUNTS);
   }

   public void assign()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      // Update inbox cache
      caching.addToCache( caze.owner().get().toString(), -1 );

      // Update assignments for user
      Assignee assignee = roleMap.get( Assignee.class );
      caching.addToCache(caze.owner().get().toString()+":"+assignee.toString(), 1 );

      next.assign();
   }

   public void open()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      // Update inbox cache
      caching.addToCache( caze.owner().get().toString(), 1 );

      // Update drafts for user
      caching.addToCache( caze.createdBy().get().toString(), -1 );

      next.open();
   }

   public void close()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      if (caze.isAssigned())
      {
         // Update assignments for user
         Assignee assignee = roleMap.get( Assignee.class );
         caching.addToCache( caze.owner().get().toString()+":"+assignee.toString(), -1 );
      } else
      {
         // Update inbox cache
         caching.addToCache( caze.owner().get().toString(),-1 );
      }

      next.close();
   }

   public void resolve( EntityValue resolution )
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      if (caze.isAssigned())
      {
         // Update assignments for user
         Assignee assignee = roleMap.get( Assignee.class );
         caching.addToCache( caze.owner().get().toString()+":"+assignee.toString() , -1 );
      } else
      {
         // Update inbox cache
         caching.addToCache( caze.owner().get().toString(), -1 );
      }

      next.resolve(resolution);
   }

   public void sendto( EntityValue entity )
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      Owner owner = caze.owner().get();
      if (owner != null) // If no owner, then it is still in drafts mode - no cache to fix
      {
         if (caze.isAssigned())
         {
            // Update assignments for user
            Assignee assignee = roleMap.get( Assignee.class );
            caching.addToCache( owner.toString()+":"+assignee.toString(), -1 );
         } else
         {
            // Update inbox cache
            caching.addToCache( owner.toString(), -1 );
         }

         // Update inbox cache on receiving end
         caching.addToCache( entity.entity().get(), 1 );
      }

      next.sendto(entity);
   }

   public void reopen()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      // Update assignments for user
      Assignee assignee = roleMap.get( Assignee.class );
      caching.addToCache( caze.owner().get().toString()+":"+assignee.toString(), 1 );

      next.reopen();
   }

   public void unassign()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      // Update assignments for user
      Assignee assignee = roleMap.get( Assignee.class );
      caching.addToCache( caze.owner().get().toString()+":"+assignee.toString(), -1 );

      // Update inbox cache
      caching.addToCache( caze.owner().get().toString(), 1 );

      next.unassign();
   }

   public void delete()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      if (caze.hasOwner())
      {
         if (caze.isAssigned())
         {
            // Update assignments for user
            Assignee assignee = roleMap.get( Assignee.class );
            caching.addToCache( caze.owner().get().toString()+":"+assignee.toString(), -1 );
         } else
         {
            // Update inbox cache
            caching.addToCache( caze.owner().get().toString(), -1 );
         }
      } else
      {
         // Update drafts for user
         caching.addToCache( caze.createdBy().get().toString(), -1 );
      }

      next.delete();
   }
}