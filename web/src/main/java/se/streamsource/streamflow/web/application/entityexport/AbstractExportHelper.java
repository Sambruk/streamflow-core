package se.streamsource.streamflow.web.application.entityexport;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.structure.ModuleSPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

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

   protected String processCollection( Object value, String key ) throws SQLException, JSONException
   {
      final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( key );
      StringBuilder result = new StringBuilder();
      if ( value instanceof Collection )
      {
         for ( Object o : ( Collection<?> ) value )
         {

            String query = "INSERT INTO " +
                    escapeSqlColumnOrTable( tableName )+
                    " (" + escapeSqlColumnOrTable( "property_value" )+ ") VALUES (?)";

            PreparedStatement preparedStatement = connection.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );
            preparedStatement.setString( 1, o.toString() );
            preparedStatement.executeUpdate();
            final ResultSet generatedKey = preparedStatement.getGeneratedKeys();
            generatedKey.next();
            int id = generatedKey.getInt( 1 );
            if ( result.length() > 0 )
            {
               result.append( SEPARATOR );
            }
            result.append( tableName )
                    .append( ";" )
                    .append( id );
            preparedStatement.close();
         }
      } else
      {
         final Map<?, ?> map = ( Map<?, ?> ) value;
         final Set<?> keySet = map.keySet();

         for ( Object o : keySet )
         {
            final String objKey = o.toString();
            final String objValue = map.get( o ).toString();

            String query = "INSERT INTO " +
                    tableName +
                    " (" + escapeSqlColumnOrTable( "property_key"  )+ "," + escapeSqlColumnOrTable( "property_value" )+ ") VALUES (?,?)";

            PreparedStatement preparedStatement = connection.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );
            preparedStatement.setString( 1, objKey );
            preparedStatement.setString( 2, objValue );
            preparedStatement.executeUpdate();
            final ResultSet generatedKey = preparedStatement.getGeneratedKeys();
            generatedKey.next();
            int id = generatedKey.getInt( 1 );
            if ( result.length() > 0 )
            {
               result.append( SEPARATOR );
            }
            result.append( tableName )
                    .append( ";" )
                    .append( id );
            preparedStatement.close();
         }

      }

      return result.toString();
   }

   protected void setSimpleType( final PreparedStatement statement,
                               final Class clazz,
                               final JSONObject jsonObj,
                               final String name,
                               final int i ) throws JSONException, SQLException
   {
      if ( Boolean.class.equals( clazz ) )
      {
         statement.setBoolean( i, jsonObj.getBoolean( name ) );
      } else if ( Integer.class.equals( clazz ) )
      {
         statement.setInt( i, jsonObj.getInt( name ) );
      } else if ( Long.class.equals( clazz ) )
      {
         statement.setLong( i, jsonObj.getLong( name ) );
      } else if ( Float.class.equals( clazz ) )
      {
         statement.setFloat( i, ( float ) jsonObj.getDouble( name ) );
      } else if ( Double.class.equals( clazz ) )
      {
         statement.setDouble( i, jsonObj.getDouble( name ) );
      } else if ( String.class.equals( clazz )
              || clazz.isEnum()
              || Date.class.equals( clazz )
              || DateTime.class.equals( clazz ) )
      {
         statement.setString( i, jsonObj.getString( name ) );
      } else
      {
         throw new IllegalArgumentException();
      }
   }

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
