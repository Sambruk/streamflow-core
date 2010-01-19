/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

import liquibase.FileOpener;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;

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
      private Logger log;

      public void activate() throws Exception
      {
         log = Logger.getLogger( LiquibaseService.class.getName() );

         boolean shouldRunProperty = config.configuration().shouldRunLiquibase().get();
         if (!shouldRunProperty)
         {
            log.info( "LiquiBase did not run" );
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
            if (c != null)
               try
               {
                  c.rollback();
                  c.close();
               }
               catch (SQLException ex)
               {
               }
            throw new JDBCException( e );
         }
      }

      public void passivate() throws Exception
      {
      }

   }
}
