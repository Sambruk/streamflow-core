/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.application.management;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.entitystore.jdbm.DatabaseExport;
import org.qi4j.entitystore.jdbm.DatabaseImport;
import org.qi4j.index.reindexer.Reindexer;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entity.EntityState;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventFilter;
import se.streamsource.streamflow.infrastructure.event.source.EventQuery;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.infrastructure.event.EventManagement;

import java.io.File;
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
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * JAVADOC
 */
@Mixins(ManagerComposite.ManagerMixin.class)
public interface ManagerComposite
    extends Manager, Activatable, TransientComposite
{
    abstract class ManagerMixin
        implements Manager, Activatable
    {
        @Service
        Reindexer reindexer;

        @Service
        DatabaseExport exportDatabase;

        @Service
        DatabaseImport importDatabase;

        @Service
        EventStore eventStore;

        @Service
        EventManagement eventManagement;

        @Service
        FileConfiguration fileConfig;

        @Service
        EventSource source;

        @Service
        EntityStore entityStore;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        Module module;

        private int failedLogins;

        public File exports;

        public EventSourceListener failedLoginListener;

        public void activate() throws Exception
        {
            exports = new File(fileConfig.dataDirectory(), "exports");
            if (!exports.exists() && !exports.mkdirs())
                throw new IllegalStateException("Could not create directory for exports");

            failedLoginListener = new EventSourceListener()
            {
                private EventFilter filter = new EventFilter(new EventQuery().withNames("failedLogin"));
                public void eventsAvailable(EventStore source)
                {
                    Iterable<DomainEvent> events = filter.events(source.events(null, Integer.MAX_VALUE));
                    for (DomainEvent event : events)
                    {
                        failedLogins++;
                    }
                }
            };
            source.registerListener(failedLoginListener);
        }

        public void passivate() throws Exception
        {
            source.unregisterListener(failedLoginListener);
        }

        // Operations
        public void reindex()
        {
            reindexer.reindex();
        }

        public String exportDatabase(boolean compress) throws IOException
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");
            File exportFile = new File(exports, "streamflow_data_" + format.format(new Date()) + (compress ? ".json.gz" : ".json"));
            OutputStream out = new FileOutputStream(exportFile);

            if (compress)
            {
                out = new GZIPOutputStream(out);
            }

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            exportDatabase.exportTo(writer);
            writer.close();

            return "Database exported to " + exportFile.getAbsolutePath();
        }

        public String importDatabase(@Name("Filename") String name) throws IOException
        {
            File importFile = new File(exports, name);

            if (!importFile.exists())
                return "No such import file:" + importFile.getAbsolutePath();

            InputStream in1 = new FileInputStream(importFile);
            if (importFile.getName().endsWith("gz"))
            {
            	in1 = new GZIPInputStream(in1);
            }
            Reader in = new InputStreamReader(in1, "UTF-8");
            try
            {
                importDatabase.importFrom(in);
            } finally
            {
                in.close();
            }

            return "Data imported successfully";
        }

        public String importEvents(@Name("Filename") String name) throws IOException
        {
            File importFile = new File(exports, name);

            if (!importFile.exists())
                return "No such import file:" + importFile.getAbsolutePath();

            InputStream in1 = new FileInputStream(importFile);
            if (importFile.getName().endsWith("gz"))
            {
            	in1 = new GZIPInputStream(in1);
            }
            Reader in = new InputStreamReader(in1, "UTF-8");
            try
            {
                eventManagement.importEvents(in);
            } finally
            {
                in.close();
            }

            return "Data imported successfully";
        }

        public String exportEvents(@Name("Compress") boolean compress) throws IOException
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");
            File exportFile = new File(exports, "streamflow_events_" + format.format(new Date()) + (compress ? ".json.gz" : ".json"));
            OutputStream out = new FileOutputStream(exportFile);

            if (compress)
            {
                out = new GZIPOutputStream(out);
            }

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            Date iterableFromDate = null;
            int count;
            do
            {
                count = 0;

                Iterable<TransactionEvents> events = eventStore.events(iterableFromDate, 100);
                for (TransactionEvents event : events)
                {
                    writer.write(event.toJSON()+"\n");
                    count++;
                    iterableFromDate = new Date(event.timestamp().get());
                }

            } while (count > 0);

            writer.close();

            return "Events exported to " + exportFile.getAbsolutePath();
        }

        public String exportEventsRange(@Name("Compress") boolean compress, @Name("From") String fromDate, @Name("To") String toDate) throws IOException, ParseException
        {
            SimpleDateFormat parseFormat = new SimpleDateFormat("yyyyMMdd:HHmm");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");

            Date from = parseFormat.parse(fromDate);

            Date to;
            if (toDate == null)
            {
                // Set "to"-date to "now"
                to = new Date();
                toDate = format.format(to);
            } else
            {
                to = parseFormat.parse(toDate);
            }

            File exportFile = new File(exports, "streamflow_events_" + format.format(from)+"-"+format.format(to) + (compress ? ".json.gz" : ".json"));
            OutputStream out = new FileOutputStream(exportFile);

            if (compress)
            {
                out = new GZIPOutputStream(out);
            }

            Writer writer = new OutputStreamWriter(out, "UTF-8");

            int count;
            Date iterableFromDate = from;
            // Write 100 events at a time. Stop when no more events are found that matches the specification.
            EventFilter filter = new EventFilter(new EventQuery().beforeDate(to));
            do
            {
                count = 0;
                Iterable<DomainEvent> events = filter.events(eventStore.events(iterableFromDate, 100));

                for (DomainEvent event : events)
                {
                    writer.write(event.toJSON()+"\n");
                    count++;
                    iterableFromDate = event.on().get();
                }
            } while (count > 0);

            writer.close();

            return "Events exported to " + exportFile.getAbsolutePath();
        }

        public String restoreFromBackup()
        {
/*
            // Restore data from latest export in /exports
            File latestBackup = getLatestBackup();
            importDatabase(latestBackup.getAbsolutePath());

            // Reindex state
            reindex();

            // Add events from time of lateist backup
            eventManagement.removeAll();

            File[] eventFiles = getEventFilesSince(latestBackup);

            for (File eventFile : eventFiles)
            {
                InputStream
                if (eventFile.getName().endsWith(".gz"))
                {

                }
                eventManagement.importEvents(eventFile);
            }

            eventManagement.replayFrom();
*/

            return null;
        }

        public String generateTestData(@Name("Nr of tasks") int nrOfTasks) throws UnitOfWorkCompletionException
        {
            UnitOfWork uow = uowf.newUnitOfWork();

            Inbox inbox = uow.get(Inbox.class, "administrator");

            for (int i = 0; i < nrOfTasks; i++)
            {
                inbox.createTask().changeDescription("Task "+i);
            }

            uow.complete();

            return "Created "+nrOfTasks+" in Administrators inbox";
        }

        public String databaseSize()
        {
            final int[] count = {0};
            entityStore.visitEntityStates(new EntityStore.EntityStateVisitor()
            {
                public void visitEntityState(EntityState entityState)
                {
                    count[0]++;
                }
            }, module);

            return "Database contains "+count[0]+" objects";
        }

        private File getLatestBackup() throws ParseException
        {
            File latest = null;
            Date latestDate = null;

            for (File file : exports.listFiles())
            {
                // See if backup is newer than currently found backup file
                if (latest == null || getBackupDate(file).after(latestDate))
                {
                    latestDate = getBackupDate(file);
                }
            }

            return latest;
        }

        private Date getBackupDate(File file) throws ParseException
        {
            String name = file.getName().substring("streamflow_data_".length());
            name = name.substring(0, name.indexOf("."));

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");
            Date backupDate = format.parse(name);

            return backupDate;
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
