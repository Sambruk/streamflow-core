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

package se.streamsource.streamflow.infrastructure.configuration;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;

import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Service for accessing application-specific directories. These will default to
 * the platform settings, but can be overridden manually, either one-by-one or as a whole.
 * <p/>
 * Services will most likely want to create their own subdirectories in the directories accessed
 * from here.
 */
@Concerns({FileConfiguration.AutoCreateDirectoriesConcern.class,
        FileConfiguration.DeleteTestDataConcern.class})
@Mixins(FileConfiguration.FileConfigurationMixin.class)
public interface FileConfiguration
        extends ServiceComposite, Activatable
{
    public enum OS
    {
        windows, unix, mac
    }

    OS os();

    File configurationDirectory();

    File dataDirectory();

    File temporaryDirectory();

    File cacheDirectory();

    File logDirectory();

    interface FileState
    {
        Property<OS> os();

        Property<String> application();

        Property<File> configuration();

        Property<File> data();

        Property<File> temporary();

        Property<File> cache();

        Property<File> log();
    }

    abstract class FileConfigurationMixin
            implements FileConfiguration, Activatable
    {
        @This
        FileState state;

        public void activate() throws Exception
        {
            String osName = System.getProperty("os.name").toLowerCase();
            OS os;
            if (osName.indexOf("win") != -1)
                os = OS.windows;
            else if (osName.indexOf("mac") != -1)
                os = OS.mac;
            else
                os = OS.unix;

            state.os().set(os);

            Logger.getLogger(getClass().getName()).info("Operating system:" + os.name());

            // Get bundle with application name and configured directories
            String user = System.getProperty("user.home");
            ResourceBundle bundle = ResourceBundle.getBundle(FileConfiguration.class.getName(), new Locale(os.name()));

            // Set application name. This is taken from the bundle but can be overriden by a system property
            String application = System.getProperty("application", bundle.getString("application"));
            state.application().set(application);

            // Temp dir
            String temp = System.getProperty("java.io.tmpdir");

            // Arguments available to use in directory specifications
            String[] args = new String[]{application, user, os.name(), temp};

            state.configuration().set(new File(MessageFormat.format(bundle.getString("configuration"), args)));
            state.data().set(new File(MessageFormat.format(bundle.getString("data"), args)));
            state.temporary().set(new File(MessageFormat.format(bundle.getString("temporary"), args)));
            state.cache().set(new File(MessageFormat.format(bundle.getString("cache"), args)));
            state.log().set(new File(MessageFormat.format(bundle.getString("log"), args)));
        }

        public void passivate() throws Exception
        {
        }

        public OS os()
        {
            return state.os().get();
        }

        public File configurationDirectory()
        {
            return state.configuration().get();
        }

        public File dataDirectory()
        {
            return state.data().get();
        }

        public File temporaryDirectory()
        {
            return state.temporary().get();
        }

        public File cacheDirectory()
        {
            return state.cache().get();
        }

        public File logDirectory()
        {
            return state.log().get();
        }
    }

    class AutoCreateDirectoriesConcern
            extends ConcernOf<Activatable>
            implements Activatable
    {
        @Structure
        Application application;

        @This
        FileConfiguration config;

        public void activate() throws Exception
        {
            next.activate();

            if (application.mode().equals(Application.Mode.production))
            {
                // Create directories
                if (!config.configurationDirectory().mkdirs())
                    throw new IllegalStateException("Could not create configuration directory(" + config.configurationDirectory() + ")");
                if (!config.dataDirectory().mkdirs())
                    throw new IllegalStateException("Could not create data directory(" + config.dataDirectory() + ")");
                if (!config.temporaryDirectory().mkdirs())
                    throw new IllegalStateException("Could not create temporary directory(" + config.temporaryDirectory() + ")");
                if (!config.cacheDirectory().mkdirs())
                    throw new IllegalStateException("Could not create cache directory(" + config.cacheDirectory() + ")");
                if (!config.logDirectory().mkdirs())
                    throw new IllegalStateException("Could not create log directory(" + config.logDirectory() + ")");
            }
        }

        public void passivate() throws Exception
        {
            next.passivate();
        }
    }

    class DeleteTestDataConcern
            extends ConcernOf<Activatable>
            implements Activatable
    {
        @Structure
        Application application;

        @This
        FileConfiguration config;

        public void activate() throws Exception
        {
            next.activate();
        }

        public void passivate() throws Exception
        {
            if (application.mode().equals(Application.Mode.test))
            {
                // Delete test data
                delete(config.configurationDirectory());
                delete(config.dataDirectory());
                delete(config.temporaryDirectory());
                delete(config.cacheDirectory());
                delete(config.logDirectory());
            }
        }

        private boolean delete(File file)
        {
            if (file.isFile())
            {
                return file.delete();
            } else
            {
                for (File childFile : file.listFiles())
                {
                    if (!delete(childFile))
                        return false;
                }

                return file.delete();
            }
        }
    }
}