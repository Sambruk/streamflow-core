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
package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.web.domain.util.ToJson;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Saved changed entity states to cache. Activates saving to SQL.
 * @see StateChangeListener
 * @see EntityExportJob
 */
public class EntityStateChangeListener
        implements StateChangeListener
{
   private static final Logger logger = LoggerFactory.getLogger( EntityExportService.class );

   @This
   Configuration<EntityExportConfiguration> thisConfig;

   @Service
   EntityExportService entityExportService;

   @Service
   EntityStore entityStoreService;

   @Structure
   ModuleSPI moduleSPI;

   @Service
   ServiceReference<DataSource> dataSource;

   private static final AtomicBoolean enabled = new AtomicBoolean(true);

   public static void enable() {
      enabled.set(true);
   }

   public static void disable() {
      enabled.set(false);
   }

   private ExecutorService executor;

   @Override
   public void notifyChanges( Iterable<EntityState> changedStates )
   {
      if ( thisConfig.configuration().enabled().get() && enabled.get() )
      {
         try
         {
            final ToJson toJSON = moduleSPI.objectBuilderFactory().newObjectBuilder( ToJson.class ).use( moduleSPI, entityStoreService).newInstance();

            for ( EntityState changedState : changedStates )
            {
               if ( !changedState.status().equals( EntityStatus.LOADED ) )
               {
                  final String identity = changedState.identity().identity();
                  if ( changedState.status().equals( EntityStatus.REMOVED ) )
                  {
                     final JSONObject object = new JSONObject();
                     object
                             .put( "identity", identity)
                             .put( "_removed", true );
                     entityExportService.saveToCache( identity, changedState.lastModified(), object.toString() );
                  } else
                  {
                     entityExportService.saveToCache( identity, changedState.lastModified(), toJSON.toJSON( changedState, true ) );
                  }
               }
            }

            if ( executor == null )
            {
               executor = Executors.newSingleThreadExecutor( newThreadFactory() );
            }


            if ( EntityExportJob.FINISHED.get() && hasNext())
            {
               final Future<?> exportTask = executor.submit( newEntityExportJob() );

               final Runnable checker = new Runnable()
               {
                  private EntityStateChangeListener entityStateChangeListener;

                  @Override
                  public void run()
                  {
                     try
                     {
                        exportTask.get();
                        if ( hasNext() )
                        {
                           entityStateChangeListener.notifyChanges( new ArrayList<EntityState>() );
                        }
                     } catch ( Exception e )
                     {
                        logger.error( "Unexpected error: ", e );
                     }
                  }

                  Runnable setEntityStateChangeListener( EntityStateChangeListener entityStateChangeListener )
                  {
                     this.entityStateChangeListener = entityStateChangeListener;
                     return this;
                  }
               }.setEntityStateChangeListener( this );

               executor.submit( checker );
            }

         } catch ( Exception e )
         {
            logger.error( "Unexpected error.", e );
         }
      }

   }

   private boolean hasNext() throws SQLException {
      try (final Connection connection = dataSource.get().getConnection()) {
         return entityExportService.getNextEntity(connection) != null;
      }
   }

   private EntityExportJob newEntityExportJob()
   {
      EntityExportJob entityExportJob = new EntityExportJob();
      entityExportJob.setEntityExportService( entityExportService );
      entityExportJob.setDataSource( dataSource );
      entityExportJob.setModule( moduleSPI );
      return entityExportJob;
   }

   private ThreadFactory newThreadFactory()
   {
      return new ThreadFactory() {
         private ThreadFactory threadFactory = Executors.defaultThreadFactory();

         @Override
         public Thread newThread(Runnable r) {
            Thread thread = threadFactory.newThread(r);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.setDaemon(true);
            return thread;
         }
      };
   }

}
