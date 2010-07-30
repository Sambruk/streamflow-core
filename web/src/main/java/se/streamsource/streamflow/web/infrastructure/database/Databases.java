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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Utility methods for performing SQL calls
 */
public class Databases
{
   DataSource source;

   public Databases( DataSource source )
   {
      this.source = source;
   }

   public int update(String sql) throws SQLException
   {
      Connection connection = source.getConnection();
      try
      {
         PreparedStatement stmt = connection.prepareStatement( sql);
         try
         {
            return stmt.executeUpdate();
         } finally
         {
            stmt.close();
         }
      } finally
      {
         connection.close();
      }
   }

   public int update(String sql, StatementVisitor visitor) throws SQLException
   {
      Connection connection = source.getConnection();
      try
      {
         PreparedStatement stmt = connection.prepareStatement( sql);
         try
         {
            visitor.visit( stmt );
            return stmt.executeUpdate();
         } finally
         {
            stmt.close();
         }
      } finally
      {
         connection.close();
      }
   }

   public interface StatementVisitor
   {
      void visit(PreparedStatement statement) throws SQLException;
   }
}
