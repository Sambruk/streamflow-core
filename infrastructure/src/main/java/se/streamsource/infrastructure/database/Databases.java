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
package se.streamsource.infrastructure.database;

import se.streamsource.streamflow.util.Visitor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

   public void query(String sql, ResultSetVisitor visitor) throws SQLException
   {
      query(sql, null, visitor);
   }

   public  void query(String sql, StatementVisitor statement, ResultSetVisitor resultsetVisitor) throws SQLException
   {
      Connection connection = source.getConnection();
      try
      {
         PreparedStatement stmt = connection.prepareStatement( sql);
         if (statement != null)
            statement.visit( stmt );
         try
         {
            ResultSet resultSet = stmt.executeQuery();
            try
            {
               while (resultSet.next())
               {
                  if (!resultsetVisitor.visit( resultSet ))
                     return;
               }
            } finally
            {
               resultSet.close();
            }
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
      void visit( PreparedStatement preparedStatement)
         throws SQLException;
   }

   public interface ResultSetVisitor
      extends Visitor<ResultSet, SQLException>
   {

   }
}
