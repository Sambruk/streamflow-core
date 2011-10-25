/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.infrastructure.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.qi4j.api.composite.PropertyMapper;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * DataSource service implemented as a ServiceImporter. Sets up and exposes DataSources that can be used in the application.
 */
@Mixins(DataSourceService.Mixin.class)
public interface DataSourceService
      extends ServiceImporter, Activatable, ServiceComposite
{
   class Mixin
         implements Activatable, ServiceImporter
   {
      @Structure
      Module module;

      Map<String, ComboPooledDataSource> pools = new HashMap<String, ComboPooledDataSource>();
      Map<String, DataSourceConfiguration> configs = new HashMap<String, DataSourceConfiguration>();

      Logger logger = LoggerFactory.getLogger( DataSourceService.class );

      public void activate() throws Exception
      {
      }

      public void passivate() throws Exception
      {
         for (ComboPooledDataSource pool : pools.values())
         {
            DataSources.destroy( pool );
         }
         pools.clear();

         for (DataSourceConfiguration dataSourceConfiguration : configs.values())
         {
            module.unitOfWorkFactory().getUnitOfWork(dataSourceConfiguration).discard();
         }
         configs.clear();
      }

      public synchronized Object importService( ImportedServiceDescriptor importedServiceDescriptor ) throws ServiceImporterException
      {
         ComboPooledDataSource pool = pools.get( importedServiceDescriptor.identity() );
         if (pool == null)
         {
            // Instantiate pool
            pool = new ComboPooledDataSource();

            try
            {
               DataSourceConfiguration config = getConfiguration( importedServiceDescriptor.identity() );

               if (config.enabled().get())
               {
                  Class.forName( config.driver().get() );
                  pool.setDriverClass( config.driver().get() );
                  pool.setJdbcUrl( config.url().get() );

                  String props = config.properties().get();
                  String[] properties = props.split( "," );
                  Properties poolProperties = new Properties();
                  for (String property : properties)
                  {
                     if (property.trim().length() > 0)
                     {
                        String[] keyvalue = property.trim().split( "=" );
                        poolProperties.setProperty( keyvalue[0], keyvalue[1] );
                     }
                  }
                  pool.setProperties( poolProperties );

                  pool.setUser( config.username().get() );
                  pool.setPassword( config.password().get() );
                  pool.setMaxConnectionAge( 60 * 60 ); // One hour max age

                  logger.info( "Starting up DataSource '" + importedServiceDescriptor.identity() + "' for:{}", pool.getUser() + "@" + pool.getJdbcUrl() );
               } else
               {
                  // Not started
                  throw new ServiceImporterException( "DataSource not enabled" );
               }

               pools.put( importedServiceDescriptor.identity(), pool );
            } catch (Exception e)
            {
               throw new ServiceImporterException( e );
            }

            // Test the pool
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( null );
            try
            {
               pool.getConnection().close();
               logger.info( "Database for DataSource is up!" );
            } catch (SQLException e)
            {
               logger.warn( "Database for DataSource " + importedServiceDescriptor.identity() + " is not currently available" );
               throw new ServiceImporterException( "Database for DataSource " + importedServiceDescriptor.identity() + " is not currently available", e );
            } finally
            {
               Thread.currentThread().setContextClassLoader( cl );
            }
         }
         return pool;
      }

      public synchronized DataSourceConfiguration getConfiguration( String identity ) throws InstantiationException
      {
         DataSourceConfiguration config = configs.get( identity );
         if (config == null)
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Create DataSource pool configuration"));

            try
            {
               config = uow.get( DataSourceConfiguration.class, identity );
            } catch (NoSuchEntityException e)
            {
               EntityBuilder<DataSourceConfiguration> configBuilder = uow.newEntityBuilder( DataSourceConfiguration.class, identity );

               // Check for defaults
               String s = identity + ".properties";
               InputStream asStream = DataSourceConfiguration.class.getClassLoader().getResourceAsStream( s );
               if (asStream != null)
               {
                  try
                  {
                     PropertyMapper.map( asStream, configBuilder.instance() );
                  } catch (IOException e1)
                  {
                     uow.discard();
                     InstantiationException exception = new InstantiationException( "Could not read underlying Properties file." );
                     exception.initCause( e1 );
                     throw exception;
                  }
               }

               config = configBuilder.newInstance();


               try
               {
                  // save
                  uow.complete();
                  // create new uow and fetch entity
                  uow = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Create DataSource pool configuration"));
                  config = uow.get( DataSourceConfiguration.class, identity );
               } catch (UnitOfWorkCompletionException e2)
               {
                  InstantiationException exception = new InstantiationException( "Could not save configuration in JavaPreferences." );
                     exception.initCause( e2 );
                     throw exception;
               }
            }

            configs.put( identity, config );
         }

         return config;
      }

      public boolean isActive( Object instance )
      {
         return pools.containsValue( instance );
      }

      public boolean isAvailable( Object instance )
      {
         return pools.containsValue( instance );
      }
   }
}