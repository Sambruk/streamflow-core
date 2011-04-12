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

package se.streamsource.streamflow.web.infrastructure.database;

import liquibase.*;
import liquibase.database.*;
import liquibase.database.jvm.*;
import liquibase.resource.*;
import org.qi4j.api.configuration.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.slf4j.*;

import javax.sql.*;
import java.net.*;
import java.sql.*;

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
      ServiceReference<DataSource> dataSource;

      public void activate() throws Exception
      {
         Logger log = LoggerFactory.getLogger( LiquibaseService.class.getName() );

         boolean enabled = config.configuration().enabled().get();
         if (!enabled || !dataSource.isAvailable())
         {
            return;
         }

         Connection c = null;
         try
         {
            c = dataSource.get().getConnection();
            DatabaseConnection dc = new JdbcConnection(c);
            Liquibase liquibase = new Liquibase( config.configuration().changeLog().get(),
                  new ClassLoaderResourceAccessor(),dc );

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
         } catch (ServiceImporterException ex)
         {
            log.warn( "DataSource is not available - database refactoring skipped" );
         }
      }

      public void passivate() throws Exception
      {
      }

   }
}
