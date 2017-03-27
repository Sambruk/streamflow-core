package se.streamsource.streamflow.web.application.entityexport;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by ruslan on 27.03.17.
 */
public class PreparedStatementStringBinder extends PreparedStatementValueBinder<String>
{
   PreparedStatementStringBinder( String value, String sqlType )
   {
      super( value, sqlType );
   }

   @Override
   void bind( PreparedStatement preparedStatement, int index ) throws SQLException
   {
      preparedStatement.setString( index, value );
   }
}
