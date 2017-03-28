package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang.ClassUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.api.administration.form.FieldValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ruslan on 15.03.17.
 */
public abstract class AbstractExportHelper
{

   public static final String LINE_SEPARATOR = System.getProperty( "line.separator" );
   protected final Logger logger = LoggerFactory.getLogger( this.getClass().getName() );

   protected Connection connection;
   protected ModuleSPI module;
   protected DbVendor dbVendor;
   protected Map<String, Set<String>> tables;

   protected abstract String tableName();

   protected void createSubPropertyTableIfNotExists() throws SQLException
   {
      if ( tables.get( tableName() ) == null )
      {

         final StringBuilder subPropertyTable = new StringBuilder();

         final HashSet<String> columns = new HashSet<>();

         subPropertyTable
                 .append( "CREATE TABLE " )
                 .append( escapeSqlColumnOrTable( tableName() ) )
                 .append( " (" )
                 .append( LINE_SEPARATOR )
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "id" ) )
                 .append( " " )
                 .append( detectSqlType( Integer.class ) )
                 .append( " NOT NULL " )
                 .append( dbVendor == DbVendor.mysql ? "AUTO_INCREMENT," : "identity(1,1)," )
                 .append( LINE_SEPARATOR )
                 .append( " PRIMARY KEY (" )
                 .append( escapeSqlColumnOrTable( "id" ) )
                 .append( ")" )
                 .append( LINE_SEPARATOR )
                 .append( ");" );
         columns.add( "id" );

         final Statement statement = connection.createStatement();

         statement.executeUpdate( subPropertyTable.toString() );

