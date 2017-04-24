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
package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ClassUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.api.administration.form.FieldValue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract helper class which purpose to contain common util methods and fields
 * for entity export strategy.
 */
public abstract class AbstractExportHelper
{

   public static final String LINE_SEPARATOR = System.getProperty( "line.separator" );
   private static final Logger logger = LoggerFactory.getLogger( AbstractExportHelper.class );

   protected Connection connection;
   protected ModuleSPI module;
   protected DbVendor dbVendor;
   protected Map<String, Set<String>> tables;

   private String schemaInfoFileAbsPath;

   protected boolean showSql;

   protected abstract String tableName();

   protected void createSubPropertyTableIfNotExists( String tableName ) throws SQLException, IOException
   {
      if ( !tables.containsKey( tableName ) )
      {

         String subPropertyTable = "CREATE TABLE " +
                 escapeSqlColumnOrTable( tableName ) +
                 " (" +
                 LINE_SEPARATOR +
                 " " +
                 escapeSqlColumnOrTable( "id" ) +
                 " " +
                 detectSqlType( Integer.class ) +
                 " NOT NULL " +
                 ( dbVendor == DbVendor.mysql ? "AUTO_INCREMENT," : "identity(1,1)," ) +
                 LINE_SEPARATOR +
                 " PRIMARY KEY (" +
                 escapeSqlColumnOrTable( "id" ) +
                 ")" +
                 LINE_SEPARATOR +
                 ");";

         final HashSet<String> columns = new HashSet<>();

         columns.add( "id" );

         final Statement statement = connection.createStatement();

         statement.executeUpdate( subPropertyTable );

         tables.put( tableName, columns );
      }
   }

   void processCollection( String name,
                           Object value,
                           PreparedStatementValueBinder valueBinder
   ) throws SQLException, JSONException, ClassNotFoundException, IOException
   {
      final String tableName = tableName() + "_" + toSnakeCaseFromCamelCase( name ) + "_coll";

      final boolean isMap = value instanceof Map;

      createCollectionTableIfNotExist( tableName, isMap, valueBinder );

      final Collection<?> objects = isMap ? ( ( Map<?, ?> ) value ).keySet() : ( Collection<?> ) value;

      String query = "INSERT INTO "
              + escapeSqlColumnOrTable( tableName )
              + " ("
              + escapeSqlColumnOrTable( "owner_id" )
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

   void createCollectionTableIfNotExist( String tableName, boolean isMap, PreparedStatementValueBinder valueBinder ) throws SQLException, ClassNotFoundException, IOException
   {

      if ( !tables.containsKey( tableName ) )
      {
         final StringBuilder collectionTable = new StringBuilder();

         final HashSet<String> columns = new HashSet<>();

         collectionTable
                 .append( "CREATE TABLE " )
                 .append( escapeSqlColumnOrTable( tableName ) )
                 .append( " (" )
                 .append( LINE_SEPARATOR )
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( " " )
                 .append( valueBinder.getSqlType() )
                 .append( " NOT NULL," )
                 .append( LINE_SEPARATOR );
         columns.add( "owner_id" );

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
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( ") REFERENCES " )
                 .append( escapeSqlColumnOrTable( tableName() ) )
                 .append( " (" )
                 .append( valueBinder.getSqlType().equals( stringSqlType( 255 ) )
                         ? escapeSqlColumnOrTable( "identity" ) : escapeSqlColumnOrTable( "id" ) )
                 .append( valueBinder.getSqlType().equals( detectSqlType( Integer.class ) ) ? ") ON DELETE CASCADE," : ")," )
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

         if ( showSql )
         {
            logger.info( collectionTable.toString() );
         }

         tables.put( tableName, columns );

      }

   }

   void createCrossRefTableIfNotExists( String tableName,
                                        String associationTable,
                                        String ownerType,
                                        String linkType ) throws ClassNotFoundException, SQLException
   {

      if ( tables.get( tableName ) == null )
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

         tables.put( tableName, columns );

         final int hashCodeOwner = ( tableName() + tableName + "owner_id" ).hashCode();
         manyAssoc
                 .append( " CONSTRAINT FK_owner_" )
                 .append( hashCodeOwner >= 0 ? hashCodeOwner : ( -1 * hashCodeOwner ) )
                 .append( " FOREIGN KEY (" )
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( ") REFERENCES " )
                 .append( escapeSqlColumnOrTable( tableName() ) )
                 .append( " (" )
                 .append( stringSqlType( 255 ).equals( ownerType ) ? escapeSqlColumnOrTable( "identity" ) : escapeSqlColumnOrTable( "id" ) )
                 .append( ownerType.equals( detectSqlType( Integer.class ) ) ? ") ON DELETE CASCADE," : ")," )
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

         if ( showSql )
         {
            logger.info( manyAssoc.toString() );
         }

         if ( associationTable != null && linkType.equals( detectSqlType( Integer.class ) ) )
         {

            if ( dbVendor == DbVendor.mssql )
            {
               final String trigger = "CREATE TRIGGER trg_" + tableName + LINE_SEPARATOR +
                       "  ON " + escapeSqlColumnOrTable( tableName ) + LINE_SEPARATOR +
                       "  AFTER DELETE AS" + LINE_SEPARATOR +
                       "  BEGIN" + LINE_SEPARATOR +
                       "    IF @@ROWCOUNT = 0" + LINE_SEPARATOR +
                       "      RETURN" + LINE_SEPARATOR +
                       "    DELETE FROM " + escapeSqlColumnOrTable( associationTable ) + " WHERE id IN (SELECT link_id FROM deleted)" + LINE_SEPARATOR +
                       "  END";

               try ( final Statement statement = connection.createStatement() )
               {
                  statement.executeUpdate( trigger );
               }

               if ( showSql )
               {
                  logger.info( trigger );
               }
            } else
            {

               // TODO: 30.03.17

            }

         }

      }

   }

