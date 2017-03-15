package se.streamsource.streamflow.web.application.entityexport;

import org.json.JSONException;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.structure.ModuleSPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ruslan on 15.03.17.
 */
public abstract class AbstractExportHelper
{

   protected static final String SEPARATOR = ":separator:";
   protected static final String ESCAPE_SQL = "`";

   protected Connection connection;
   protected ModuleSPI module;

   protected abstract String tableName();

   protected String processValueComposite( ValueComposite value ) throws Exception
   {
      final ValueExportHelper valueExportHelper = new ValueExportHelper();
      valueExportHelper.setValue( value );
      valueExportHelper.setConnection( connection );
      valueExportHelper.setModule( module );
      return valueExportHelper.help();

   }

   protected ResultSet selectFromWhereId( String tableName, String id ) throws SQLException, JSONException
   {
      String isExistQuery = "SELECT " + escapeSqlColumnOrTable( "identity" ) +
              " FROM " + escapeSqlColumnOrTable( tableName ) +
              " WHERE " + escapeSqlColumnOrTable( "identity" ) + " = ?";

      final PreparedStatement isExistPS = connection
              .prepareStatement( isExistQuery );
      isExistPS.setString( 1, id );
      return isExistPS.executeQuery();
   }

   protected String escapeSqlColumnOrTable( String name )
   {
      return ESCAPE_SQL + name + ESCAPE_SQL;
   }

   protected String classSimpleName( String className )
   {
      return className.substring( className.lastIndexOf( "." ) + 1 );
   }

   protected String toSnackCaseFromCamelCase( String str )
   {
      StringBuilder stringBuilder = new StringBuilder();

      //out of naming rules
      String x = str.replace( "DTO", "Dto" );

      for ( int i = 0; i < x.length(); i++ )
      {
         char ch = x.charAt( i );
         if ( i == 0 )
         {
            ch = Character.toLowerCase( ch );
         }
         if ( Character.isUpperCase( ch ) )
         {
            stringBuilder.append( '_' );
            stringBuilder.append( Character.toLowerCase( ch ) );
         } else
         {
            stringBuilder.append( ch );
         }
      }
      return stringBuilder.toString();
   }

   //setters

   public void setConnection( Connection connection )
   {
      this.connection = connection;
   }

   public void setModule( ModuleSPI module )
   {
      this.module = module;
   }

}
