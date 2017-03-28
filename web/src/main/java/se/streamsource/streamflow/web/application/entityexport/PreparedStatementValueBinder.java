package se.streamsource.streamflow.web.application.entityexport;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by ruslan on 27.03.17.
 */
abstract class PreparedStatementValueBinder<T>
{

   T value;
   private String sqlType;

   PreparedStatementValueBinder( T value, String sqlType )
   {
      this.value = value;
      this.sqlType = sqlType;
   }

   abstract void bind( PreparedStatement preparedStatement, int index ) throws SQLException;

   public String getSqlType()
   {
      return sqlType;
   }
}
