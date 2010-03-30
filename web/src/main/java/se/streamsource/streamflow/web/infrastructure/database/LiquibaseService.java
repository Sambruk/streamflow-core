/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.infrastructure.database;

import liquibase.FileOpener;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Wrapper service for LiquiBase
 */
@Mixins(LiquibaseService.Mixin.class)
public interface LiquibaseService
      extends Activatable, ServiceComposite
{
   class Mixin
         implements Activatable
   {
      @This
      Configuration<LiquibaseConfiguration> config;

      @Service
      DataSource dataSource;

      public void activate() throws Exception
      {
         Logger log = LoggerFactory.getLogger( LiquibaseService.class.getName() );

         boolean enabled = config.configuration().enabled().get();
         if (!enabled)
         {
            return;
         }

         Connection c = null;
         try
         {
            c = dataSource.getConnection();
            Liquibase liquibase = new Liquibase( config.configuration().changeLog().get(),
                  new FileOpener()
                  {
                     public InputStream getResourceAsStream( String s ) throws IOException
                     {
                        return getClass().getClassLoader().getResourceAsStream( s );
                     }

                     public Enumeration<URL> getResources( String s ) throws IOException
                     {
                        return getClass().getClassLoader().getResources( s );
                     }

                     public ClassLoader toClassLoader()
                     {
                        return getClass().getClassLoader();
                     }
                  },
                  DatabaseFactory.getInstance().findCorrectDatabaseImplementation( c ) );

            liquibase.update( config.configuration().contexts().get() );
         }
         catch (SQLException e)
         {
            Throwable ex = e;
            while (ex.getCause() != null)
               ex = ex.getCause();

            if (ex instanceof ConnectException)
            {
               log.warn( "Could not connect to database; LiquiBase should be disabled" );
               return;
            }

            log.error( "LiquiBase could not perform database migration", e );

            if (c != null)
               try
               {
                  c.rollback();
                  c.close();
               }
               catch (SQLException ex1)
               {
               }
         }
      }

      public void passivate() throws Exception
      {
      }

   }
}
