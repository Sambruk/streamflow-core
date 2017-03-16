package se.streamsource.streamflow.web.application.entityexport;

import org.joda.time.DateTime;
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
import java.util.List;
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

   public void create() throws Exception
   {

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

            createSubPropsTables();
         }
      }

   }

   private void createSubPropsTables() throws SQLException
   {
      for ( PropertyType property : entityType.properties() )
      {
         final boolean isList = property.type().type().name().equals( List.class.getName() );
         final boolean isSet = property.type().type().name().equals( Set.class.getName() );
         final boolean isMap = property.type().type().name().equals( Map.class.getName() );

         if ( isList || isSet || isMap )
         {

            final StringBuilder collectionTable = new StringBuilder();
            final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( property.qualifiedName().name() );

            collectionTable
                    .append( "CREATE TABLE " )
                    .append( escapeSqlColumnOrTable( tableName ) )
                    .append( " (" )
                    .append( LINE_SEPARATOR )
                    .append( escapeSqlColumnOrTable( "id" ) )
                    .append( " INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," )
                    .append( LINE_SEPARATOR );

            if ( isMap )
            {
               collectionTable
                       .append( " " )
                       .append( escapeSqlColumnOrTable( "property_key" ) )
                       .append( stringLimitedSqlType( Integer.MAX_VALUE ) )
                       .append( " NULL," )
                       .append( LINE_SEPARATOR );
            }

            collectionTable
                    .append( " " )
                    .append( escapeSqlColumnOrTable( "property_value" ) )
                    .append( stringLimitedSqlType( Integer.MAX_VALUE ) )
                    .append( " NULL," )
                    .append( LINE_SEPARATOR )
                    .append( " PRIMARY KEY (" )
                    .append( escapeSqlColumnOrTable( "id" ) )
                    .append( ") " )
                    .append( LINE_SEPARATOR )
                    .append( tableEnd() )
                    .append( LINE_SEPARATOR );

            final Statement statement = connection.createStatement();

            statement.executeUpdate( collectionTable.toString() );
            statement.close();

            logger.info( collectionTable.toString() );


         } else
         {

         }
      }

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
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( " " )
                 .append( stringLimitedSqlType( 255 ) )
                 .append( " NOT NULL," )
                 .append( LINE_SEPARATOR )
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( " " )
                 .append( stringLimitedSqlType( 255 ) )
                 .append( " NOT NULL," )
                 .append( LINE_SEPARATOR );


         if ( i == 1 )
         {
            final int hashCodeOwner = ( tableName() + tableName + "owner" ).hashCode();
            manyAssoc
                    .append( " CONSTRAINT FK_owner_" )
                    .append( hashCodeOwner > 0 ? hashCodeOwner : -1 * hashCodeOwner )
                    .append( " FOREIGN KEY (" )
                    .append( escapeSqlColumnOrTable( "owner_id" ) )
                    .append( ") REFERENCES " )
                    .append( escapeSqlColumnOrTable( tableName() ))
                    .append( " (" )
                    .append( escapeSqlColumnOrTable( "identity" ) )
                    .append( ")," )
                    .append( LINE_SEPARATOR );

            final int hashCodeLink = ( tableName() + tableName + "link" ).hashCode();
            manyAssoc
                    .append( " CONSTRAINT FK_link_" )
                    .append( hashCodeOwner > 0 ? hashCodeLink : -1 * hashCodeLink )
                    .append( " FOREIGN KEY (" )
                    .append( escapeSqlColumnOrTable( "link_id" ) )
                    .append( ") REFERENCES " )
                    .append( escapeSqlColumnOrTable( associationTable ))
                    .append( " (" )
                    .append( escapeSqlColumnOrTable( "identity" ) )
                    .append( ")," )
                    .append( LINE_SEPARATOR );

         }

         manyAssoc
                 .append( " PRIMARY KEY (")
                 .append( escapeSqlColumnOrTable( "owner_id" ) )
                 .append( "," )
                 .append( escapeSqlColumnOrTable("link_id" ))
                 .append( ")" )
                 .append( LINE_SEPARATOR )
                 .append( tableEnd() );

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

      for ( PropertyType property : entityType.properties() )
      {

         if ( property.qualifiedName().name().equals( "identity" ) )
         {
            mainTableCreate
                    .append( " " )
                    .append( escapeSqlColumnOrTable( "identity" ) )
                    .append( " " )
                    .append( stringLimitedSqlType( 255 ) )
                    .append( " NOT NULL," );
         } else
         {
            mainTableCreate
                    .append( " " )
                    .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( property.qualifiedName().name() ) ) )
                    .append( " " )
                    .append( detectType( property ) )
                    .append( " NULL," )
                    .append( LINE_SEPARATOR );
         }

      }


      for ( AssociationType association : entityType.associations() )
      {
         final String associationName = toSnackCaseFromCamelCase( association.qualifiedName().name() );

         mainTableCreate
                 .append( " " )
                 .append( escapeSqlColumnOrTable( associationName ) )
                 .append( " " )
                 .append( stringLimitedSqlType( 255 ) )
                 .append( " NULL," )
                 .append( LINE_SEPARATOR );

      }

      mainTableCreate
              .append( " PRIMARY KEY (" )
              .append( escapeSqlColumnOrTable( "identity" ) )
              .append( ") " )
              .append( tableEnd() )
              .append( LINE_SEPARATOR );

      final Statement statement = connection.createStatement();

      statement.executeUpdate( mainTableCreate.toString() );
      statement.close();

      logger.info( mainTableCreate.toString() );

   }

   private String tableEnd()
   {
      switch ( dbVendor )
      {
         case mssql:
            return ");";
         case oracle:
            return ") CHARACTER SET utf8 COLLATE utf8_unicode_ci;";
         default:
            return ") ENGINE='InnoDB'  DEFAULT CHARSET='utf8' COLLATE='utf8_unicode_ci';";
      }
   }

   private String stringLimitedSqlType( int length )
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

   private boolean isCollectionOrValue( PropertyType property )
   {
      return property.type().isValue()
              || property.type().type().name().equals( List.class.getName() )
              || property.type().type().name().equals( Set.class.getName() )
              || property.type().type().name().equals( Map.class.getName() );
   }

   private String detectType( PropertyType property ) throws ClassNotFoundException
   {
      final Class type = Class.forName( property.type().type().name() );

      if ( Boolean.class.equals( type ) )
      {
         return "BIT(1)";
      } else if ( Integer.class.equals( type ) )
      {
         return "INT(11)";
      } else if ( Long.class.equals( type ) )
      {
         return "BIGINT(20)";
      } else if ( Float.class.equals( type ) )
      {
         return "FLOAT";
      } else if ( Double.class.equals( type ) )
      {
         return "DOUBLE";
      } else if ( String.class.equals( type )
              || type.isEnum()
              || Date.class.equals( type )
              || DateTime.class.equals( type )
              || isCollectionOrValue( property ) )
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
