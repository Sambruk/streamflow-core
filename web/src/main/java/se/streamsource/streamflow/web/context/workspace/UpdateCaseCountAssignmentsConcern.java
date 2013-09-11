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
package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Service;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

/**
 * Increase case count when creating new case in assignments
 */
public abstract class UpdateCaseCountAssignmentsConcern
        extends ConcernOf<AssignmentsContext>
        implements AssignmentsContext
{
   @Optional
   @Service
   CachingService caching;

   public void createcase()
   {
      next.createcase();

      Owner owner = RoleMap.role(Owner.class);
      Assignee assignee = RoleMap.role(Assignee.class);

      // Update assignments for assignee
      new Caching(caching, Caches.CASECOUNTS).addToCaseCountCache(owner.toString() + ":" + assignee.toString(), 1);
      new Caching(caching, Caches.CASECOUNTS).addToUnreadCache(owner.toString() + ":" + assignee.toString(), 1);
   }
}
