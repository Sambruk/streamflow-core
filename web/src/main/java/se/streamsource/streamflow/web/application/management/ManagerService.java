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

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.entitystore.jdbm.DatabaseExport;
import org.qi4j.entitystore.jdbm.DatabaseImport;
import org.qi4j.index.reindexer.Reindexer;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * JMX Management MBean for StreamFlow
 */
@Mixins(ManagerService.ManagerMixin.class)
public interface ManagerService
    extends Manager, ServiceComposite, Activatable
{
    class ManagerMixin
        implements Manager, Activatable
    {
        @Service
        MBeanServer server;

        @Service
        Reindexer reindexer;

        @Service
        DatabaseExport exportDatabase;

        @Service
        DatabaseImport importDatabase;

        @Service
        FileConfiguration fileConfig;
        public File exports;

        public void activate() throws Exception
        {
            exports = new File(fileConfig.dataDirectory(), "exports");
            exports.mkdirs();

            // Register methods as operations
            Method[] methods = Manager.class.getMethods();
            ResourceBundle bundle = ResourceBundle.getBundle(Manager.class.getName());
            ModelMBeanOperationInfo[] operations = new ModelMBeanOperationInfo[methods.length];
            for (int i = 0; i < methods.length; i++)
            {
                Method method = methods[i];
                String name = method.getName();
                try
                {
                    name = bundle.getString(name+".name");
                } catch (MissingResourceException e)
                {
                    // Ignore
                }

                MBeanParameterInfo[] signature = new MBeanParameterInfo[method.getParameterTypes().length];
                Annotation[][] annotations = method.getParameterAnnotations();
                for (int j = 0; j < method.getParameterTypes().length; j++)
                {
                    Class<?> parameterType = method.getParameterTypes()[j];
                    Name paramName = getAnnotationOfType(annotations[j], Name.class);
                    String nameStr = paramName == null ? "param"+j : paramName.value();
                    signature[j] = new MBeanParameterInfo(nameStr, parameterType.getName(), nameStr);
                }

                ModelMBeanOperationInfo operation =
                    new ModelMBeanOperationInfo(method.getName(), name, signature, method.getReturnType().getName(), MBeanOperationInfo.ACTION);
                operations[i] = operation;
            }

            ModelMBeanInfo mmbi =
                new ModelMBeanInfoSupport(Manager.class.getName(),
                                          "StreamFlow manager",
                                          null,  // no attributes
                                          null,  // no constructors
                                          operations,
                                          null); // no notifications

            // Make the Model MBean and link it to the resource
            ModelMBean mmb = new RequiredModelMBean(mmbi);
            mmb.setManagedResource(this, "ObjectReference");

            // Register the Model MBean in the MBean Server
            ObjectName mapName = new ObjectName("StreamFlow:name=Manager");
            server.registerMBean(mmb, mapName);
        }

        public void passivate() throws Exception
        {
        }

        public void reindex()
        {
            reindexer.reindex();
        }

        public String exportDatabase() throws IOException
        {
            File exportFile = File.createTempFile("streamflow", ".json", exports);
            FileOutputStream out = new FileOutputStream(exportFile);
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            exportDatabase.exportTo(writer);
            writer.close();

            return "Database exported to "+exportFile.getAbsolutePath();
        }

        public String importDatabase(@Name("Filename") String name) throws IOException
        {
            File importFile = new File(exports, name);

            if (!importFile.exists())
                return "No such import file:"+importFile.getAbsolutePath();

            Reader in = new InputStreamReader(new FileInputStream(importFile), "UTF-8");

            try
            {
                importDatabase.importFrom(in);
            } finally
            {
                in.close();
            }

            return "Data imported successfully";
        }

        private <T extends Annotation> T getAnnotationOfType( Annotation[] annotations, Class<T> annotationType )
        {
            for( Annotation annotation : annotations )
            {
                if( annotationType.equals( annotation.annotationType() ) )
                {
                    return annotationType.cast( annotation );
                }
            }
            return null;
        }
    }
}
