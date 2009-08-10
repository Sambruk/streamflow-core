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

package se.streamsource.streamflow.web.application.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.entitystore.jdbm.DatabaseExport;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
@Mixins(DataExporterService.DataExporterMixin.class)
public interface DataExporterService
        extends ServiceComposite, Activatable
{
    class DataExporterMixin
            implements Activatable
    {
        @Structure
        Application app;

        @Service
        FileConfiguration config;

        @Optional
        @Service
        DatabaseExport export;

        public void activate() throws Exception
        {
            // Only do this in prodmode
            if (!app.mode().equals(Application.Mode.production) || export == null)
                return;

            Date now = new Date();
            String dateString = new SimpleDateFormat("yyyyMMddHHmm").format(now);
            String name = config.dataDirectory() + "streamflow_export_" + dateString+".json";
            File file = new File(name).getAbsoluteFile();
            FileOutputStream exportFile = new FileOutputStream(file);
            export.exportTo(new OutputStreamWriter(exportFile, "UTF-8"));
            exportFile.close();
            Logger.getLogger(getClass().getName()).info("Database exported to:"+file);
        }

        public void passivate() throws Exception
        {
        }
    }
}