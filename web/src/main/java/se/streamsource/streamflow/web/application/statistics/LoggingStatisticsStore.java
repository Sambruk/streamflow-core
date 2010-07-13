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

package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC
 */
@Mixins(LoggingStatisticsStore.Mixin.class)
public interface LoggingStatisticsStore
   extends ServiceComposite, StatisticsStore
{
   class Mixin
      implements Activatable, StatisticsStore
   {
      private Logger log;

      public void activate() throws Exception
      {
         log = LoggerFactory.getLogger( "statistics" );
      }

      public void passivate() throws Exception
      {
      }

      public void related( RelatedStatisticsValue related )
      {
         log.info( "id:{}, description:{}, type:{}", new Object[]{related.identity().get(), related.description().get(), related.type().get()} );
      }

      public void caseStatistics( CaseStatisticsValue caseStatistics )
      {
         // TODO Add all fields to log message

         StringBuilder str = new StringBuilder();
         str.append( "id:" ).append( caseStatistics.identity().get() );
         str.append( "caseId:" ).append( caseStatistics.caseId().get() );
         str.append( "description:" ).append( caseStatistics.description().get() );
         str.append( "note:" ).append( caseStatistics.note().get() );
         str.append( "createdOn:" ).append( caseStatistics.createdOn().get() );
         str.append( "closedOn:" ).append( caseStatistics.closedOn().get() );

         log.info( str.toString() );
      }

      public void clearAll()
      {
      }
   }
}
