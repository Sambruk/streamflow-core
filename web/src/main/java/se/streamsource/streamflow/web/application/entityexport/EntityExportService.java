package se.streamsource.streamflow.web.application.entityexport;

import net.sf.ehcache.Element;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
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
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.database.DataSourceConfiguration;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchSupport;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
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
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JAVADOC
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

   Map<String,Set<String>> getTables() throws IOException, ClassNotFoundException;

   void setTables( Map<String, Set<String>> tables );

   abstract class Mixin
           implements EntityExportService
   {
      private static final Logger logger = LoggerFactory.getLogger( EntityExportService.class.getName() );

      private String infoFileAbsPath;

      @This
      Configuration<EntityExportConfiguration> thisConfig;

      private AtomicLong cacheIdGenerator = new AtomicLong( 1 );
      private AtomicLong currentId = new AtomicLong( 1 );
      private AtomicBoolean isExported = new AtomicBoolean( false );

      private String schemaInfoFileAbsPath;
      private Map<String, Set<String>> tables;

      //statistics
      private long statisticsStartExportTime;
      private int statisticsCounter = 1;
      private long statisticsPackAmount = 0;
      private double totalExportTime = 0;

      @Service
      FileConfiguration config;
      @Service
      ElasticSearchSupport support;
      @Service
      CachingService cachingService;
      @Service
      ServiceReference<DataSource> dataSource;

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

            logger.info( "Started entity export" );
            caching = new Caching( cachingService, Caches.ENTITYSTATES );
            caching.removeAll();

            try
            {

               tables =  readSchemaStateFromFile();

               try ( final Connection connection = dataSource.get().getConnection() )
               {
                  createBaseSchema( connection );
               }

               export();

            } catch ( Exception e )
            {
               logger.error( "Unexpected exception: ", e );
            }
         }

      }

      private Map<String, Set<String>> readSchemaStateFromFile() throws IOException, ClassNotFoundException
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

         final FileInputStream fis = new FileInputStream( infoFile );

         try ( final ObjectInputStream ois = new ObjectInputStream( fis ) )
         {
            result = ( Map<String, Set<String>> ) ois.readObject();
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
         if ( loggingStatisticsEntitiesCount > 0 && statisticsCounter == 1 )
         {
            statisticsStartExportTime = System.currentTimeMillis();
         } else if ( loggingStatisticsEntitiesCount < 1 )
         {
            statisticsCounter = 1;
            statisticsPackAmount = 0;
            totalExportTime = 0;
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

                  if ( statisticsCounter == loggingStatisticsEntitiesCount || isLastProcessed )
                  {
                     final long currentTime = System.currentTimeMillis();
                     final long exportTime = currentTime - statisticsStartExportTime;
                     totalExportTime += exportTime / loggingStatisticsEntitiesCount;
                     statisticsPackAmount++;

                     String message =
                             String.format( "The average export time is %.3f ms of %d entities selection",
                                     totalExportTime / statisticsPackAmount,
                                     isLastProcessed ? loggingStatisticsEntitiesCount * ( statisticsPackAmount - 1 ) + statisticsCounter : loggingStatisticsEntitiesCount * statisticsPackAmount );

                     logger.info( message );

                     statisticsCounter = 1;
                  } else
                  {
                     statisticsCounter++;
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

         long numberOfExportedEntities = 0;

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
            logger.info( "Started entities export from index to cache." );

            final int millis = 60000;
            SearchResponse searchResponse = client.prepareSearch( support.index() )
                    .addSort( "_modified", SortOrder.ASC )
                    .setScroll( new TimeValue( millis ) )
                    .setQuery( query )
                    .setSize( 1000 )
                    .get();

            SearchHit[] entities = searchResponse.getHits().getHits();

            final float step = 0.05f;
            float partPercent = 0f;
            long nextForLog = ( long ) (count * ( partPercent += step ) );
            do
            {

               for ( SearchHit searchHit : entities )
               {
                  caching.put( new Element( cacheIdGenerator.getAndIncrement(), searchHit.getSourceAsString() ) );
               }
               numberOfExportedEntities += entities.length;

               searchResponse = client
                       .prepareSearchScroll(searchResponse.getScrollId())
                       .setScroll(new TimeValue( millis ))
                       .execute().actionGet();

               entities = searchResponse.getHits().getHits();
               if ( numberOfExportedEntities >= nextForLog )
               {
                  logger.info( String.format( "Exported %f.2%% (%d) entities",
                          numberOfExportedEntities * 100.0 / count, numberOfExportedEntities ) );
                  nextForLog = ( long ) (count * ( partPercent += step ) );
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
