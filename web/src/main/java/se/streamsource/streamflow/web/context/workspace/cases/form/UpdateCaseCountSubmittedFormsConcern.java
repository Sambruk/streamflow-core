/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.context.workspace.cases.form;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Service;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

/**
 * Updates the cache of casecounts.
 */
public abstract class UpdateCaseCountSubmittedFormsConcern extends ConcernOf<CaseSubmittedFormsContext> implements
      CaseSubmittedFormsContext
{

   Caching caching;

   public void init(@Optional @Service CachingService cache)
   {
      caching = new Caching( cache, Caches.CASECOUNTS );
   }

   public void read(int index)
   {
      CaseEntity caze = RoleMap.role( CaseEntity.class );

      boolean unreadFromStart = caze.isUnread();

      next.read( index );

      if (unreadFromStart && !caze.isUnread())
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
}
