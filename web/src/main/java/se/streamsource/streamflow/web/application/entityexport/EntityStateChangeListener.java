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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.entitystore.helpers.JSONEntityState;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.util.Primitives;
import se.streamsource.streamflow.web.domain.util.ToJson;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
            for ( EntityState changedState : changedStates )
            {
               if ( !changedState.status().equals( EntityStatus.LOADED ) )
               {
                  final String identity = changedState.identity().identity();
                  if ( changedState.status().equals( EntityStatus.REMOVED ) )
                  {
                     final JSONObject object = new JSONObject();
                     object
                             .put(JSONEntityState.JSON_KEY_IDENTITY, identity)
                             .put(JSONEntityState.JSON_KEY_TYPE, changedState.entityDescriptor().entityType().toString())
                             .put( "_removed", true );
                     entityExportService.saveToCache( identity, changedState.lastModified(), object.toString() );
                  } else
                  {
                     entityExportService.saveToCache( identity, changedState.lastModified(), toJSON(changedState) );
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

   private String toJSON(EntityState state)
   {
      JSONObject json;
      try
      {
         json = new JSONObject();

         json.put(JSONEntityState.JSON_KEY_IDENTITY, state.identity());

         EntityDescriptor entityDesc = state.entityDescriptor();
         EntityType entityType = entityDesc.entityType();

         json.put(JSONEntityState.JSON_KEY_TYPE, entityType.toString());
         json.put(JSONEntityState.JSON_KEY_MODIFIED, state.lastModified() );

         final JSONObject properties = new JSONObject();
         for( PropertyType propType : entityType.properties() )
         {

               String key = propType.qualifiedName().name();
               Object value = state.getProperty(propType.qualifiedName());
               if( value == null || Primitives.isPrimitiveValue(value) )
               {
                  properties.put( key, value );
               }
               else
               {
                  // TODO Theses tests are pretty fragile, find a better way to fix this, Jackson API should behave better
                  String serialized = propType.type().toJSON(value).toString();
                  if( serialized.startsWith( "{" ) )
                  {
                     properties.put( key, new JSONObject( serialized ) );
                  }
                  else if( serialized.startsWith( "[" ) )
                  {
                     properties.put( key, new JSONArray( serialized ) );
                  }
                  else
                  {
                     properties.put( key, serialized );
                  }
               }
         }
         json.put(JSONEntityState.JSON_KEY_PROPERTIES, properties);

         final JSONObject associations = new JSONObject();
         for( AssociationDescriptor assocDesc : entityDesc.state().associations() )
         {
            String key = assocDesc.qualifiedName().name();
            EntityReference associated = state.getAssociation(assocDesc.qualifiedName());
            associations.put( key, associated != null ? associated.identity() : null );
         }
         json.put(JSONEntityState.JSON_KEY_ASSOCIATIONS, associations);

         final JSONObject manyassociations = new JSONObject();
         for( ManyAssociationDescriptor manyAssocDesc : entityDesc.state().manyAssociations() )
         {
            String key = manyAssocDesc.qualifiedName().name();
            ManyAssociationState associateds = state.getManyAssociation(manyAssocDesc.qualifiedName());
            JSONArray array = null;
            if (associateds != null) {
               array = new JSONArray();
               for( EntityReference associated : associateds )
               {
                  array.put(associated.identity());
               }
            }
            manyassociations.put( key, array );
         }
         json.put(JSONEntityState.JSON_KEY_MANYASSOCIATIONS, manyassociations);

         return json.toString();
      }
      catch( Exception e )
      {
         logger.info("Failed to convert Entity to Json: " + state.identity(), e);
         throw new RuntimeException("Failed to convert Entity to Json: " + state.identity(), e);
      }
   }

}
