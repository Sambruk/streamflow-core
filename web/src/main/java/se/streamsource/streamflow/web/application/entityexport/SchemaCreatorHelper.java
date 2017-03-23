package se.streamsource.streamflow.web.application.entityexport;

import org.joda.time.DateTime;
import org.qi4j.runtime.types.CollectionType;
import org.qi4j.runtime.types.MapType;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ruslan on 09.03.17.
 */
public class SchemaCreatorHelper extends AbstractExportHelper
{

   private final static Logger logger = LoggerFactory.getLogger( SchemaCreatorHelper.class.getName() );

   private EntityType entityType;
   private EntityInfo entityInfo;

   private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

   private Map<String, Set<String>> tableColumns;

   public Map<String, Set<String>> create() throws Exception
   {

      tableColumns = new HashMap<>();

      //main tables
      for ( EntityInfo entityInfo : EntityInfo.values() )
      {
         if ( !entityInfo.equals( EntityInfo.UNKNOWN ) )
         {
            this.entityInfo = entityInfo;
            final EntityDescriptor entityDescriptor = module.entityDescriptor( entityInfo.getClassName() );
            entityType = entityDescriptor.entityType();

            createMainTable();
         }
      }

      //foreign keys
      for ( EntityInfo entityInfo : EntityInfo.values() )
      {
         if ( !entityInfo.equals( EntityInfo.UNKNOWN ) )
         {
            this.entityInfo = entityInfo;
            final EntityDescriptor entityDescriptor = module.entityDescriptor( entityInfo.getClassName() );
            entityType = entityDescriptor.entityType();

            createForeignKeys();

            createManyAssociationsTables();

         }
      }

      return tableColumns;

   }

   private void createManyAssociationsTables() throws ClassNotFoundException, SQLException
   {
      for ( ManyAssociationType manyAssociation : entityType.manyAssociations() )
      {
         final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( manyAssociation.qualifiedName().name() ) + "_cross_ref";

         String associationTable = null;

         final Class<?> clazz = Class.forName( manyAssociation.type() );

         int i = 0;
         for ( EntityInfo info : EntityInfo.values() )
         {
            if ( clazz.isAssignableFrom( info.getEntityClass() ) )
            {
               associationTable = toSnackCaseFromCamelCase( info.getClassSimpleName() );
               i++;
            }
         }

         final StringBuilder manyAssoc = new StringBuilder();


         manyAssoc
                 .append( "CREATE TABLE " )
                 .append( escapeSqlColumnOrTable( tableName ) )
                 .append( " (" )
                 .append( LINE_SEPARATOR )
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( " " )
                 .append( stringSqlType( 255 ) )
                 .append( " NOT NULL," )
                 .append( LINE_SEPARATOR )
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "link_id" ) )
                 .append( " " )
                 .append( stringSqlType( 255 ) )
                 .append( " NOT NULL," )
                 .append( LINE_SEPARATOR );

         final HashSet<String> columns = new HashSet<>();
         columns.add( "owner_id" );
         columns.add( "link_id" );

         tableColumns.put( tableName, columns );

