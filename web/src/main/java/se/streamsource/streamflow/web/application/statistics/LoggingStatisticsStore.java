/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.Qi4jSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log all statistics information
 */
@Mixins(LoggingStatisticsStore.Mixin.class)
public interface LoggingStatisticsStore
   extends ServiceComposite, StatisticsStore
{
   class Mixin
      implements Activatable, StatisticsStore
   {
      private Logger log;

      @Structure
      private Qi4jSPI qi4j;

      public void activate() throws Exception
      {
         log = LoggerFactory.getLogger( "statistics" );
      }

      public void passivate() throws Exception
      {
      }

      public void related( RelatedStatisticsValue related )
      {
         log.info( "id:{}, description:{}, type:{}", new Object[]{related.identity().get(), related.description().get(), related.relatedType().get()} );
      }

      public void caseStatistics( CaseStatisticsValue caseStatistics )
      {
         final StringBuilder str = new StringBuilder();
         StateHolder state = qi4j.getState( caseStatistics);
         state.visitProperties( new StateHolder.StateVisitor<RuntimeException>()
         {
            public void visitProperty( QualifiedName name, Object value )
            {
               if (str.length()> 0)
                  str.append(", " );
               str.append(name.name()).append( ":" ).append( value );
            }
         });

         log.info( str.toString() );
      }

      public void removedCase( String id ) throws StatisticsStoreException
      {
         log.info("Removed statistics about "+id);
      }

      public void structure(OrganizationalStructureValue structureValue)
      {
         log.info("New organizational structure:"+structureValue.toString());
      }

      public void clearAll()
      {
         log.info("Cleared statistics");
      }
   }
}
