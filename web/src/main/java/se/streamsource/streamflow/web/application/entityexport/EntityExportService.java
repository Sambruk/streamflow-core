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

import net.sf.ehcache.Element;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.database.DataSourceConfiguration;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchSupport;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.domain.util.ToJson;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service encapsulates interaction with cache for entity export.
 * It fires on startup (writes from index to cache)
 * and helps to write to cache when application started.
 * <br/>
 * Search response sized with 1000 because tests show this's the optimal value.
 */
@Mixins({ EntityExportService.Mixin.class, EntityStateChangeListener.class })
public interface EntityExportService
        extends StateChangeListener,
        ServiceComposite,
        Activatable,
        Configuration<EntityExportConfiguration>
{

   boolean isExported();

   void saveToCache( String transaction );

   String getNextEntity();

   String getSchemaInfoFileAbsPath();

   boolean hasNextEntity();

   void savedSuccess( JSONObject entity );

   Map<String, Set<String>> getTables() throws IOException, ClassNotFoundException;

   void setTables( Map<String, Set<String>> tables );

   DbVendor getDbVendor();

   abstract class Mixin
           implements EntityExportService
   {
      private static final Logger logger = LoggerFactory.getLogger( EntityExportService.class );

      private String infoFileAbsPath;

      private DbVendor dbVendor;

      @This
      Configuration<EntityExportConfiguration> thisConfig;

      private AtomicLong cacheIdGenerator = new AtomicLong( 1 );
      private AtomicLong currentId = new AtomicLong( 1 );
      private AtomicBoolean isExported = new AtomicBoolean( false );

      private static final int REQUEST_SIZE_THRESHOLD = 1000;
      private static final long SCROLL_KEEP_ALIVE = TimeUnit.MINUTES.toMillis(1);

      private String schemaInfoFileAbsPath;
      private Map<String, Set<String>> tables;

      //statistics
      private long statisticsStartExportTime;
      private AtomicInteger statisticsCounter = new AtomicInteger(1);
      private AtomicLong totalExportTime = new AtomicLong(0);

      @Service
      FileConfiguration config;
      @Service
      ElasticSearchSupport support;
      @Service
      CachingService cachingService;
      @Service
      ServiceReference<DataSource> dataSource;
      @Service
      EntityStore entityStoreService;

      @Structure
      ModuleSPI moduleSPI;
      @Structure
      Module module;

      private Caching caching;

      @Override
      public void activate() throws Exception
      {

         if ( dataSource.isAvailable()
                 && dataSource.isActive()
                 && thisConfig.configuration().enabled().get() )
         {

            caching = new Caching( cachingService, Caches.ENTITYSTATES );
            caching.removeAll();

            try
            {

               tables = readSchemaStateFromFile();

               try ( final Connection connection = dataSource.get().getConnection() )
               {
                  createBaseSchema( connection );
               }

               dbVendor = _getDbVendor();

               export();

            } catch ( Exception e )
            {
               logger.error( "Unexpected exception: ", e );
            }
         }

      }

      private Map<String, Set<String>> readSchemaStateFromFile() throws IOException, ClassNotFoundException, JSONException
      {
         final File infoFile = new File( config.dataDirectory(), "entityexport/schema.info" );
         schemaInfoFileAbsPath = infoFile.getAbsolutePath();

         Map<String, Set<String>> result = new HashMap<>();

         if ( !infoFile.exists() )
         {
            final File parentDirectory = infoFile.getParentFile();
            if ( !parentDirectory.exists() )
            {
               parentDirectory.mkdir();
            }

            infoFile.createNewFile();

            return result;
         }

         String fileContent = FileUtils.readFileToString( infoFile, StandardCharsets.UTF_8.name() );
         if ( fileContent.isEmpty() )
         {
            return result;
         }
         JSONObject jsonObject = new JSONObject( fileContent );
         Iterator keys = jsonObject.keys();
         while ( keys.hasNext() )
         {
            String tableName = ( String ) keys.next();
            JSONArray jsonArray = jsonObject.getJSONArray( tableName );
            Set<String> columns = new LinkedHashSet<>();
            for ( int i = 0; i < jsonArray.length(); i++ )
            {
               columns.add( jsonArray.getString( i ) );
            }
            result.put( tableName, columns );
         }
         return result;
      }

      @Override
      public synchronized void saveToCache( String transaction )
      {
         //Resolved possible NPE
         if ( caching != null )
         {
            caching.put( new Element( cacheIdGenerator.getAndIncrement(), transaction ) );
         }
      }

      @Override
      public boolean isExported()
      {
         return isExported.get();
      }

      @Override
      public String getNextEntity()
      {
         final Integer loggingStatisticsEntitiesCount = thisConfig.configuration().loggingStatisticsEntitiesCount().get();
         if ( loggingStatisticsEntitiesCount > 0 && statisticsCounter.get() == 1 )
         {
            statisticsStartExportTime = System.currentTimeMillis();
         } else if ( loggingStatisticsEntitiesCount < 1 )
         {
            statisticsCounter.set(1);
            totalExportTime.set(0);
         }

         final Element element = caching.get( currentId.get() );
         return element == null ? "" : ( String ) element.getObjectValue();
      }

      @Override
      public boolean hasNextEntity()
      {
         return !( cacheIdGenerator.get() == currentId.get() );
      }

      @Override
      public Map<String, Set<String>> getTables() throws IOException, ClassNotFoundException
      {
         return tables;
      }

      @Override
      public void setTables( Map<String, Set<String>> tables )
      {
         this.tables = tables;
      }

      @Override
      public DbVendor getDbVendor() {
         return dbVendor;
      }

      private DbVendor _getDbVendor() {
         final UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Get Datasource configuration" ) );
         final DataSourceConfiguration dataSourceConfiguration = uow.get( DataSourceConfiguration.class, dataSource.identity() );
         return DbVendor.from( dataSourceConfiguration.dbVendor().get() );
      }

      @Override
      public void savedSuccess( JSONObject entity )
      {
         try
         {
            try ( final BufferedReader br = new BufferedReader( new FileReader( infoFileAbsPath ) ) )
            {
               final String modifiedFromInfo = br.readLine();

               final Long modified = entity.getLong( "_modified" );
               final String identity = entity.getString( "identity" );

               final PrintWriter pw;

               if ( modifiedFromInfo == null
                       || br.readLine() == null
                       || !modifiedFromInfo.equals( modified.toString() ) )
               {
                  pw = new PrintWriter( infoFileAbsPath );
                  pw.println( modified );
                  pw.println( identity );
               } else
               {
                  pw = new PrintWriter( new FileWriter( infoFileAbsPath, true ) );
                  pw.println( identity );
               }

               pw.flush();
               pw.close();

               final Integer loggingStatisticsEntitiesCount = thisConfig.configuration().loggingStatisticsEntitiesCount().get();
               if ( loggingStatisticsEntitiesCount > 0 )
               {

                  final boolean isLastProcessed = currentId.get() + 1 == cacheIdGenerator.get();

                  if ( statisticsCounter.get() == loggingStatisticsEntitiesCount || isLastProcessed )
                  {
                     final long currentTime = System.currentTimeMillis();
                     final long exportTime = currentTime - statisticsStartExportTime;
                     totalExportTime.addAndGet(exportTime);

                     String message =
                             String.format( "The average export time is %.3f ms of %d entities selection",
                                     (double) totalExportTime.get() / currentId.get(),
                                     currentId.get());

                     logger.info( message );

                     statisticsCounter.set(1);
                  } else
                  {
                     statisticsCounter.incrementAndGet();
                  }

               }

            }

         } catch ( Exception e )
         {
            logger.error( "Error: ", e );
         }

         caching.remove( currentId.getAndIncrement() );
      }

      @Override
      public String getSchemaInfoFileAbsPath()
      {
         return schemaInfoFileAbsPath;
      }

      @Override
      public void passivate() throws Exception
      {

      }

      private void createBaseSchema( Connection connection ) throws Exception
      {
         final UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Get Datasource configuration" ) );
         final DataSourceConfiguration dataSourceConfiguration = uow.get( DataSourceConfiguration.class, dataSource.identity() );
         final DbVendor dbVendor = DbVendor.from( dataSourceConfiguration.dbVendor().get() );

         final SchemaCreatorHelper schemaUpdater = new SchemaCreatorHelper();
         schemaUpdater.setModule( moduleSPI );
         schemaUpdater.setConnection( connection );
         schemaUpdater.setDbVendor( dbVendor );
         schemaUpdater.setSchemaInfoFileAbsPath( schemaInfoFileAbsPath );
         schemaUpdater.setTables( tables );
         schemaUpdater.setShowSql( thisConfig.configuration().showSql().get() );
         schemaUpdater.create();
      }

      private void export() throws IOException, InterruptedException
      {

         final File infoFile = new File( config.dataDirectory(), "entityexport/last_processed_timestamp.info" );
         infoFileAbsPath = infoFile.getAbsolutePath();

         LastProcessedTimestampInfo lastProcessedTimestamp = new LastProcessedTimestampInfo( 0L );
         if ( infoFile.exists() )
         {
            try
            {
               lastProcessedTimestamp = getLastProcessedTimestampInfo();
            } catch ( IllegalStateException allOk )
            {
            } catch ( Exception e )
            {
               logger.error( "Error on reading last_processed_timestamp.info file.", e );
            }
         } else if ( !( infoFile.createNewFile() ) )
         {
            throw new IllegalStateException( "Can't create file of last processed entities info" );
         }

         long numberOfExportedEntities = 0L;

         Client client = support.client();

         QueryBuilder query = QueryBuilders
                 .rangeQuery( "_modified" )
                 .gte( lastProcessedTimestamp.lastProcessedTimestamp );

         if ( lastProcessedTimestamp.ids.size() > 0 )
         {
            query = QueryBuilders
                    .filteredQuery( query,
                            FilterBuilders.boolFilter().mustNot( FilterBuilders.termsFilter( "_identity", lastProcessedTimestamp.ids ) ) );
         }

         final long count = client
                 .prepareCount( support.index() )
                 .setQuery( query )
                 .execute()
                 .actionGet()
                 .getCount();

         if ( count != 0 )
         {
            logger.info( "Started entities export from index to cache. Ready to be exported " + count + " entities." );

            final EntityStoreUnitOfWork uow = entityStoreService.newUnitOfWork(UsecaseBuilder.newUsecase("toJSON"), moduleSPI);
            final ToJson toJSON = module.objectBuilderFactory().newObjectBuilder(ToJson.class).use( moduleSPI, entityStoreService).newInstance();

            SearchResponse searchResponse = client.prepareSearch( support.index() )
                    .addSort( "_modified", SortOrder.ASC )
                    .setScroll( new TimeValue( SCROLL_KEEP_ALIVE ) )
                    .setSearchType( SearchType.QUERY_AND_FETCH )
                    .setQuery( query )
                    .setSize( REQUEST_SIZE_THRESHOLD )
                    .get();

            SearchHit[] entities = searchResponse.getHits().getHits();

            final float step = 0.05f;
            float partPercent = 0f;
            long nextForLog = ( long ) ( count * ( partPercent += step ) );
            do
            {

               for ( SearchHit searchHit : entities )
               {
                  final String identity = searchHit.getId();
                  final EntityState entityState = uow.getEntityState( EntityReference.parseEntityReference( identity ) );
                  final String entity = toJSON.toJSON(entityState, true);
                  caching.put( new Element( cacheIdGenerator.getAndIncrement(), entity) );
               }
               numberOfExportedEntities += entities.length;

               searchResponse = client
                       .prepareSearchScroll( searchResponse.getScrollId() )
                       .setScroll( new TimeValue( SCROLL_KEEP_ALIVE ) )
                       .execute().actionGet();

               entities = searchResponse.getHits().getHits();

               if ( numberOfExportedEntities >= nextForLog )
               {
                  logger.info( String.format( "Exported %d %% (%d) entities",
                          numberOfExportedEntities * 100 / count, numberOfExportedEntities ) );
                  nextForLog = ( long ) ( count * ( partPercent += step ) );
               }

            } while ( entities.length != 0 );

            logger.info( "Finished entities export from index to cache." );
         } else
         {
            logger.info( "Nothing to export from index to cache." );
         }

         isExported.set( true );
      }

      LastProcessedTimestampInfo getLastProcessedTimestampInfo() throws Exception
      {
         final List<String> data = IOUtils.readLines( new FileInputStream( infoFileAbsPath ), "UTF-8" );

         if ( data.isEmpty() )
         {
            throw new IllegalStateException( "File is empty." );
         }

         if ( data.size() < 2 )
         {
            throw new IllegalStateException( "At least must be two records." );
         }

         LastProcessedTimestampInfo lastProcessedTimestampInfo = new LastProcessedTimestampInfo( new Long( data.remove( 0 ) ) );

         lastProcessedTimestampInfo.ids.addAll( data );

         return lastProcessedTimestampInfo;

      }

      private class LastProcessedTimestampInfo
      {
         private Long lastProcessedTimestamp;
         private Set<String> ids;

         private LastProcessedTimestampInfo( Long lastProcessedTimestamp )
         {
            this.lastProcessedTimestamp = lastProcessedTimestamp;
            this.ids = new HashSet<>();
         }
      }

   }


}
