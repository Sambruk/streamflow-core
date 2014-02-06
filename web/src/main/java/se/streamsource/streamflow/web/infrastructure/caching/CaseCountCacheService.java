/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.infrastructure.caching;

import net.sf.ehcache.Element;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.user.EndUser;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * Service wrapper CaseCount cache.
 */
@Mixins(CaseCountCacheService.Mixin.class)
public interface CaseCountCacheService
   extends ServiceComposite, Activatable
{
   class Mixin
      implements Activatable
   {

      @Structure
      Module module;
      
      @Service
      CachingService caching;
      
      public void activate() throws Exception
      {
         caching.manager().clearAll();
         
         initCache();
      }

      public void passivate() throws Exception
      {
         
      }
      
      private void initCache() {

         Caching caching = new Caching( this.caching, Caches.CASECOUNTS );

         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "CaseCountCacheInit" ) );
         
         // Find all Open cases
         QueryBuilder<Case> queryBuilder = module.queryBuilderFactory().newQueryBuilder(Case.class);
         queryBuilder = queryBuilder.where(and(
                 eq(templateFor(Status.Data.class).status(), CaseStates.OPEN),
                 eq( templateFor(Removable.Data.class).removed(), Boolean.FALSE )
         ));
         for (Case caze : queryBuilder.newQuery( uow ))
         {
            CaseEntity aCase = (CaseEntity) caze;
            try
            {
               aCase.owner().get().toString();
            } catch ( ClassCastException cc )
            {
               System.out.println( aCase.caseId().get() );
               continue;
            }
            String key = aCase.owner().get().toString();
            String assignee = (aCase.assignedTo().get() != null) ? aCase.assignedTo().get().toString() : null;
            
            if (assignee != null) {
               key = key + ":" + assignee;
            }

            CaseCountItem caseCountItem;
            Element element = caching.get( key );
            if (element == null)
            {
               caseCountItem = new CaseCountItem();
            } else
            {
               caseCountItem = (CaseCountItem) element.getObjectValue();
            }
            caseCountItem.addToCount( 1 );
            if (aCase.hasUnreadConversation() || aCase.hasUnreadForm() || aCase.isUnread())
            {
               caseCountItem.addToUnread( 1 );
            }
            caching.put( new Element( key, caseCountItem ) );
         }

         // Find all drafts
         queryBuilder = module.queryBuilderFactory().newQueryBuilder( Case.class );
         queryBuilder = queryBuilder.where(
               QueryExpressions.eq( templateFor( Status.Data.class ).status(), CaseStates.DRAFT ));

         for (Case caze : queryBuilder.newQuery( uow ))
         {
            CaseEntity aCase = (CaseEntity) caze;
            if( aCase.createdBy().get() instanceof EndUser )
               continue;
            String key = aCase.createdBy().get().toString();

            CaseCountItem caseCountItem;
            Element element = caching.get( key );
            if (element == null)
            {
               caseCountItem = new CaseCountItem();
            } else
            {
               caseCountItem = (CaseCountItem) element.getObjectValue();
            }
            caseCountItem.addToCount( 1 );
            caching.put( new Element( key, caseCountItem ) );
         }
      }
   }
}
