/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.CaseContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

/**
 * Update case counts when submitted from Surface
 */
public abstract class UpdateCaseCountFormSummaryConcern
   extends ConcernOf<SummaryContext>
   implements SummaryContext
{
   @Optional
   @Service
   CachingService caching;

   @Structure
   UnitOfWorkFactory uowf;

   public void submitandsend()
   {
      next.submitandsend();

      RoleMap roleMap = uowf.currentUnitOfWork().metaInfo().get( RoleMap.class );
      CaseEntity caze = roleMap.get( CaseEntity.class );

      // Update project inbox cache
      new Caching(caching, Caches.CASECOUNTS).addToCache( caze.owner().get().toString(), 1 );
   }
}