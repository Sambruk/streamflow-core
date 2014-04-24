/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import org.apache.solr.common.SolrException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import org.openrdf.repository.Repository;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Transforms;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.index.reindexer.Reindexer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.BackupRestore;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.structure.ModuleSPI;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.factory.DomainEventFactory;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayer;
import se.streamsource.streamflow.infrastructure.event.domain.replay.EventReplayException;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionVisitor;
import se.streamsource.streamflow.web.application.archival.ArchivalService;
import se.streamsource.streamflow.web.application.archival.ArchivalStartJob;
import se.streamsource.streamflow.web.application.dueon.DueOnNotificationJob;
import se.streamsource.streamflow.web.application.statistics.CaseStatistics;
import se.streamsource.streamflow.web.application.statistics.StatisticsStoreException;
import se.streamsource.streamflow.web.infrastructure.event.EventManagement;
import se.streamsource.streamflow.web.infrastructure.index.EmbeddedSolrService;
import se.streamsource.streamflow.web.infrastructure.index.SolrQueryService;
import se.streamsource.streamflow.web.infrastructure.plugin.StreetAddressLookupConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.address.StreetAddressLookupService;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImportJob;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImporterService;
import se.streamsource.streamflow.web.infrastructure.scheduler.QuartzSchedulerService;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import static org.qi4j.api.util.Iterables.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * Implementation of Manager interface. All general JMX management methods
 * should be put here for convenience.
 */
@Mixins(ManagerComposite.ManagerMixin.class)
public interface
        ManagerComposite
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
      final Logger logger = LoggerFactory.getLogger(Manager.class.getName());

      private static final long ONE_DAY = 1000 * 3600 * 24;
