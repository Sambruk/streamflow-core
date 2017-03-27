package se.streamsource.streamflow.web.application.entityexport;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by ruslan on 27.03.17.
 */
public class PreparedStatementIntegerBinder extends PreparedStatementValueBinder<Integer>
{
   PreparedStatementIntegerBinder( Integer value, String sqlType )
   {
      super( value, sqlType );
   }

   @Override
   void bind( PreparedStatement preparedStatement, int index ) throws SQLException
   {
      preparedStatement.setInt( index, value );
   }

}
