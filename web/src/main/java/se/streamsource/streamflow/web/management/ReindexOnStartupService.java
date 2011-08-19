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

package se.streamsource.streamflow.web.management;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.index.reindexer.Reindexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reindexer. If the system property "reindex" is set, then
 * trigger the reindex on startup.
 */
@Mixins(ReindexOnStartupService.Mixin.class)
public interface ReindexOnStartupService
      extends Activatable, ServiceComposite
{
   class Mixin
         implements Activatable
   {
      @Service
      Reindexer reindexer;

      final Logger logger = LoggerFactory.getLogger( ReindexOnStartupService.class.getName() );

      public void activate() throws Exception
      {
         if (System.getProperty( "reindex" ) != null)
         {
            logger.info( "Reindex started" );
            reindexer.reindex();
            logger.info( "Reindex finished" );
         }
      }

      public void passivate() throws Exception
      {
      }
   }
}