   protected void createTrigger( Set<String> triggerStatements ) throws SQLException, IOException
   {
      if ( !triggerStatements.isEmpty() )
      {

         if ( dbVendor == DbVendor.mssql )
         {
            final String triggerName = "trg_" + tableName();

            Set<String> triggerStatementsPersisted = tables.get( triggerName );
            if ( triggerStatementsPersisted == null )
            {
               triggerStatementsPersisted = new LinkedHashSet<>();
            }

            StringBuilder trigger = new StringBuilder();

            trigger.append( "CREATE OR ALTER" )
                    .append( " TRIGGER " )
                    .append( triggerName )
                    .append( LINE_SEPARATOR )
                    .append( " ON " )
                    .append( escapeSqlColumnOrTable( tableName() ) )
                    .append( LINE_SEPARATOR )
                    .append( " AFTER UPDATE AS " )
                    .append( LINE_SEPARATOR )
                    .append( "BEGIN" )
                    .append( LINE_SEPARATOR );

            triggerStatementsPersisted.addAll( triggerStatements );

            for ( String statement : triggerStatementsPersisted )
            {
               trigger.append( statement )
                       .append( LINE_SEPARATOR );
            }

            trigger.append( "END" );

            try ( final Statement statement = connection.createStatement() )
            {
               statement.executeUpdate( trigger.toString() );
            }

            tables.put( triggerName, triggerStatementsPersisted );

         }

      }
   }

