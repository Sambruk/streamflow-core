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

package se.streamsource.streamflow.web.infrastructure.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.structure.Module;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * DataSource service. Sets up and exposes a DataSource that can be used in the application.
 */
@Mixins(DataSourceService.Mixin.class)
public interface DataSourceService
      extends ServiceImporter, Configuration, Activatable, ServiceComposite
{
   class Mixin
         implements Activatable, ServiceImporter
   {
      @This
      Configuration<DatabaseConfiguration> config;

      ComboPooledDataSource pool;

      @Structure
      Module module;

      public void activate() throws Exception
      {
         pool = new ComboPooledDataSource( );

         Class.forName( "com.mysql.jdbc.Driver" );
         pool.setDriverClass("com.mysql.jdbc.Driver" );
         pool.setJdbcUrl( "jdbc:mysql://" + config.configuration().host().get() + "/streamflow" );

         String props = config.configuration().properties().get();
         String[] properties = props.split( "," );
         Properties poolProperties = new Properties();
         for (String property : properties)
         {
            if (property.trim().length() > 0)
            {
               String[] keyvalue = property.trim().split("=" );
               poolProperties.setProperty( keyvalue[0], keyvalue[1] );
            }
         }
         pool.setProperties( poolProperties );

         pool.setUser( config.configuration().username().get() );
         pool.setPassword( config.configuration().password().get() );
         pool.setMaxConnectionAge( 60*60 ); // One hour max age

         LoggerFactory.getLogger( DataSourceService.class ).info( "Starting up DataSource for:{}", pool.getUser()+"@"+pool.getJdbcUrl() );

         // Test the pool
         pool.getConnection().close();
      }

      public void passivate() throws Exception
      {
         DataSources.destroy( pool ); 
         pool = null;
      }

      public Object importService( ImportedServiceDescriptor importedServiceDescriptor ) throws ServiceImporterException
      {
         return pool;
      }

      public boolean isActive( Object o )
      {
         return pool != null;
      }
   }
}