         tables.put( tableName(), columns );
      }
   }

   void processCollection( String name,
                                   Object value,
                           PreparedStatementValueBinder valueBinder
   ) throws SQLException, JSONException, ClassNotFoundException
   {
      final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( name ) + "_coll";

      final boolean isMap = value instanceof Map;

      createCollectionTableIfNotExist( tableName, isMap, valueBinder );

      final Collection<?> objects = isMap ? ( ( Map<?, ?> ) value ).keySet() : ( Collection<?> ) value;

      String query = "INSERT INTO "
              + escapeSqlColumnOrTable( tableName )
              + " ("
              + escapeSqlColumnOrTable( "owner" )
              + "," + escapeSqlColumnOrTable( "property_value" )
              + ( isMap ? "," + escapeSqlColumnOrTable( "property_key" ) : "" )
              + ") VALUES (?,?" + ( isMap ? ",?)" : ")" );

      try ( final PreparedStatement preparedStatement = connection.prepareStatement( query ) )
      {
         for ( Object o : objects )
         {
            valueBinder.bind( preparedStatement, 1 );
            final String strValue = isMap ? ( ( Map<?, ?> ) value ).get( o ).toString() : o.toString();
            preparedStatement.setString( 2, strValue );

            if ( isMap )
            {
               preparedStatement.setString( 3, o.toString() );
            }
            preparedStatement.addBatch();
         }

         preparedStatement.executeBatch();
      }

   }

   void createCollectionTableIfNotExist( String tableName, boolean isMap, PreparedStatementValueBinder valueBinder ) throws SQLException, ClassNotFoundException
   {

      if ( tables.get( tableName ) == null )
      {
         final StringBuilder collectionTable = new StringBuilder();

         final HashSet<String> columns = new HashSet<>();

         collectionTable
                 .append( "CREATE TABLE " )
                 .append( escapeSqlColumnOrTable( tableName ) )
                 .append( " (" )
                 .append( LINE_SEPARATOR )
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "owner" ) )
                 .append( " " )
                 .append( valueBinder.getSqlType() )
                 .append( " NOT NULL," )
                 .append( LINE_SEPARATOR );
         columns.add( "owner" );

         if ( isMap )
         {
            collectionTable
                    .append( " " )
                    .append( escapeSqlColumnOrTable( "property_key" ) )
                    .append( " " )
                    .append( stringSqlType( Integer.MAX_VALUE ) )
                    .append( " NULL," )
                    .append( LINE_SEPARATOR );
            columns.add( "property_key" );
         }

         final int hashCodeOwner = tableName.hashCode();
         collectionTable
                 .append( " CONSTRAINT FK_owner_" )
                 .append( hashCodeOwner >= 0 ? hashCodeOwner : ( -1 * hashCodeOwner ) )
                 .append( " FOREIGN KEY (" )
                 .append( escapeSqlColumnOrTable( "owner" ) )
                 .append( ") REFERENCES " )
                 .append( escapeSqlColumnOrTable( tableName() ) )
                 .append( " (" )
                 .append( valueBinder.getSqlType().equals( stringSqlType( 255 ) )
                         ? escapeSqlColumnOrTable( "identity" ) : escapeSqlColumnOrTable( "id" ) )
                 .append( ")," )
                 .append( LINE_SEPARATOR );

         collectionTable
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "property_value" ) )
                 .append( " " )
                 .append( stringSqlType( Integer.MAX_VALUE ) )
                 .append( " NULL" )
                 .append( LINE_SEPARATOR )
                 .append( ");" )
                 .append( LINE_SEPARATOR );
         columns.add( "property_value" );


         try ( final Statement statement = connection.createStatement() )
         {
            statement.executeUpdate( collectionTable.toString() );
         }

         tables.put( tableName, columns );
      }

   }

   void createCrossRefTableIfNotExists( String tableName,
                                        Map<String, Set<String>> tableColumns,
                                        String associationTable,
                                        String ownerType,
                                        String linkType ) throws ClassNotFoundException, SQLException
   {

      if ( tableColumns.get( tableName ) == null )
      {

         final StringBuilder manyAssoc = new StringBuilder();

         manyAssoc
                 .append( "CREATE TABLE " )
                 .append( escapeSqlColumnOrTable( tableName ) )
                 .append( " (" )
                 .append( LINE_SEPARATOR )
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( " " )
                 .append( ownerType )
                 .append( " NOT NULL," )
                 .append( LINE_SEPARATOR )
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "link_id" ) )
                 .append( " " )
                 .append( linkType )
                 .append( " NOT NULL," )
                 .append( LINE_SEPARATOR );

         final HashSet<String> columns = new HashSet<>();
         columns.add( "owner_id" );
         columns.add( "link_id" );

         tableColumns.put( tableName, columns );

         final int hashCodeOwner = ( tableName() + tableName + "owner" ).hashCode();
         manyAssoc
                 .append( " CONSTRAINT FK_owner_" )
                 .append( hashCodeOwner >= 0 ? hashCodeOwner : ( -1 * hashCodeOwner ) )
                 .append( " FOREIGN KEY (" )
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( ") REFERENCES " )
                 .append( escapeSqlColumnOrTable( tableName() ) )
                 .append( " (" )
                 .append( stringSqlType( 255 ).equals( ownerType ) ? escapeSqlColumnOrTable( "identity" ) : escapeSqlColumnOrTable( "id" ) )
                 .append( ")," )
                 .append( LINE_SEPARATOR );

         if ( associationTable != null )
         {
            final int hashCodeLink = ( tableName() + tableName + "link" ).hashCode();
            manyAssoc
                    .append( " CONSTRAINT FK_link_" )
                    .append( hashCodeLink >= 0 ? hashCodeLink : ( -1 * hashCodeLink ) )
                    .append( " FOREIGN KEY (" )
                    .append( escapeSqlColumnOrTable( "link_id" ) )
                    .append( ") REFERENCES " )
                    .append( escapeSqlColumnOrTable( associationTable ) )
                    .append( " (" )
                    .append( stringSqlType( 255 ).equals( linkType ) ? escapeSqlColumnOrTable( "identity" ) : escapeSqlColumnOrTable( "id" ) )
                    .append( ")," )
                    .append( LINE_SEPARATOR );
         }

         manyAssoc
                 .append( " PRIMARY KEY (" )
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( "," )
                 .append( escapeSqlColumnOrTable( "link_id" ) )
                 .append( ")" )
                 .append( LINE_SEPARATOR )
                 .append( ");" );

         try ( final Statement statement = connection.createStatement() )
         {
            statement.executeUpdate( manyAssoc.toString() );
         }

         logger.info( manyAssoc.toString() );
      }

   }

   protected boolean addColumn( String name, Class<?> type, Statement statement ) throws SQLException
   {
      final Set<String> columns = tables.get( tableName() );

      assert columns != null;

      final boolean columnAdded = columns.add( name );
      if ( columnAdded )
      {
         final String alterTable = "ALTER TABLE " +
                 escapeSqlColumnOrTable( tableName() ) +
                 LINE_SEPARATOR +
                 "ADD " +
                 escapeSqlColumnOrTable( name ) +
                 " " +
                 detectSqlType( type );

         statement.addBatch( alterTable );

         if ( ValueComposite.class.isAssignableFrom( type ) )
         {

            final String alterTableConstraint = "ALTER TABLE " +
                    escapeSqlColumnOrTable( tableName() ) +
                    LINE_SEPARATOR +
                    "ADD FOREIGN KEY (" +
                    escapeSqlColumnOrTable( name ) +
                    ") REFERENCES " +
                    escapeSqlColumnOrTable( toSnackCaseFromCamelCase( type.getSimpleName() ) ) +
                    "(" + escapeSqlColumnOrTable( "id" ) + ")";
            statement.addBatch( alterTableConstraint );
         }
      }

      return columnAdded;
   }


   protected Class<?> detectType( ValueComposite valueComposite )
   {

      final Class<? extends Composite> type = valueComposite.type();

      //exclusions
      if ( FieldValue.class.isAssignableFrom( type ) )
      {
         return FieldValue.class;
      }

      return type;
   }

   void setSimpleType( final PreparedStatement statement,
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

   void setSimpleType( final PreparedStatement statement,
                                 Object value,
                                 final int i ) throws JSONException, SQLException
   {
      if ( Boolean.class.equals( value.getClass() ) )
      {
         statement.setBoolean( i, ( Boolean ) value );
      } else if ( Integer.class.equals( value.getClass() ) )
      {
         statement.setInt( i, ( Integer ) value );
      } else if ( Long.class.equals( value.getClass() ) )
      {
         statement.setLong( i, ( Long ) value );
      } else if ( Float.class.equals( value.getClass() ) )
      {
         statement.setFloat( i, ( Float ) value );
      } else if ( Double.class.equals( value.getClass() ) )
      {
         statement.setDouble( i, ( Double ) value );
      } else if ( String.class.equals( value.getClass() )
              || value.getClass().isEnum()
              || Date.class.equals( value.getClass() )
              || DateTime.class.equals( value.getClass() ) )
      {
         statement.setString( i, ( String ) value );
      } else if ( EntityReference.class.equals( value.getClass() ) )
      {
         statement.setString( i, ( ( EntityReference ) value ).identity() );
      } else
      {
         throw new IllegalArgumentException();
      }
   }

   SingletonMap processValueComposite( ValueComposite value ) throws Exception
   {
      final ValueExportHelper valueExportHelper = new ValueExportHelper();
      valueExportHelper.setValue( value );
      valueExportHelper.setConnection( connection );
      valueExportHelper.setModule( module );
      valueExportHelper.setDbVendor( dbVendor );
      valueExportHelper.setTables( tables );
      return valueExportHelper.help();
   }

   ResultSet selectFromWhereId( String tableName, String id ) throws SQLException, JSONException
   {
      String isExistQuery = "SELECT " + escapeSqlColumnOrTable( "identity" ) +
              " FROM " + escapeSqlColumnOrTable( tableName ) +
              " WHERE " + escapeSqlColumnOrTable( "identity" ) + " = ?";

      final PreparedStatement isExistPS = connection
              .prepareStatement( isExistQuery );
      isExistPS.setString( 1, id );
      return isExistPS.executeQuery();
   }

   String escapeSqlColumnOrTable( String name )
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

   String classSimpleName( String className )
   {
      return ClassUtils.getShortClassName( className );
   }

   String toSnackCaseFromCamelCase( String str )
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

   String detectSqlType( Class type )
   {

      if ( Boolean.class.equals( type ) )
      {
         return dbVendor == DbVendor.mssql ? "BIT" : "BIT(1)";
      } else if ( Integer.class.equals( type )
              || ValueComposite.class.isAssignableFrom( type ) )
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
      } else if ( EntityReference.class.equals( type ) )
      {
         return stringSqlType( 255 );
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

   public void setTables( Map<String, Set<String>> tables )
   {
      this.tables = tables;
   }
}
