package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang.ClassUtils;
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

   public static final String LINE_SEPARATOR = System.getProperty( "line.separator" );


   protected Connection connection;
   protected ModuleSPI module;
   protected DbVendor dbVendor;

   protected abstract String tableName();

   protected void createCollectionTableIfNotExist( String tableName, Map<String, Set<String>> tables, boolean isMap ) throws SQLException, ClassNotFoundException
   {

      if ( tables.get( tableName ) == null )
      {
         final StringBuilder collectionTable = new StringBuilder();

         collectionTable
                 .append( "CREATE TABLE " )
                 .append( escapeSqlColumnOrTable( tableName ) )
                 .append( " (" )
                 .append( LINE_SEPARATOR )
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "id" ) )
                 .append( " " )
                 .append( detectSqlType( Integer.class ) )
                 .append( " NOT NULL " )
                 .append( dbVendor == DbVendor.mssql ? "IDENTITY (1, 1)," : "AUTO_INCREMENT," )
                 .append( LINE_SEPARATOR );

         if ( isMap )
         {
            collectionTable
                    .append( " " )
                    .append( escapeSqlColumnOrTable( "property_key" ) )
                    .append( " " )
                    .append( stringSqlType( Integer.MAX_VALUE ) )
                    .append( " NULL," )
                    .append( LINE_SEPARATOR );
         }

         collectionTable
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "owner" ) )
                 .append( " " )
                 .append( stringSqlType( 255 ) )
                 .append( " NULL," )
                 .append( LINE_SEPARATOR );

         collectionTable
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "property_value" ) )
                 .append( " " )
                 .append( stringSqlType( Integer.MAX_VALUE ) )
                 .append( " NULL," )
                 .append( LINE_SEPARATOR )
                 .append( " PRIMARY KEY (" )
                 .append( escapeSqlColumnOrTable( "id" ) )
                 .append( ") " )
                 .append( LINE_SEPARATOR )
                 .append( ");" )
                 .append( LINE_SEPARATOR );

         final Statement statement = connection.createStatement();

         statement.executeUpdate( collectionTable.toString() );
         statement.close();


      }

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

   protected SingletonMap processValueComposite( ValueComposite value ) throws Exception
   {
      final ValueExportHelper valueExportHelper = new ValueExportHelper();
      valueExportHelper.setValue( value );
      valueExportHelper.setConnection( connection );
      valueExportHelper.setModule( module );
      valueExportHelper.setDbVendor( dbVendor );
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
      switch ( dbVendor )
      {
         case mssql:
            return "[" + name + "]";

         case oracle:
            return "\"" + name + "\"";

         default:
            return "`" + name + "`";
      }
   }

   protected String classSimpleName( String className )
   {
      return ClassUtils.getShortClassName( className );
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

   protected String detectSqlType( Class type ) throws ClassNotFoundException
   {

      if ( Boolean.class.equals( type ) )
      {
         return dbVendor == DbVendor.mssql ? "BIT" : "BIT(1)";
      } else if ( Integer.class.equals( type ) )
      {
         return dbVendor == DbVendor.mssql ? "INT" : "INT(11)";
      } else if ( Long.class.equals( type ) )
      {
         return dbVendor == DbVendor.mssql ? "BIGINT" : "BIGINT(20)";
      } else if ( Float.class.equals( type ) )
      {
         return "FLOAT";
      } else if ( Double.class.equals( type ) )
      {
         return "DOUBLE";
      } else if ( type.isEnum() )
      {
         return stringSqlType( 255 );
      } else if ( type.equals( String.class )
              || type.equals( Date.class )
              || type.equals( DateTime.class ) )
      {
         return stringSqlType( Integer.MAX_VALUE );
      } else
      {
         throw new IllegalArgumentException();
      }

   }

   protected String stringSqlType( int length )
   {

      final boolean isMax = length == Integer.MAX_VALUE;

      switch ( dbVendor )
      {
         case mssql:
            return isMax ? "NTEXT" : "NVARCHAR(" + length + ")";

         default:
            return isMax ? "TEXT" : "VARCHAR(" + length + ")";
      }
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

   public void setDbVendor( DbVendor dbVendor )
   {
      this.dbVendor = dbVendor;
   }
}