//        private static final long ONE_DAY = 1000 * 60*10; // Ten minutes

      @Structure
      Qi4j api;

      @Service
      Reindexer reindexer;

      @Service
      BackupRestore backupRestore;

      @Service
      EventSource eventSource;

      @Service
      EntityFinder entityFinder;

      @Service
      EventManagement eventManagement;

      @Service
      FileConfiguration fileConfig;

      @Service
      EventStream stream;

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

      @Service
      LdapImporterService ldapImporterService;
      
      @Structure
      ModuleSPI module;

      @Service
      ArchivalService archivalService;

      @Service
      QuartzSchedulerService scheduler;

      private int failedLogins;

      public File exports;
      public File backup;

      public TransactionListener failedLoginListener;

      private ArchivalStartJob archivalJob;

      public void start() throws Exception
      {
         exports = new File(fileConfig.dataDirectory(), "exports");
         if (!exports.exists() && !exports.mkdirs())
            throw new IllegalStateException("Could not create directory for exports");

         backup = new File(fileConfig.dataDirectory(), "backup");
         if (!backup.exists() && !backup.mkdirs())
            throw new IllegalStateException("Could not create directory for backups");

         failedLoginListener = new TransactionListener()
         {
            public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
            {
               failedLogins += count(filter(withNames("failedLogin"), events(transactions)));
            }
         };

         stream.registerListener( failedLoginListener );
      }

      public void stop() throws Exception
      {
         stream.unregisterListener( failedLoginListener );
      }

      // Operations

      public void reindex() throws Exception
      {
         DateTime startDateTime = new DateTime( );
         logger.info( "Starting reindex at " + startDateTime.toString() );

         logger.info( "Remove RDF index." );
         // Delete current index
         removeRdfRepository();

         logger.info( "Remove Solr index." );
         // Remove Lucene index contents
         removeSolrLuceneIndexContents();

         logger.info( "Reindexing ..." );
         // Reindex state
         reindexer.reindex();

         // reindex street cache if plugin is enabled
         StreetAddressLookupService streetLookup = (StreetAddressLookupService) module.serviceFinder().findService( StreetAddressLookupService.class ).get();
         if( streetLookup != null && ((StreetAddressLookupConfiguration)streetLookup.configuration()).enabled().get() )
         {
            logger.info( "Reindexing StreetLookup." );
            streetLookup.reindex();
         }

         logger.info( "Reindexing done in " + PeriodFormat.getDefault().print( new Duration( startDateTime, new DateTime( ) ).toPeriod() ) );
      }

      public String exportDatabase(boolean compress) throws IOException
      {
         SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
         File exportFile = new File(exports, "streamflow_data_" + format.format(new Date()) + (compress ? ".json.gz" : ".json"));

         backupRestore.backup().transferTo(Outputs.text(exportFile));

         return "Database exported to:" + exportFile.getAbsolutePath();
      }

      public String importDatabase(@Name("Filename") String name) throws IOException
      {
         File importFile = new File(name);
         if (!importFile.isAbsolute())
            importFile = new File(exports, name);

         if (!importFile.exists())
            return "No such import file:" + importFile.getAbsolutePath();

         logger.info("Importing " + importFile);

         try
         {
            Inputs.text(importFile).transferTo(Transforms.map(new Function<String, String>()
            {
               int count = 0;

               public String map(String s)
               {
                  count++;

                  if (count % 1000 == 0)
                     logger.info("Imported " + count + " entities");

                  return s;
               }
            }, backupRestore.restore()));
            logger.info("Imported " + importFile);
         } finally
         {
            try
            {
               reindex();
            } catch (Exception e)
            {
               throw new RuntimeException("Could not reindex rdf-repository", e);
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

      public String exportEvents(@Name("Compress") boolean compress) throws IOException
      {
         File exportFile = exportEvents0( compress );

         return "Events exported to:" + exportFile.getAbsolutePath();
      }

      public String exportEventsRange(@Name("Compress") boolean compress, @Name("From") String fromDate, @Name("To") String toDate) throws IOException, ParseException
      {
         SimpleDateFormat parseFormat = new SimpleDateFormat("yyyyMMdd:HHmmss");

         Date from = parseFormat.parse(fromDate);

         Date to;
         if (toDate == null)
         {
            // Set "to"-date to "now"
            to = new Date();
         } else
         {
            to = parseFormat.parse(toDate);
         }

         File exportFile = exportEventsRange( compress, from.getTime(), to.getTime() );

         return "Events exported to:" + exportFile.getAbsolutePath();
      }

      // Backup management operations

      public String backup() throws IOException, ParseException
      {
         DateTime startDateTime = new DateTime( );
         logger.info( "Started backup at " + startDateTime.toString() );
         String backupResult = backupEvents();

         backupResult += backupDatabase();

         logger.info( "Backup done successfully in: " + PeriodFormat.getDefault().print( new Duration(startDateTime, new DateTime( ) ).toPeriod() ) );
         return backupResult;
      }

      public String restore() throws Exception
      {
         try
         {
            DateTime startDateTime = new DateTime(  );
            logger.info( "Starting restore at " + startDateTime.toString() );

            // Restore data from latest backup in /backup
            File latestBackup = getLatestBackup();

            // Check if a backup actually exists
            if (latestBackup == null)
            {
               return "Error: no backup to restore";
            }
            logger.info( "Fetching latest backup and start import database and reindex." );

            // contains already a call to reindex
            importDatabase( latestBackup.getAbsolutePath() );

            logger.info( "Import database and reindex done. Clearing event database." );

            // Add events from backup files
            eventManagement.removeAll();

            logger.info( "Replaying backup event files from time of snapshot backup." );
            File[] eventFiles = getBackupEventFiles();

            // Replay events from time of snapshot backup
            Date latestBackupDate = latestBackup == null ? new Date(0) : getBackupDate(latestBackup);

            Inputs.combine(Iterables.map(new Function<File, Input<String, IOException>>()
            {
               public Input<String, IOException> map(File file)
               {
                  return Inputs.text(file);
               }
            }, Arrays.asList(eventFiles))).transferTo(eventManagement.restore());

            {
               // Replay transactions
               final EventReplayException[] ex = new EventReplayException[1];
               eventSource.transactionsAfter(latestBackupDate.getTime() - 60000, new TransactionVisitor()
               {
                  public boolean visit(TransactionDomainEvents transactionDomain)
                  {
                     try
                     {
                        eventPlayer.playTransaction(transactionDomain);
                        return true;
                     } catch (EventReplayException e)
                     {
                        ex[0] = e;
                        return false;
                     }
                  }
               });

               if (ex[0] != null)
                  throw ex[0];
            }

            logger.info( "Restore done successfully in: " + PeriodFormat.getDefault().print( new Duration(startDateTime, new DateTime( ) ).toPeriod() ) );

            return "Backup restored successfully";
         } catch (Exception ex)
         {
            logger.error("Backup restore failed:", ex);
            return "Backup restore failed:" + ex.getMessage();
         }
      }

      private String backupDatabase()
              throws ParseException, IOException
      {
         if (shouldBackupDatabase())
         {
            String result = exportDatabase(true);

            String fileName = result.substring(result.indexOf(':') + 1);
            File backupFile = moveToBackup(new File(fileName));

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
            File backupFile = moveToBackup(exportEvents0(true));

            return "Event backup created:" + backupFile.getAbsolutePath();
         } else
         {
            // Export events since last backup
            Date lastBackup = getEventBackupDate(eventBackups[eventBackups.length - 1]);
            File exportFile = moveToBackup(exportEventsRange(true, lastBackup.getTime(), System.currentTimeMillis()));

            return "Event diff backup created:" + exportFile.getAbsolutePath();
         }
      }

      private File moveToBackup(File file)
      {
         File backupFile = new File(backup, file.getName());
         file.renameTo(backupFile);
         return backupFile;
      }

      private Date getEventBackupDate(File eventBackup) throws ParseException
      {
         String name = eventBackup.getName().substring("streamflow_events_".length());
         if (isDiffEventBackup(eventBackup))
         {
            // Range
            name = name.substring(name.indexOf("-") + 1, name.indexOf("."));
         } else
         {
            // Complete backup
            name = name.substring(0, name.indexOf("."));
         }

         SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
         Date backupDate = format.parse(name);

         return backupDate;
      }

      private boolean isDiffEventBackup(File eventBackup)
      {
         return eventBackup.getName().substring("streamflow_events_".length()).contains("-");
      }

      // Backup the database if no backups exist yet,
      // or if the existing one was not made today

      private boolean shouldBackupDatabase()
              throws ParseException
      {
         boolean exportDatabase = false;

         File lastBackup = getLatestBackup();
         //Date twentyFourHoursAgo = new Date(System.currentTimeMillis() - ONE_DAY);
         if (lastBackup != null)
         {
            DateTime lastDate = new DateTime( getBackupDate(lastBackup) );

            //if (lastDate.before(twentyFourHoursAgo))
            if( DateTimeComparator.getDateOnlyInstance().compare( lastDate, new DateTime( ) ) != 0 )
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

         ((Activatable) api.getModule((Composite) entityFinder)).passivate();

         try
         {
            removeDirectory(new File(fileConfig.dataDirectory(), "rdf-repository"));
         } finally
         {
            ((Activatable) api.getModule((Composite) entityFinder)).activate();
         }
      }

      private void removeSolrLuceneIndexContents()
              throws Exception
      {
         /*((Activatable) api.getModule((Composite) solr.get())).passivate();

         try
         {
            removeDirectory(new File(fileConfig.dataDirectory(), "solr"));
         } finally
         {
            ((Activatable) api.getModule((Composite) solr.get())).activate();
         }*/
         try{
            solr.get().getSolrServer( "sf-core" ).deleteByQuery( "*:*" );
         } catch (SolrException se )
         {
            // do nothing
         }
      }

      private void removeApplicationDatabase()
              throws Exception
      {
         ((Activatable) api.getModule((Composite) entityStore.get())).passivate();
         try
         {
            removeDirectory(new File(fileConfig.dataDirectory(), "data"));
         } finally
         {
            ((Activatable) api.getModule((Composite) entityStore.get())).activate();
         }
      }

      public String databaseSize()
      {
         Transforms.Counter<EntityState> counter = new Transforms.Counter<EntityState>();
         entityStore.get().entityStates(module).transferTo( Transforms.map( counter, Outputs.<EntityState>noop() ) );

         return "Database contains " + counter.getCount() + " objects";
      }

      public void refreshStatistics() throws StatisticsStoreException
      {
         logger.info("Start refreshing statistics");
         statistics.refreshStatistics();
         logger.info("Finished refreshing statistics");
      }

      public String performArchivalCheck()
      {
         logger.info("Start archival check");
          TransientBuilder<? extends ArchivalStartJob> newJobBuilder = module.transientBuilderFactory().newTransientBuilder(ArchivalStartJob.class);
          archivalJob = newJobBuilder.newInstance();
          logger.info("Finished archival check");

          return archivalJob.performArchivalCheck();
      }

      public void performArchival()
      {
         try
         {
            logger.info("Start archival");
             TransientBuilder<? extends ArchivalStartJob> newJobBuilder = module.transientBuilderFactory().newTransientBuilder(ArchivalStartJob.class);
             archivalJob = newJobBuilder.newInstance();
             archivalJob.performArchival();
            logger.info("Finished archival");
         } catch (UnitOfWorkCompletionException e)
         {
            logger.warn("Could not perform archival", e);
         }
      }

      public void interruptArchival()
      {
          JobKey jobKey =  JobKey.jobKey( "archivalstartjob", "archivalgroup" );
          try
          {
              // if started by Quartz let it handle interruption
              if( scheduler.isExecuting(jobKey ) )
              {
                scheduler.interruptJob( jobKey );
              } else
              {
                if( archivalJob != null )
                {
                      logger.info( "Interrupting manual archival" );
                      archivalJob.interrupt();
                }
              }
          }catch ( Exception e )
          {
              logger.error( "Could not interrupt archival", e);
          }
      }

      public void sendDueOnNotifications()
      {
         try
         {
            logger.info("Start to send dueOn notifications");
            TransientBuilder<? extends DueOnNotificationJob> newJobBuilder = module.transientBuilderFactory().newTransientBuilder( DueOnNotificationJob.class );
            DueOnNotificationJob dueOnNotificationJob = newJobBuilder.newInstance();
            dueOnNotificationJob.performNotification();
            logger.info("Finished sending dueOn notifications");
         } catch (UnitOfWorkCompletionException e)
         {
            logger.warn("Could not send dueOn notifications", e);
         }
      }

      public String importUserAndGroupsFromLdap()
      {
         try
         {
            if( ldapImporterService.getConfiguration().configuration().enabled().get() )
            {
               logger.info("Start to import users and groups");
               TransientBuilder<? extends LdapImportJob> newJobBuilder = module.transientBuilderFactory().newTransientBuilder( LdapImportJob.class )
                     .use( ldapImporterService.getConfiguration() );
               LdapImportJob ldapImportJob = newJobBuilder.newInstance();
               ldapImportJob.importUsers();
               logger.info("Finished importing users");
               ldapImportJob.importGroups();
               logger.info("Finished importing groups.");

               return "Import done successfully.";
            } else
            {
               logger.warn( "LdapImporterService is not available." );
               return "Service not available. Check LdapImporterService configuration!";
            }
         } catch (Exception e)
         {
            logger.warn("Could not complete import", e);
            return e.getMessage();
         }
      }
      
      private File getLatestBackup() throws ParseException
      {
         File latest = null;
         Date latestDate = null;

         for (File file : backup.listFiles(new FileFilter()
         {
            public boolean accept(File pathname)
            {
               return pathname.getName().startsWith("streamflow_data_");
            }
         }))
         {
            // See if backup is newer than currently found backup file
            if (latest == null || getBackupDate(file).after(latestDate))
            {
               latestDate = getBackupDate(file);
               latest = file;
            }
         }

         return latest;
      }

      private Date getBackupDate(File file) throws ParseException
      {
         String name = file.getName().substring("streamflow_data_".length());
         name = name.substring(0, name.indexOf("."));

         SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
         Date backupDate = format.parse(name);

         return backupDate;
      }

      private File[] getBackupEventFiles()
      {
         File[] files = backup.listFiles(new FileFilter()
         {
            public boolean accept(File pathname)
            {
               return pathname.getName().startsWith("streamflow_events");
            }
         });

         Arrays.sort(files, new Comparator<File>()
         {
            public int compare(File o1, File o2)
            {
               String o1Name = o1.getName();
               o1Name = o1Name.substring(0, o1Name.indexOf('.'));
               String o2Name = o2.getName();
               o2Name = o2Name.substring(0, o2Name.indexOf('.'));

               return o1Name.compareTo(o2Name);
            }
         });

         return files;
      }

      private void removeDirectory(File dir)
              throws IOException
      {
         if (dir == null || !dir.exists())
            return;

         for (File file : dir.listFiles())
         {
            if (file.isDirectory())
            {
               removeDirectory(file);
            } else
            {
               if (!file.delete())
                  throw new IOException("Could not delete file:" + file.getAbsolutePath());
            }
         }

         if (!dir.delete())
            throw new IOException("Could not delete directory:" + dir.getAbsolutePath());
      }

      private File exportEvents0(boolean compress)
              throws IOException
      {
         SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
         File exportFile = new File(exports, "streamflow_events_" + format.format(new Date()) + (compress ? ".json.gz" : ".json"));

         OutputStream out = new FileOutputStream(exportFile);

         if (compress)
         {
            out = new GZIPOutputStream(out);
         }

         final Writer writer = new OutputStreamWriter(out, "UTF-8");

         final IOException[] ex = new IOException[1];

         eventSource.transactionsAfter(0, new TransactionVisitor()
         {
            public boolean visit(TransactionDomainEvents transactionDomain)
            {
               try
               {
                  writer.write(transactionDomain.toJSON() + "\n");
               } catch (IOException e)
               {
                  ex[0] = e;
                  return false;
               }

               return true;
            }
         });

         writer.close();

         if (ex[0] != null)
         {
            exportFile.delete();
            throw ex[0];
         }

         return exportFile;
      }

      private File exportEventsRange(boolean compress, long from, final long to)
              throws IOException
      {
         SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
         File exportFile = new File(exports, "streamflow_events_" + format.format(from) + "-" + format.format(to) + (compress ? ".json.gz" : ".json"));
         OutputStream out = new FileOutputStream(exportFile);

         if (compress)
         {
            out = new GZIPOutputStream(out);
         }

         final Writer writer = new OutputStreamWriter(out, "UTF-8");

         final IOException[] ex = new IOException[1];

         eventSource.transactionsAfter(from, new TransactionVisitor()
         {
            public boolean visit(TransactionDomainEvents transactionDomain)
            {
               if (transactionDomain.timestamp().get() > to)
                  return false;

               try
               {
                  writer.write(transactionDomain.toJSON() + "\n");
               } catch (IOException e)
               {
                  ex[0] = e;
                  return false;
               }

               return true;
            }
         });

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
         return new ComputedPropertyInstance<Integer>(new GenericPropertyInfo(Manager.class, "failedLogins"))
         {
            public Integer get()
            {
               return failedLogins;
            }
         };
      }
   }
}
