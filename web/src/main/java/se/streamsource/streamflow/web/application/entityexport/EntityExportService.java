package se.streamsource.streamflow.web.application.entityexport;

import net.sf.ehcache.Element;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchSupport;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JAVADOC
 */
@Mixins({EntityExportService.Mixin.class, EntityStateChangeListener.class})
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

      @Service
      FileConfiguration config;
      @Service
      ElasticSearchSupport support;
      @Service
      CachingService cachingService;
      @Service
      FileConfiguration fileConfiguration;

      private Caching caching;

      @Override
      public void activate() throws Exception
      {

         if ( thisConfig.configuration().enabled().get() )
         {
            caching = new Caching( cachingService, Caches.ENTITYSTATES );

            export();

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
            List<String> info = IOUtils.readLines( new FileInputStream( infoFileAbsPath ), "UTF-8" );

            final String modified = entity.getLong( "_modified" ) + "";
            final String identity = entity.getString( "identity" );

            if ( info.size() < 2 || !( info.get( 0 ).equals( modified ) ) )
            {
               info = new ArrayList<>( 2 );
               info.add( modified );
               info.add( identity );
            } else
            {
               info.add( identity );
            }

            PrintWriter pw = new PrintWriter( infoFileAbsPath );
            for ( String line : info )
            {
               pw.println( line );
            }
            pw.flush();
            pw.close();
         } catch ( Exception e )
         {
            logger.error( "Error: ", e );
         }
         caching.remove( currentId.getAndIncrement() );
      }

      @Override
      public void passivate() throws Exception
      {

      }

      private void export() throws IOException, InterruptedException
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
         } else if ( !( infoFile.getParentFile().mkdirs() & infoFile.createNewFile() ) )
         {
            throw new IllegalStateException( "Can't create file of last processed entities info" );
         }

         final int numberOfEntitiesForRequest = thisConfig.configuration().numberOfEntitiesForESrequest().get();
         boolean searchContinue = true;

         int numberOfExportedEntities = 0;

         while ( searchContinue )
         {
            final SearchHit[] entities = searchEntitiesAfterTimestamp( lastProcessedTimestamp, numberOfEntitiesForRequest );

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

            searchContinue = entities.length == numberOfEntitiesForRequest;
         }

         isExported.set( true );

         logger.info( "Finished entities export from ES index." );
      }

      private SearchHit[] searchEntitiesAfterTimestamp( LastProcessedTimestampInfo info, final int maxEntitiesPerRequest )
      {
         Client client = support.client();
         String modified = "_modified";

         final SearchRequestBuilder request = client.prepareSearch( support.index() );

         final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                 .must( QueryBuilders.rangeQuery( modified ).gte( info.lastProcessedTimestamp ) );
         if ( info.ids.size() > 0 )
         {
            boolQueryBuilder.mustNot( QueryBuilders.termsQuery( "_identity", info.ids ) );
         }

         request.setQuery( boolQueryBuilder );
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
