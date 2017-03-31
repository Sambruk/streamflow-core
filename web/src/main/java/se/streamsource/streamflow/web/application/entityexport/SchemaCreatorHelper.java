package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.io.IOUtils;
import org.qi4j.runtime.types.CollectionType;
import org.qi4j.runtime.types.MapType;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.property.PropertyType;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ruslan on 09.03.17.
 */
public class SchemaCreatorHelper extends AbstractExportHelper
{

   private EntityType entityType;
   private EntityInfo entityInfo;
   private List<String> info;
   private File infoFile;

   public Map<String, Set<String>> create() throws Exception
   {

      tables = new HashMap<>();

      info = IOUtils.readLines( new FileReader( infoFile ) );
      final List<String> schema = new LinkedList<>();

      //main tables
      for ( EntityInfo entityInfo : EntityInfo.values() )
      {
         if ( !entityInfo.equals( EntityInfo.UNKNOWN ) )
         {
            this.entityInfo = entityInfo;
            final EntityDescriptor entityDescriptor = module.entityDescriptor( entityInfo.getClassName() );
            entityType = entityDescriptor.entityType();

            final String mainTable = createMainTable();

            if ( !mainTable.isEmpty() )
            {
               schema.add( mainTable );
            }

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

            createForeignKeys( schema );

         }
      }


      try ( final Statement statement = connection.createStatement() )
      {
         for ( String command : schema )
         {
            statement.addBatch( command );
         }
         statement.executeBatch();
      }

      try ( final PrintWriter pw = new PrintWriter( infoFile ) )
      {
         for ( String schemaComponent : info )
         {
            pw.println( schemaComponent );
         }
         pw.flush();
      }

      return tables;

   }

   private void createForeignKeys( List<String> schema ) throws ClassNotFoundException, SQLException
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

            final String foreignKeyName = "FK_" + associationName + "_" + ( hashCode > 0 ? hashCode : -1 * hashCode );

            if ( !info.contains( foreignKeyName ) )
            {
               final StringBuilder foreignKey = new StringBuilder();
               foreignKey.append( "ALTER TABLE " )
                       .append( escapeSqlColumnOrTable( tableName() ) )
                       .append( " ADD CONSTRAINT " )
                       .append( foreignKeyName )
                       .append( " FOREIGN KEY (" )
                       .append( associationName )
                       .append( ") REFERENCES " )
                       .append( escapeSqlColumnOrTable( associationTable ) )
                       .append( " (" )
                       .append( escapeSqlColumnOrTable( "identity" ) )
                       .append( ");" );

               logger.info( foreignKey.toString() );
               schema.add( foreignKey.toString() );
               info.add( foreignKeyName );
            }
         }

      }

   }

   private String createMainTable() throws ClassNotFoundException, SQLException
   {
      final StringBuilder mainTableCreate = new StringBuilder();
      mainTableCreate.append( "CREATE TABLE " )
              .append( escapeSqlColumnOrTable( tableName() ) )
              .append( " (" )
              .append( LINE_SEPARATOR );

      tables.put( tableName(), new HashSet<String>() );

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

            final Set<String> columns = tables.get( tableName() );
            columns.add( "identity" );
         } else
         {

            if ( !( property.type().isValue()
                    || property.type() instanceof CollectionType
                    || property.type() instanceof MapType ) )
            {
               final Class<?> clazz = Class.forName( property.type().type().name() );
               mainTableCreate
                       .append( " " )
                       .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( property.qualifiedName().name() ) ) )
                       .append( " " )
                       .append( detectSqlType( clazz ) )
                       .append( " NULL," )
                       .append( LINE_SEPARATOR );

               final Set<String> columns = tables.get( tableName() );
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

      if ( info.contains( tableName() ) )
      {
         return "";
      }

      logger.info( mainTableCreate.toString() );
      info.add( tableName() );
      return mainTableCreate.toString();

   }

   @Override
   protected String tableName()
   {
      return toSnackCaseFromCamelCase( entityInfo.getClassSimpleName() );
   }

   public void setInfoFile( File infoFile )
   {
      this.infoFile = infoFile;
   }
}
