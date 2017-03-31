package se.streamsource.streamflow.web.application.entityexport;

import net.sf.ehcache.Element;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        EntityExportJob,
        ServiceComposite,
        Activatable,
        Configuration<EntityExportConfiguration>
{

   boolean isExported();

   void saveToCache( String transaction );

   String getNextEntity();

   boolean hasNextEntity();

   void savedSuccess( JSONObject entity );

   Map<String, Set<String>> getTables();

   void setTables( Map<String, Set<String>> tables );

   abstract class Mixin
           implements EntityExportService
   {
      private final Logger logger = LoggerFactory.getLogger( EntityExportService.class );

      private String infoFileAbsPath;

      @This
      Configuration<EntityExportConfiguration> thisConfig;

      private AtomicLong cacheIdGenerator = new AtomicLong( 1 );
      private AtomicLong currentId = new AtomicLong( 1 );
      private AtomicBoolean isExported = new AtomicBoolean( false );

      private Map<String, Set<String>> schema;

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

            try ( final Connection connection = dataSource.get().getConnection() )
            {
               schema = createBaseSchema( connection );

               addSchemaInfo( schema, connection );

               caching = new Caching( cachingService, Caches.ENTITYSTATES );
               caching.removeAll();

               export();

            } catch ( Exception e )
            {
               logger.error( "Unexpected exception: ", e );
            }


         }

      }

      private void addSchemaInfo( Map<String, Set<String>> schema, Connection connection ) throws SQLException
      {
         final DatabaseMetaData metaData = connection.getMetaData();

         String[] types = { "TABLE" };
         final ResultSet tablesRs =
                 metaData.getTables( null, null, "%", types );

         while ( tablesRs.next() )
         {
            final String tableName = tablesRs.getString( "TABLE_NAME" );

            Set<String> columns = schema.get( tableName );
            if ( columns == null )
            {
               columns = new HashSet<>();
            }

            final ResultSet columnsRs = metaData.getColumns( null, null, tableName, null );
            while ( columnsRs.next() )
            {
               columns.add( columnsRs.getString( "COLUMN_NAME" ) );
            }

            schema.put( tableName, columns );
         }
      }

      @Override
      public synchronized void saveToCache( String transaction )
      {
         caching.put( new Element( cacheIdGenerator.getAndIncrement(), transaction ) );
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

                  if ( statisticsCounter == loggingStatisticsEntitiesCount )
                  {
                     final long currentTime = System.currentTimeMillis();
                     final long exportTime = currentTime - statisticsStartExportTime;
                     totalExportTime += exportTime / loggingStatisticsEntitiesCount;
                     statisticsPackAmount++;

                     String message =
                             String.format( "Exported %d entities with average time %.3f ms",
                                     loggingStatisticsEntitiesCount * statisticsPackAmount,
                                     totalExportTime / statisticsPackAmount );

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
      public Map<String, Set<String>> getTables()
      {
         return schema;
      }

      @Override
      public void setTables( Map<String, Set<String>> tables )
      {
         schema = tables;
      }

      @Override
      public void passivate() throws Exception
      {

      }

      private Map<String, Set<String>> createBaseSchema( Connection connection ) throws Exception
      {

         final File infoFile = new File( config.dataDirectory(), "entityexport/schema.info" );

         if ( !infoFile.exists() )
         {
            final File parentDirectory = infoFile.getParentFile();
            if ( !parentDirectory.exists() )
            {
               parentDirectory.mkdir();
            }

            infoFile.createNewFile();
         }

         final UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Get Datasource configuration" ) );
         final DataSourceConfiguration dataSourceConfiguration = uow.get( DataSourceConfiguration.class, dataSource.identity() );
         final DbVendor dbVendor = DbVendor.from( dataSourceConfiguration.dbVendor().get() );

         final SchemaCreatorHelper schemaUpdater = new SchemaCreatorHelper();
         schemaUpdater.setModule( moduleSPI );
         schemaUpdater.setConnection( connection );
         schemaUpdater.setDbVendor( dbVendor );
         schemaUpdater.setInfoFile( infoFile );
         return schemaUpdater.create();
      }

      private void export() throws IOException, InterruptedException
      {
         final int entitiesNumberForESrequest = thisConfig.configuration().entitiesNumberForESrequest().get();

         if ( entitiesNumberForESrequest != 0 )
         {
            logger.info( "Started entities export from ES index." );

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

            boolean searchContinue = true;

            int numberOfExportedEntities = 0;

            while ( searchContinue )
            {
               final SearchHit[] entities = searchEntitiesAfterTimestamp( lastProcessedTimestamp, entitiesNumberForESrequest );

               List<Element> forPersist = new ArrayList<>( entities.length );
               for ( int i = 0; i < entities.length; i++ )
               {

                  forPersist.add( new Element( cacheIdGenerator.getAndIncrement(), entities[i].getSourceAsString() ) );

                  if ( i == entities.length - 1 )
                  {

                     final String modifiedKey = "_modified";

                     final Long modified = ( Long ) entities[i].getSource().get( modifiedKey );

                     if ( !modified.equals( lastProcessedTimestamp.lastProcessedTimestamp ) )
                     {
                        lastProcessedTimestamp.ids = new HashSet<>();
                     }

                     lastProcessedTimestamp.lastProcessedTimestamp = modified;

                     for ( int j = entities.length - 1; j >= 0 &&
                             entities[j].getSource().get( modifiedKey ).equals( lastProcessedTimestamp.lastProcessedTimestamp ); j-- )
                     {
                        lastProcessedTimestamp.ids.add( entities[j].getId() );
                     }

                  }
               }

               caching.putAll( forPersist );

               logger.info( "Exported " + ( numberOfExportedEntities += entities.length ) + " entities" );

               searchContinue = entities.length == entitiesNumberForESrequest;
            }

            isExported.set( true );

            logger.info( "Finished entities export from ES index." );
         }
      }

      private SearchHit[] searchEntitiesAfterTimestamp( LastProcessedTimestampInfo info, final int maxEntitiesPerRequest )
      {
         Client client = support.client();
         String modified = "_modified";

         final SearchRequestBuilder request = client.prepareSearch( support.index() );

         QueryBuilder query = QueryBuilders.rangeQuery( modified ).gte( info.lastProcessedTimestamp );

         if ( info.ids.size() > 0 )
         {
            query = QueryBuilders
                    .filteredQuery( query,
                            FilterBuilders.boolFilter().mustNot( FilterBuilders.termsFilter( "_identity", info.ids ) ) );
         }

         request.setQuery( query );
         request.setSize( maxEntitiesPerRequest );
         final FieldSortBuilder sortBuilder = new FieldSortBuilder( modified );
         sortBuilder.order( SortOrder.ASC );
         sortBuilder.ignoreUnmapped( true );
         sortBuilder.missing( "_first" );
         request.addSort( sortBuilder );
         final SearchResponse response = request.execute().actionGet();

         return response.getHits().getHits();
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