   protected String addColumn( String name, Class<?> type, Statement statement ) throws SQLException, IOException
   {
      final Set<String> columns = tables.get( tableName() );

      assert columns != null;

      if ( columns.add( name ) )
      {
         final String alterTable = "ALTER TABLE " +
                 escapeSqlColumnOrTable( tableName() ) +
                 LINE_SEPARATOR +
                 "ADD " +
                 escapeSqlColumnOrTable( name ) +
                 " " +
                 detectSqlType( type );

         if ( showSql )
         {
            logger.info( alterTable );
         }

         statement.addBatch( alterTable );

         if ( ValueComposite.class.isAssignableFrom( type ) )
         {

            final String reference = toSnakeCaseFromCamelCase( type.getSimpleName() );

            createSubPropertyTableIfNotExists( reference );

            final String alterTableConstraint = "ALTER TABLE " +
                    escapeSqlColumnOrTable( tableName() ) +
                    LINE_SEPARATOR +
                    "ADD FOREIGN KEY (" +
                    escapeSqlColumnOrTable( name ) +
                    ") REFERENCES " +
                    escapeSqlColumnOrTable( reference ) +
                    "(" + escapeSqlColumnOrTable( "id" ) + ")";

            if ( showSql )
            {
               logger.info( alterTableConstraint );
            }

            statement.addBatch( alterTableConstraint );

            if ( dbVendor == DbVendor.mssql )
            {
               return "IF UPDATE(" + escapeSqlColumnOrTable( name ) + ")" + LINE_SEPARATOR +
                       "    BEGIN" + LINE_SEPARATOR +
                       "      DELETE FROM " + escapeSqlColumnOrTable( reference ) + LINE_SEPARATOR +
                       "      WHERE id IN (SELECT d." + escapeSqlColumnOrTable( name ) + LINE_SEPARATOR +
                       "                   FROM deleted d" + LINE_SEPARATOR +
                       "                     JOIN inserted i ON " + ( columns.contains( "identity" ) ? "d.[identity] = i.[identity]" : "d.[id] = i.[id]" ) + LINE_SEPARATOR +
                       "                   WHERE d." + escapeSqlColumnOrTable( name ) + " IS NOT NULL AND i." + escapeSqlColumnOrTable( name ) + " IS NULL)" + LINE_SEPARATOR +
                       "    END";
            }

         }
      }

      return "";
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
      final Class<?> clazz = value.getClass();
      if ( Boolean.class.equals( clazz ) )
      {
         statement.setBoolean( i, ( Boolean ) value );
      } else if ( Integer.class.equals( clazz ) )
      {
         statement.setInt( i, ( Integer ) value );
      } else if ( Long.class.equals( clazz ) )
      {
         statement.setLong( i, ( Long ) value );
      } else if ( Float.class.equals( clazz ) )
      {
         statement.setFloat( i, ( Float ) value );
      } else if ( Double.class.equals( clazz ) )
      {
         statement.setDouble( i, ( Double ) value );
      } else if ( String.class.equals( clazz ) || clazz.isEnum() )
      {
         statement.setString( i, value.toString() );
      } else if ( Date.class.equals( clazz ) )
      {
         final Date date = ( Date ) value;
         statement.setTimestamp( i, new Timestamp( date.getTime() ) );
      } else if ( DateTime.class.equals( clazz ) )
      {
         final DateTime dateTime = ( DateTime ) value;
         statement.setTimestamp( i, new Timestamp( dateTime.getMillis() ) );
      } else if ( EntityReference.class.equals( clazz ) )
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
      valueExportHelper.setSchemaInfoFileAbsPath( schemaInfoFileAbsPath );
      valueExportHelper.setShowSql( showSql );
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

   String toSnakeCaseFromCamelCase( String str )
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
      } else if ( Date.class.equals( type ) || DateTime.class.equals( type ) )
      {
         return "DATETIME";
      } else if ( type.isEnum() || EntityReference.class.equals( type ) )
      {
         return stringSqlType( 255 );
      } else if ( String.class.equals( type ) )
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

   protected void saveTablesState() throws IOException, JSONException
   {
      JSONObject jsonObject = new JSONObject();
      for ( String tableName : tables.keySet() )
      {
         jsonObject.put( tableName, tables.get( tableName ) );
      }
      FileUtils.writeStringToFile( new File( schemaInfoFileAbsPath ), jsonObject.toString(), StandardCharsets.UTF_8.name() );
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

   public void setSchemaInfoFileAbsPath( String infoFilePath )
   {
      this.schemaInfoFileAbsPath = infoFilePath;
   }

   public void setShowSql( boolean showSql )
   {
      this.showSql = showSql;
   }
}
