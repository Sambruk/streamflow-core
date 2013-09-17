/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Service;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
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
   extends ConcernOf<CaseCommandsContext>
   implements CaseCommandsContext
{
   Caching caching;

   public void init(@Optional @Service CachingService cache)
   {
      caching = new Caching(cache, Caches.CASECOUNTS);
   }

   public void assign()
   {

      next.assign();
      
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      // Update inbox cache
      caching.addToCaseCountCache( caze.owner().get().toString(), -1 );
      // Update assignments for user
      Assignee assignee = roleMap.get( Assignee.class );
      caching.addToCaseCountCache(caze.owner().get().toString()+":"+assignee.toString(), 1 );
      
      if (caze.isUnread()) {
         caching.addToUnreadCache( caze.owner().get().toString(), -1 );
         caching.addToUnreadCache(caze.owner().get().toString()+":"+assignee.toString(), 1 );
      }

   }

   public void open()
   {
      next.open();
      
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      // Update inbox cache
      caching.addToCaseCountCache( caze.owner().get().toString(), 1 );
      // Update drafts for user
      caching.addToCaseCountCache( caze.createdBy().get().toString(), -1 );
    
      if (caze.isUnread()) {
         caching.addToUnreadCache( caze.owner().get().toString(), 1 );
         caching.addToUnreadCache( caze.createdBy().get().toString(), -1 );
      }

    

   }

   public void close()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );
      boolean assigned = caze.isAssigned();

      next.close();
      
      if (assigned)
      {
         // Update assignments for user
         Assignee assignee = roleMap.get( Assignee.class );
         caching.addToCaseCountCache( caze.owner().get().toString()+":"+assignee.toString(), -1 );
         if (caze.isUnread()) {
            caching.addToUnreadCache( caze.owner().get().toString()+":"+assignee.toString(), -1 );
         }
      } else
      {
         // Update inbox cache
         caching.addToCaseCountCache( caze.owner().get().toString(),-1 );
         if (caze.isUnread()) {
            caching.addToUnreadCache( caze.owner().get().toString(),-1 );
         }
      }

   }

   public void resolve( EntityValue resolution )
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );
      boolean assigned = caze.isAssigned();
      
      next.resolve(resolution);

      if (assigned)
      {
         // Update assignments for user
         Assignee assignee = roleMap.get( Assignee.class );
         caching.addToCaseCountCache( caze.owner().get().toString()+":"+assignee.toString() , -1 );
         if (caze.isUnread()) {
            caching.addToUnreadCache( caze.owner().get().toString()+":"+assignee.toString() , -1 );
         }
      } else
      {
         // Update inbox cache
         caching.addToCaseCountCache( caze.owner().get().toString(), -1 );
         if (caze.isUnread()) {
            caching.addToUnreadCache( caze.owner().get().toString(), -1 );
         }
      }

   }

   public void formonclose()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );
      boolean assigned = caze.isAssigned();
      next.formonclose();
      
      if (assigned)
      {
         // Update assignments for user
         Assignee assignee = roleMap.get( Assignee.class );
         caching.addToCaseCountCache( caze.owner().get().toString()+":"+assignee.toString() , -1 );
         if (caze.isUnread()) {
            caching.addToUnreadCache( caze.owner().get().toString()+":"+assignee.toString() , -1 );
         }
      } else
      {
         // Update inbox cache
         caching.addToCaseCountCache( caze.owner().get().toString(), -1 );
         if (caze.isUnread()) {
            caching.addToUnreadCache( caze.owner().get().toString(), -1 );
         }
      }
   }

   public void sendto( EntityValue entity )
   {
      
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );
      Owner oldOwner = caze.owner().get();
      boolean assigned = caze.isAssigned();
      
      next.sendto(entity);
      
      // If status DRAFT - no cache to fix since case is moved by open command
      if ( !CaseStates.DRAFT.equals( caze.status().get() ))
      {
         if (assigned)
         {
            // Update assignments for user
            Assignee assignee = roleMap.get( Assignee.class );
            caching.addToCaseCountCache( oldOwner.toString()+":"+assignee.toString(), -1 );
            if(caze.isUnread()) {
               caching.addToUnreadCache( oldOwner.toString()+":"+assignee.toString(), -1 );
            }
         } else
         {
            // Update inbox cache
            caching.addToCaseCountCache( oldOwner.toString(), -1 );
            if (caze.isUnread()) {
               caching.addToUnreadCache( oldOwner.toString(), -1 );
            }
         }

         // Update inbox cache on receiving end
         caching.addToCaseCountCache( caze.owner().get().toString(), 1 );
         if (caze.isUnread()) {
            caching.addToUnreadCache( caze.owner().get().toString(), 1 );
         }
      }
   }

   public void reopen()
   {
      next.reopen();
      
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      // Update assignments for user
      Assignee assignee = roleMap.get( Assignee.class );
      caching.addToCaseCountCache( caze.owner().get().toString()+":"+assignee.toString(), 1 );
      if (caze.isUnread()){
         caching.addToUnreadCache( caze.owner().get().toString()+":"+assignee.toString(), 1 );
      }
   }

   public void unassign()
   {
      next.unassign();
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      // Update assignments for user
      Assignee assignee = roleMap.get( Assignee.class );
      caching.addToCaseCountCache( caze.owner().get().toString()+":"+assignee.toString(), -1 );
      // Update inbox cache
      caching.addToCaseCountCache( caze.owner().get().toString(), 1 );
      if (caze.isUnread()){
         caching.addToUnreadCache( caze.owner().get().toString()+":"+assignee.toString(), -1 );
         caching.addToUnreadCache( caze.owner().get().toString(), 1 );
      }
   }

   public void delete()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );
      
      boolean assigned = caze.isAssigned();

      next.delete();

      if (caze.hasOwner() && !CaseStates.DRAFT.equals( caze.status().get() ) )
      {
         if (assigned)
         {
            // Update assignments for user
            Assignee assignee = roleMap.get( Assignee.class );
            caching.addToCaseCountCache( caze.owner().get().toString()+":"+assignee.toString(), -1 );
            if (caze.isUnread()){
               caching.addToUnreadCache( caze.owner().get().toString()+":"+assignee.toString(), -1 );
            }
         } else
         {
            // Update inbox cache
            caching.addToCaseCountCache( caze.owner().get().toString(), -1 );
            if (caze.isUnread()){
               caching.addToUnreadCache( caze.owner().get().toString(), -1 );
            }
         }
      } else
      {
         // Update drafts for user
         caching.addToCaseCountCache( caze.createdBy().get().toString(), -1 );
      }
   }
   
   public void reinstate()
   {
      RoleMap roleMap = RoleMap.current();
      CaseEntity caze = roleMap.get( CaseEntity.class );

      boolean assigned = caze.isAssigned();
      next.reinstate();
      
      if (caze.hasOwner() && !CaseStates.DRAFT.equals( caze.status().get() ) )
      {
         if (assigned)
         {
            // Update assignments for user
            Assignee assignee = roleMap.get( Assignee.class );
            caching.addToCaseCountCache( caze.owner().get().toString()+":"+assignee.toString(), 1 );
            if (caze.isUnread()) {
               caching.addToUnreadCache( caze.owner().get().toString()+":"+assignee.toString(), 1 );
            }
         } else
         {
            // Update inbox cache
            caching.addToCaseCountCache( caze.owner().get().toString(), 1 );
            if(caze.isUnread()){
               caching.addToUnreadCache( caze.owner().get().toString(), 1 );
            }
         }
      } else
      {
         // Update drafts for user
         caching.addToCaseCountCache( caze.createdBy().get().toString(), 1 );
      }
   }
   
   public void read()
   {
      next.read();
      CaseEntity caze = role( CaseEntity.class );

      if (!caze.isUnread())
      {
         if (caze.isAssigned())
         {
            // Update assignments for user
            Assignee assignee = role( Assignee.class );
            caching.addToUnreadCache( caze.owner().get().toString() + ":" + assignee.toString(), -1 );
         } else
         {
            // Update inbox cache
            caching.addToUnreadCache( caze.owner().get().toString(), -1 );
         }
      }
   }

   public void markunread()
   {
      next.markunread();
      CaseEntity caze = role( CaseEntity.class );

      if (caze.isAssigned())
      {
         // Update assignments for user
         Assignee assignee = role( Assignee.class );
         caching.addToUnreadCache( caze.owner().get().toString() + ":" + assignee.toString(), 1 );
      } else
      {
         // Update inbox cache
         caching.addToUnreadCache( caze.owner().get().toString(), 1 );
      }
   }

   public void markread() {
      next.markread();
      CaseEntity caze = role( CaseEntity.class );

      if (caze.isAssigned())
      {
         // Update assignments for user
         Assignee assignee = role( Assignee.class );
         caching.addToUnreadCache( caze.owner().get().toString() + ":" + assignee.toString(), -1 );
      } else
      {
         // Update inbox cache
         caching.addToUnreadCache( caze.owner().get().toString(), -1 );
      }
   }
}
