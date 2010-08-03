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

package se.streamsource.streamflow.web.application.management;

import org.openrdf.repository.Repository;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.entitystore.jdbm.DatabaseExport;
import org.qi4j.entitystore.jdbm.DatabaseImport;
import org.qi4j.index.reindexer.Reindexer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.factory.DomainEventFactory;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.replay.DomainEventPlayer;
import se.streamsource.streamflow.infrastructure.event.replay.EventReplayException;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.OnEvents;
import se.streamsource.streamflow.web.application.statistics.CaseStatistics;
import se.streamsource.streamflow.web.application.statistics.StatisticsStoreException;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.infrastructure.event.EventManagement;
import se.streamsource.streamflow.web.infrastructure.index.EmbeddedSolrService;
import se.streamsource.streamflow.web.infrastructure.index.SolrQueryService;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Implementation of Manager interface. All general JMX management methods
 * should be put here for convenience.
 */
@Mixins(ManagerComposite.ManagerMixin.class)
public interface ManagerComposite
      extends Manager, TransientComposite
{
   void start()
       throws Exception;

   /**
    * This is invoked on the service when the instance is being passivated
    *
    * @throws Exception if the service could not be passivated
    */
   void stop()
       throws Exception;


   abstract class ManagerMixin
         implements ManagerComposite
   {
      final Logger logger = LoggerFactory.getLogger( Manager.class.getName() );

      private static final long ONE_DAY = 1000 * 3600 * 24;
//        private static final long ONE_DAY = 1000 * 60*10; // Ten minutes

      @Structure
      Qi4j api;

      @Service
      Reindexer reindexer;

      @Service
      DatabaseExport exportDatabase;

      @Service
      DatabaseImport importDatabase;

      @Service
      EventStore eventStore;

      @Service
      EntityFinder entityFinder;

      @Service
      EventManagement eventManagement;

      @Service
      FileConfiguration fileConfig;

      @Service
      EventSource source;

      @Service
      DomainEventFactory domainEventFactory;

      @Service
      DomainEventPlayer eventPlayer;

      @Service
      ServiceReference<EntityStore> entityStore;

      @Service
      ServiceReference<Repository> repository;

      @Service
      ServiceReference<EmbeddedSolrService> solr;

      @Service
      ServiceReference<SolrQueryService> solrIndexer;

      @Service
      CaseStatistics statistics;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ModuleSPI module;

      private int failedLogins;

      public File exports;
      public File backup;

      public TransactionVisitor failedLoginListener;

      public void start() throws Exception
      {
         exports = new File( fileConfig.dataDirectory(), "exports" );
         if (!exports.exists() && !exports.mkdirs())
            throw new IllegalStateException( "Could not create directory for exports" );

         backup = new File( fileConfig.dataDirectory(), "backup" );
         if (!backup.exists() && !backup.mkdirs())
            throw new IllegalStateException( "Could not create directory for backups" );

         failedLoginListener = new OnEvents( "failedLogin" )
         {
            public void run()
            {
               failedLogins++;
            }
         };
         source.registerListener( failedLoginListener );
      }

      public void stop() throws Exception
      {
         source.unregisterListener( failedLoginListener );
      }

      // Operations

      public void reindex() throws Exception
      {
         // Delete current index
         removeRdfRepository();

         // Remove Lucene index
         removeSolrLuceneIndex();

         // Reindex state
         reindexer.reindex();
      }

      public String exportDatabase( boolean compress ) throws IOException
      {
         SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
         File exportFile = new File( exports, "streamflow_data_" + format.format( new Date() ) + (compress ? ".json.gz" : ".json") );
         OutputStream out = new FileOutputStream( exportFile );

         if (compress)
         {
            out = new GZIPOutputStream( out );
         }

         Writer writer = new OutputStreamWriter( out, "UTF-8" );
         exportDatabase.exportTo( writer );
         writer.close();

         return "Database exported to:" + exportFile.getAbsolutePath();
      }

      public String importDatabase( @Name("Filename") String name ) throws IOException
      {
         File importFile = new File( name );
         if (!importFile.isAbsolute())
            importFile = new File( exports, name );

         if (!importFile.exists())
            return "No such import file:" + importFile.getAbsolutePath();

         InputStream in1 = new FileInputStream( importFile );
         if (importFile.getName().endsWith( "gz" ))
         {
            in1 = new GZIPInputStream( in1 );
         }
         Reader in = new InputStreamReader( in1, "UTF-8" );
         try
         {
            importDatabase.importFrom( in );
         } finally
         {
            in.close();
            try
            {
               reindex();
            } catch (Exception e)
            {
               throw new RuntimeException( "Could not reindex rdf-repository", e );
            }
         }

         return "Data imported successfully";
      }

     /* public String importEvents( @Name("Filename") String name ) throws IOException
      {
         File importFile = new File( exports, name );

         if (!importFile.exists())
            return "No such import file:" + importFile.getAbsolutePath();

         InputStream in1 = new FileInputStream( importFile );
         if (importFile.getName().endsWith( "gz" ))
         {
            in1 = new GZIPInputStream( in1 );
         }
         Reader in = new InputStreamReader( in1, "UTF-8" );
         try
         {
            eventManagement.importEvents( in );
         } finally
         {
            in.close();
         }

         return "Data imported successfully";
      }*/

      public String exportEvents( @Name("Compress") boolean compress ) throws IOException
      {
         File exportFile = exportEvents0( compress );

         return "Events exported to:" + exportFile.getAbsolutePath();
      }

      public String exportEventsRange( @Name("Compress") boolean compress, @Name("From") String fromDate, @Name("To") String toDate ) throws IOException, ParseException
      {
         SimpleDateFormat parseFormat = new SimpleDateFormat( "yyyyMMdd:HHmmss" );

         Date from = parseFormat.parse( fromDate );

         Date to;
         if (toDate == null)
         {
            // Set "to"-date to "now"
            to = new Date();
         } else
         {
            to = parseFormat.parse( toDate );
         }

         File exportFile = exportEventsRange( compress, from.getTime(), to.getTime() );

         return "Events exported to:" + exportFile.getAbsolutePath();
      }

      // Backup management operations

      public String backup() throws IOException, ParseException
      {
         String backupResult = backupEvents();

         backupResult += backupDatabase();

         return backupResult;
      }

      public String restore() throws Exception
      {
         try
         {
            // Delete current database
            removeApplicationDatabase();

            // Restore data from latest backup in /backup
            File latestBackup = getLatestBackup();

            if (latestBackup != null)
               importDatabase( latestBackup.getAbsolutePath() );
            else
            {
               // Ensure that at least the root OrganizationsEntity is created
               UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Create organizations" ) );
               uow.newEntity( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
               uow.complete();
            }

            // Reindex state
            reindex();

            // Add events from backup files
            eventManagement.removeAll();

            File[] eventFiles = getBackupEventFiles();

            // Replay events from time of snapshot backup
            Date latestBackupDate = latestBackup == null ? new Date( 0 ) : getBackupDate( latestBackup );

            for (File eventFile : eventFiles)
            {

               InputStream in = new FileInputStream( eventFile );
               if (eventFile.getName().endsWith( ".gz" ))
               {
                  in = new GZIPInputStream( in );
               }

               Reader reader = new InputStreamReader( in, "UTF-8" );
               eventManagement.restoreEvents( reader );
            }

            {
               // Replay transactions
               final EventReplayException[] ex = new EventReplayException[1];
               eventStore.transactionsAfter( latestBackupDate.getTime() - 60000, new TransactionVisitor()
               {
                  public boolean visit( TransactionEvents transaction )
                  {
                     try
                     {
                        eventPlayer.playTransaction( transaction );
                        return true;
                     } catch (EventReplayException e)
                     {
                        ex[0] = e;
                        return false;
                     }
                  }
               } );

               if (ex[0] != null)
                  throw ex[0];
            }

            return "Backup restored successfully";
         } catch (Exception ex)
         {
            logger.error( "Backup restore failed:", ex );
            return "Backup restore failed:" + ex.getMessage();
         }
      }

      private String backupDatabase()
            throws ParseException, IOException
      {
         if (shouldBackupDatabase())
         {
            String result = exportDatabase( true );

            String fileName = result.substring( result.indexOf( ':' ) + 1 );
            File backupFile = moveToBackup( new File( fileName ) );

            return ", Backup created:" + backupFile.getAbsolutePath();
         } else
            return "";
      }

      private String backupEvents()
            throws IOException, ParseException
      {
         File[] eventBackups = getBackupEventFiles();
         if (eventBackups.length == 0)
         {
            // Make complete event export
            File backupFile = moveToBackup( exportEvents0( true ) );

            return "Event backup created:" + backupFile.getAbsolutePath();
         } else
         {
            // Export events since last backup
            Date lastBackup = getEventBackupDate( eventBackups[eventBackups.length - 1] );
            File exportFile = moveToBackup( exportEventsRange( true, lastBackup.getTime(), System.currentTimeMillis() ) );

            return "Event diff backup created:" + exportFile.getAbsolutePath();
         }
      }

      private File moveToBackup( File file )
      {
         File backupFile = new File( backup, file.getName() );
         file.renameTo( backupFile );
         return backupFile;
      }

      private Date getEventBackupDate( File eventBackup ) throws ParseException
      {
         String name = eventBackup.getName().substring( "streamflow_events_".length() );
         if (isCompleteEventBackup( eventBackup ))
         {
            // Range
            name = name.substring( name.indexOf( "-" ) + 1, name.indexOf( "." ) );
         } else
         {
            // Complete backup
            name = name.substring( 0, name.indexOf( "." ) );
         }

         SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
         Date backupDate = format.parse( name );

         return backupDate;
      }

      private boolean isCompleteEventBackup(File eventBackup)
      {
         return eventBackup.getName().substring( "streamflow_events_".length() ).contains( "-" );
      }

      // Backup the database if no backups exist yet,
      // or if the existing one is older than 24h

      private boolean shouldBackupDatabase()
            throws ParseException
      {
         boolean exportDatabase = false;

         File lastBackup = getLatestBackup();
         Date twentyFourHoursAgo = new Date( System.currentTimeMillis() - ONE_DAY );
         if (lastBackup != null)
         {
            Date lastDate = getBackupDate( lastBackup );
            if (lastDate.before( twentyFourHoursAgo ))
            {
               exportDatabase = true;
            }
         } else
         {
            exportDatabase = true;
         }
         return exportDatabase;
      }

      private void removeRdfRepository()
            throws Exception
      {

         ((Activatable)api.getModule( (Composite) entityFinder )).passivate();

         try
         {
            removeDirectory( new File( fileConfig.dataDirectory(), "rdf-repository" ) );
         } finally
         {
            ((Activatable)api.getModule( (Composite) entityFinder )).activate();
         }
      }

      private void removeSolrLuceneIndex()
            throws Exception
      {
         ((Activatable)api.getModule( (Composite) solr.get() )).passivate();

         try
         {
            removeDirectory( new File( fileConfig.dataDirectory(), "solr" ) );
         } finally
         {
            ((Activatable)api.getModule( (Composite) solr.get() )).activate();
         }
      }

      private void removeApplicationDatabase()
            throws Exception
      {
         ((Activatable)api.getModule( (Composite) entityStore.get() )).passivate();
         try
         {
            removeDirectory( new File( fileConfig.dataDirectory(), "data" ) );
         } finally
         {
            ((Activatable)api.getModule( (Composite) entityStore.get() )).activate();
         }
      }

      public String databaseSize()
      {
         final int[] count = {0};
         entityStore.get().visitEntityStates( new EntityStore.EntityStateVisitor()
         {
            public void visitEntityState( EntityState entityState )
            {
               count[0]++;
            }
         }, module );

         return "Database contains " + count[0] + " objects";
      }

      public void refreshStatistics() throws StatisticsStoreException
      {
         logger.info( "Start refreshing statistics" );
         statistics.refresh();
         logger.info( "Finished refreshing statistics" );
      }

      private File getLatestBackup() throws ParseException
      {
         File latest = null;
         Date latestDate = null;

         for (File file : backup.listFiles( new FileFilter()
         {
            public boolean accept( File pathname )
            {
               return pathname.getName().startsWith( "streamflow_data_" );
            }
         } ))
         {
            // See if backup is newer than currently found backup file
            if (latest == null || getBackupDate( file ).after( latestDate ))
            {
               latestDate = getBackupDate( file );
               latest = file;
            }
         }

         return latest;
      }

      private Date getBackupDate( File file ) throws ParseException
      {
         String name = file.getName().substring( "streamflow_data_".length() );
         name = name.substring( 0, name.indexOf( "." ) );

         SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
         Date backupDate = format.parse( name );

         return backupDate;
      }

      private File[] getBackupEventFiles()
      {
         File[] files = backup.listFiles( new FileFilter()
         {
            public boolean accept( File pathname )
            {
               return pathname.getName().startsWith( "streamflow_events" );
            }
         } );

         Arrays.sort( files, new Comparator<File>()
         {
            public int compare( File o1, File o2 )
            {
               return o2.getName().compareTo( o1.getName() );
            }
         } );

         return files;
      }

      private void removeDirectory( File dir )
            throws IOException
      {
         if (dir == null || !dir.exists())
            return;

         for (File file : dir.listFiles())
         {
            if (file.isDirectory())
            {
               removeDirectory( file );
            } else
            {
               if (!file.delete())
                  throw new IOException( "Could not delete file:" + file.getAbsolutePath() );
            }
         }

         if (!dir.delete())
            throw new IOException( "Could not delete directory:" + dir.getAbsolutePath() );
      }

      private File exportEvents0( boolean compress )
            throws IOException
      {
         SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
         File exportFile = new File( exports, "streamflow_events_" + format.format( new Date() ) + (compress ? ".json.gz" : ".json") );

         OutputStream out = new FileOutputStream( exportFile );

         if (compress)
         {
            out = new GZIPOutputStream( out );
         }

         final Writer writer = new OutputStreamWriter( out, "UTF-8" );

         final IOException[] ex = new IOException[1];

         eventStore.transactionsAfter( 0, new TransactionVisitor()
         {
            public boolean visit( TransactionEvents transaction )
            {
               try
               {
                  writer.write( transaction.toJSON() + "\n" );
               } catch (IOException e)
               {
                  ex[0] = e;
                  return false;
               }

               return true;
            }
         } );

         writer.close();

         if (ex[0] != null)
         {
            exportFile.delete();
            throw ex[0];
         }

         return exportFile;
      }

      private File exportEventsRange( boolean compress, long from, final long to )
            throws IOException
      {
         SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
         File exportFile = new File( exports, "streamflow_events_" + format.format( from ) + "-" + format.format( to ) + (compress ? ".json.gz" : ".json") );
         OutputStream out = new FileOutputStream( exportFile );

         if (compress)
         {
            out = new GZIPOutputStream( out );
         }

         final Writer writer = new OutputStreamWriter( out, "UTF-8" );

         final IOException[] ex = new IOException[1];

         eventStore.transactionsAfter( from, new TransactionVisitor()
         {
            public boolean visit( TransactionEvents transaction )
            {
               if (transaction.timestamp().get() > to)
                  return false;

               try
               {
                  writer.write( transaction.toJSON() + "\n" );
               } catch (IOException e)
               {
                  ex[0] = e;
                  return false;
               }

               return true;
            }
         } );

         writer.close();

         if (ex[0] != null)
         {
            exportFile.delete();
            throw ex[0];
         }

         return exportFile;
      }


      // Attributes

      public Property<Integer> failedLogins()
      {
         return new ComputedPropertyInstance<Integer>( new GenericPropertyInfo( Manager.class, "failedLogins" ) )
         {
            public Integer get()
            {
               return failedLogins;
            }
         };
      }
   }
}
