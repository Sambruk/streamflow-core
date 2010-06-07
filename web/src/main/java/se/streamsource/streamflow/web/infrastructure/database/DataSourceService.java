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

package se.streamsource.streamflow.web.infrastructure.database;

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
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
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
 * DataSource service. Sets up and exposes a DataSource that can be used in the application.
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

      Map<String, ComboPooledDataSource> pools = new HashMap<String, ComboPooledDataSource>( );

      @Structure
      UnitOfWorkFactory uowf;

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
      }

      public synchronized Object importService( ImportedServiceDescriptor importedServiceDescriptor ) throws ServiceImporterException
      {
         ComboPooledDataSource pool = pools.get( importedServiceDescriptor.identity() );
         if (pool == null)
         {
            // Instantiate pool
            pool = new ComboPooledDataSource( );

            UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase("Create DataSource pool" ));

            try
            {
               DataSourceConfiguration config = getConfiguration(uow, importedServiceDescriptor.identity());

               Class.forName( config.driver().get() );
               pool.setDriverClass(config.driver().get() );
               pool.setJdbcUrl( config.url().get());

               String props = config.properties().get();
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

               pool.setUser( config.username().get() );
               pool.setPassword( config.password().get() );
               pool.setMaxConnectionAge( 60*60 ); // One hour max age

               logger.info( "Starting up DataSource '"+importedServiceDescriptor.identity()+"' for:{}", pool.getUser()+"@"+pool.getJdbcUrl() );

               uow.complete();

               pools.put( importedServiceDescriptor.identity(), pool );
            } catch (Exception e)
            {
               uow.discard();

               throw new ServiceImporterException(e);
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
               logger.warn("Database for DataSource is not currently available");
            } finally
            {
               Thread.currentThread().setContextClassLoader( cl );
            }
         }
         return pool;
      }

      private DataSourceConfiguration getConfiguration( UnitOfWork uow, String identity ) throws InstantiationException
      {
         try
         {
            return uow.get( DataSourceConfiguration.class, identity );
         } catch (NoSuchEntityException e)
         {
            EntityBuilder<DataSourceConfiguration> configBuilder = uow.newEntityBuilder( DataSourceConfiguration.class, identity );

            // Check for defaults
            String s = identity + ".properties";
            InputStream asStream = DataSourceConfiguration.class.getClassLoader().getResourceAsStream( s );
            if( asStream != null )
            {
                try
                {
                    PropertyMapper.map( asStream, configBuilder.instance() );
                }
                catch( IOException e1 )
                {
                    InstantiationException exception = new InstantiationException( "Could not read underlying Properties file." );
                    exception.initCause( e1 );
                    throw exception;
                }
            }
            return configBuilder.newInstance();
         }
      }

      public boolean isActive( Object o )
      {
         return pools.containsValue( o );
      }
   }
}