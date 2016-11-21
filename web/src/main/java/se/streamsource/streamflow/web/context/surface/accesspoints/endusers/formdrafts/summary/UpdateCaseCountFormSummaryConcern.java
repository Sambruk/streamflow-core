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
package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Service;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

/**
 * Update case counts when submitted from Surface
 */
public abstract class UpdateCaseCountFormSummaryConcern
      extends ConcernOf<SurfaceSummaryContext>
      implements SurfaceSummaryContext
{
   @Optional
   @Service
   CachingService caching;

   public void submitandsend()
   {
      next.submitandsend();

      CaseEntity caze = RoleMap.role( CaseEntity.class );

      // Update project inbox cache
      new Caching( caching, Caches.CASECOUNTS ).addToCaseCountCache( caze.owner().get().toString(), 1 );
      new Caching( caching, Caches.CASECOUNTS ).addToUnreadCache( caze.owner().get().toString(), 1 );
   }
}