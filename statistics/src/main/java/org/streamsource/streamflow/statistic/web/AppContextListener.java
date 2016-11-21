/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
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
