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
package se.streamsource.streamflow.web.management;

import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.isNotNull;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import org.qi4j.api.cache.CacheOptions;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;
import se.streamsource.streamflow.web.domain.structure.caze.History;

/**
 * Handles cleanup after update migration of history to case log.
 */
public class HistoryCleanup
   implements Runnable
{
   @Structure
   Module module;

   Logger log = LoggerFactory.getLogger( HistoryCleanup.class );

   private UnitOfWork findOldHistoryCases = null;
   private UnitOfWork deleteHistoryUow = null;
   private boolean stop = false;
   private long remaining = 0;

   public void run()
   {

      try
      {

         long historyToDeleteCount = 0;

         do
         {

            if( stop )
               return;
            else
            {
               QueryBuilder<CaseEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( CaseEntity.class );
               findOldHistoryCases = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.buildUsecase( "FindOldHistoryCases" ).with( CacheOptions.NEVER ).newUsecase() );
               Query<CaseEntity> query = queryBuilder.where( and(
                     isNotNull( templateFor( History.Data.class ).history() ),
                     isNotNull( templateFor( CaseLoggable.Data.class ).caselog() ) ) )
                     .newQuery( findOldHistoryCases ).maxResults( 1000 );

               remaining = historyToDeleteCount = query.count();


               int count = 0;
               for (CaseEntity caze : query)
               {
                  if (stop)
                     return;
                  if (deleteHistoryUow == null)
                     deleteHistoryUow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.buildUsecase( "Upgrade_1.6.0.0_DeleteHistory" ).with( CacheOptions.NEVER ).newUsecase() );

                  if (caze != null && caze.history().get() != null)
                  {
                     CaseEntity caseToChange = deleteHistoryUow.get( caze );
                     caseToChange.history().get().deleteEntity();
                     caseToChange.history().set( null );
                     count++;
                     remaining--;
                  }

                  if (count % 10 == 0)
                  {
                     log.info( " " + count + " old history entries about to be deleted." );
                     if( deleteHistoryUow != null )
                        deleteHistoryUow.complete();
                     deleteHistoryUow = null;
                     log.info( "Delete succeded." );
                     synchronized (this)
                     {
                        this.wait( 500 );
                     }
                  }
               }

               if (deleteHistoryUow != null)
                  deleteHistoryUow.complete();
               log.info( "Delete of " + count + " history entries done." );
               log.info( +remaining + " remain for delete." );
            }
         } while (historyToDeleteCount != 0 && !stop);
         findOldHistoryCases = null;
         log.info( "HistoryCleanup done." );
      } catch (Throwable e)
      {
         log.error( e.getMessage() );

         throw new RuntimeException( "Upgrade migration cleanup failed!", e );
      }
   }

   public void stopAndDiscard()
   {
      stop = true;
      try
      {
         if (deleteHistoryUow != null)
         {
            deleteHistoryUow.discard();
            deleteHistoryUow = null;
         }

         if (findOldHistoryCases != null)
         {
            findOldHistoryCases = null;
         }
      } catch (Throwable e)
      {
         log.error( "stopAndDiscard encountered problems.", e );
      }
      log.info( "HistoryCleanup stopped and discarded with " + remaining + " cases left to cleanup." );
   }
}