         if ( i == 1 )
         {
            final int hashCodeOwner = ( tableName() + tableName + "owner" ).hashCode();
            manyAssoc
                    .append( " CONSTRAINT FK_owner_" )
                    .append( hashCodeOwner >= 0 ? hashCodeOwner : ( -1 * hashCodeOwner ) )
                    .append( " FOREIGN KEY (" )
                    .append( escapeSqlColumnOrTable( "owner_id" ) )
                    .append( ") REFERENCES " )
                    .append( escapeSqlColumnOrTable( tableName() ) )
                    .append( " (" )
                    .append( escapeSqlColumnOrTable( "identity" ) )
                    .append( ")," )
                    .append( LINE_SEPARATOR );

            final int hashCodeLink = ( tableName() + tableName + "link" ).hashCode();
            manyAssoc
                    .append( " CONSTRAINT FK_link_" )
                    .append( hashCodeLink >= 0 ? hashCodeLink : ( -1 * hashCodeLink ) )
                    .append( " FOREIGN KEY (" )
                    .append( escapeSqlColumnOrTable( "link_id" ) )
                    .append( ") REFERENCES " )
                    .append( escapeSqlColumnOrTable( associationTable ) )
                    .append( " (" )
                    .append( escapeSqlColumnOrTable( "identity" ) )
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

         final Statement statement = connection.createStatement();

         statement.executeUpdate( manyAssoc.toString() );
         statement.close();

         logger.info( manyAssoc.toString() );

      }
   }

   private void createForeignKeys() throws ClassNotFoundException, SQLException
   {
      for ( AssociationType association : entityType.associations() )
      {
         final String associationName = toSnackCaseFromCamelCase( association.qualifiedName().name() );
         String associationTable = null;

         final Class<?> clazz = Class.forName( association.type().name() );

         int i = 0;
         for ( EntityInfo info : EntityInfo.values() )
         {
            if ( clazz.isAssignableFrom( info.getEntityClass() ) )
            {
               associationTable = toSnackCaseFromCamelCase( info.getClassSimpleName() );
               i++;
            }
         }


         if ( i == 1 )
         {
            final int hashCode = ( tableName() + associationName ).hashCode();

            final StringBuilder foreignKey = new StringBuilder();
            foreignKey.append( "ALTER TABLE " )
                    .append( escapeSqlColumnOrTable( tableName() ) )
                    .append( " ADD CONSTRAINT " )
                    .append( "FK_" )
                    .append( associationName )
                    .append( "_" )
                    .append( hashCode > 0 ? hashCode : -1 * hashCode )
                    .append( " FOREIGN KEY (" )
                    .append( associationName )
                    .append( ") REFERENCES " )
                    .append( escapeSqlColumnOrTable( associationTable ) )
                    .append( " (" )
                    .append( escapeSqlColumnOrTable( "identity" ) )
                    .append( ");" );

            final Statement statement = connection.createStatement();

            statement.executeUpdate( foreignKey.toString() );
            statement.close();

            logger.info( foreignKey.toString() );

         }

      }

   }

   private void createMainTable() throws ClassNotFoundException, SQLException
   {
      final StringBuilder mainTableCreate = new StringBuilder();
      mainTableCreate.append( "CREATE TABLE " )
              .append( escapeSqlColumnOrTable( tableName() ) )
              .append( " (" )
              .append( LINE_SEPARATOR );

      tableColumns.put( tableName(), new HashSet<String>() );

      for ( PropertyType property : entityType.properties() )
      {

         if ( property.qualifiedName().name().equals( "identity" ) )
         {
            mainTableCreate
                    .append( " " )
                    .append( escapeSqlColumnOrTable( "identity" ) )
                    .append( " " )
                    .append( stringSqlType( 255 ) )
                    .append( " NOT NULL," )
                    .append( LINE_SEPARATOR );

            final Set<String> columns = tableColumns.get( tableName() );
            columns.add( "identity" );
         } else
         {

            if ( !( property.type().isValue()
                    || property.type() instanceof CollectionType
                    || property.type() instanceof MapType ) )
            {
               mainTableCreate
                       .append( " " )
                       .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( property.qualifiedName().name() ) ) )
                       .append( " " )
                       .append( detectSqlType( property ) )
                       .append( " NULL," )
                       .append( LINE_SEPARATOR );

               final Set<String> columns = tableColumns.get( tableName() );
               columns.add( toSnackCaseFromCamelCase( property.qualifiedName().name() ) );
            }

         }

      }


      for ( AssociationType association : entityType.associations() )
      {
         final String associationName = toSnackCaseFromCamelCase( association.qualifiedName().name() );

         mainTableCreate
                 .append( " " )
                 .append( escapeSqlColumnOrTable( associationName ) )
                 .append( " " )
                 .append( stringSqlType( 255 ) )
                 .append( " NULL," )
                 .append( LINE_SEPARATOR );

      }

      mainTableCreate
              .append( " PRIMARY KEY (" )
              .append( escapeSqlColumnOrTable( "identity" ) )
              .append( ") " )
              .append( LINE_SEPARATOR )
              .append( ");" )
              .append( LINE_SEPARATOR );

      final Statement statement = connection.createStatement();

      statement.executeUpdate( mainTableCreate.toString() );
      statement.close();

      logger.info( mainTableCreate.toString() );

   }

   private String stringSqlType( int length )
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

   private String detectSqlType( Class type ) throws ClassNotFoundException
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

   private String detectSqlType( PropertyType property ) throws ClassNotFoundException
   {

      final Class type = Class.forName( property.type().type().name() );

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
      } else if ( type.isEnum()
              || Date.class.equals( type )
              || DateTime.class.equals( type ) )
      {
         return stringSqlType( 255 );
      } else if ( String.class.equals( type ) )
      {
         return "TEXT";
      } else
      {
         throw new IllegalArgumentException();
      }
   }

   @Override
   protected String tableName()
   {
      return toSnackCaseFromCamelCase( entityInfo.getClassSimpleName() );
   }

}
