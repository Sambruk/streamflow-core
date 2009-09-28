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
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.entitystore.jdbm.DatabaseExport;
import org.qi4j.entitystore.jdbm.DatabaseImport;
import org.qi4j.index.reindexer.Reindexer;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.AllEventsSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventQuery;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

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
import java.text.SimpleDateFormat;
import java.text.ParseException;
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
        implements Manager, EventSourceListener, Activatable
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
        FileConfiguration fileConfig;

        @Service
        EventSource source;

        private int failedLogins;

        public File exports;

        public void activate() throws Exception
        {
            exports = new File(fileConfig.dataDirectory(), "exports");
            if (!exports.exists() && !exports.mkdirs())
                throw new IllegalStateException("Could not create directory for exports");

            source.registerListener(this, new EventQuery().withNames("failedLogin"));
        }

        public void passivate() throws Exception
        {
            source.unregisterListener(this);
        }

        // EventSourceListener implementation
        public void eventsAvailable(EventStore source, EventSpecification specification)
        {
            Iterable<DomainEvent> events = source.events(specification, null, Integer.MAX_VALUE);
            for (DomainEvent event : events)
            {
                if (event.name().get().equals("failedLogin"))
                {
                    failedLogins++;
                }
            }
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

                Iterable<DomainEvent> events = eventStore.events(new AllEventsSpecification(), iterableFromDate, 100);
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

        public String exportEventsRange(@Name("Compress") boolean compress, @Name("From") String fromDate, @Name("To") String toDate) throws IOException, ParseException
        {
            SimpleDateFormat parseFormat = new SimpleDateFormat("yyyyMMdd:HHmm");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");
            Date from = parseFormat.parse(fromDate);
            Date to = parseFormat.parse(toDate);
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
            do
            {
                count = 0;
                Iterable<DomainEvent> events = eventStore.events(new EventQuery().beforeDate(to), iterableFromDate, 100);

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
