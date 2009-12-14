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

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

import javax.sql.DataSource;

/**
 * DataSource for MySQL database.
 */
@Mixins(MySQLDatabaseService.MySQLDatabaseMixin.class)
public interface MySQLDatabaseService
      extends DataSource, Configuration, Activatable, ServiceComposite
{
   class MySQLDatabaseMixin
         extends BasicDataSource
         implements Activatable
   {
      @This
      Configuration<MySQLDatabaseConfiguration> config;

      public MysqlConnectionPoolDataSource dataSource;

      public void activate() throws Exception
      {
         Class.forName( "com.mysql.jdbc.Driver" );
         setDriverClassName( "com.mysql.jdbc.Driver" );
         setUsername( config.configuration().username().get() );
         setPassword( config.configuration().password().get() );
         setUrl( "jdbc:mysql://" + config.configuration().host().get() + "/streamflow" );
      }

      public void passivate() throws Exception
      {
         close();
      }
   }
}