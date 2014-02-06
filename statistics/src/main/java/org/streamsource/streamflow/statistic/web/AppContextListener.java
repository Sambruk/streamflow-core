/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package org.streamsource.streamflow.statistic.web;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Context listener to create and close ds on context changes.
 */
public class AppContextListener
   implements ServletContextListener
{

   static JdbcTemplate jt;
   public static JdbcTemplate getJdbcTemplate()
   {
      return jt;
   }
   public void contextInitialized( ServletContextEvent sce )
   {
      jt = new JdbcTemplate( Dao.getDataSource() );
   }

   public void contextDestroyed( ServletContextEvent sce )
   {
      try
      {
         Dao.closeDataSource();
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
