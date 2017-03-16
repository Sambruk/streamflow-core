package se.streamsource.streamflow.web.application.entityexport;

import org.joda.time.DateTime;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

import java.util.Date;
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

   public void create() throws Exception
   {
      //main tables
      for ( EntityInfo entityInfo : EntityInfo.values() )
      {

         if ( entityInfo.equals( EntityInfo.UNKNOWN ) )
         {
            continue;
         }

         this.entityInfo = entityInfo;
         final EntityDescriptor entityDescriptor = module.entityDescriptor( entityInfo.getClassName() );
         entityType = entityDescriptor.entityType();

         createMainTable();

      }

      //foreign keys
      for ( EntityInfo entityInfo : EntityInfo.values() )
      {

         if ( entityInfo.equals( EntityInfo.UNKNOWN ) )
         {
            continue;
         }

         this.entityInfo = entityInfo;
         final EntityDescriptor entityDescriptor = module.entityDescriptor( entityInfo.getClassName() );
         entityType = entityDescriptor.entityType();

         createForeignKeys();

         createManyAssociationsTables();

         createSubPropsTables();

      }

   }

   private void createSubPropsTables()
   {
      for ( PropertyType property : entityType.properties() )
      {


         if ( property.type().type().name().equals( List.class.getName() )
                 || property.type().type().name().equals( Set.class.getName() ) )
         {
            final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( property.qualifiedName().name() );
            pw.print( "CREATE TABLE " );
            pw.print( "`" );
            pw.print( tableName );
            pw.print( "`" );
            pw.println( " (" );
            pw.println( " `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," );
            pw.println( " `property_value` TEXT NULL," );
            pw.println( " PRIMARY KEY (`id`)" );
            pw.println( ") ENGINE='InnoDB'  DEFAULT CHARSET='utf8' COLLATE='utf8_swedish_ci';" );
            pw.println();
            pw.println();
         } else if ( property.type().type().name().equals( Map.class.getName() ) )
         {
            final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( property.qualifiedName().name() );
            pw.print( "CREATE TABLE " );
            pw.print( "`" );
            pw.print( tableName );
            pw.print( "`" );
            pw.println( " (" );
            pw.println( " `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," );
            pw.println( " `property_key` TEXT NULL," );
            pw.println( " `property_value` TEXT NULL," );
            pw.println( " PRIMARY KEY (`id`)" );
            pw.println( ") ENGINE='InnoDB'  DEFAULT CHARSET='utf8' COLLATE='utf8_swedish_ci';" );
            pw.println();
            pw.println();
         }
      }

   }

   private void createManyAssociationsTables() throws ClassNotFoundException
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

         pw.print( "CREATE TABLE " );
         pw.print( "`" );
         pw.print( tableName );
         pw.print( "`" );
         pw.print( " (" );
         pw.println();
         pw.println( " `owner_id` VARCHAR(255) NOT NULL," );
         pw.println( " `link_id` VARCHAR(255) NOT NULL," );

         if ( i == 1 )
         {

            pw.print( " CONSTRAINT " );
            pw.print( "`" );
            pw.print( "FK_owner_" );
            final int hashCodeOwner = ( tableName() + tableName + "owner" ).hashCode();
            pw.print( hashCodeOwner > 0 ? hashCodeOwner : -1 * hashCodeOwner );
            pw.print( "`" );
            pw.print( " FOREIGN KEY (`owner_id`) REFERENCES " );
            pw.print( "`" );
            pw.print( tableName() );
            pw.print( "`" );
            pw.println( " (`identity`)," );

            pw.print( " CONSTRAINT " );
            pw.print( "`" );
            pw.print( "FK_link_" );
            final int hashCodeLink = ( tableName() + tableName + "link" ).hashCode();
            pw.print( hashCodeLink > 0 ? hashCodeLink : -1 * hashCodeLink );
            pw.print( "`" );
            pw.print( " FOREIGN KEY (`link_id`) REFERENCES " );
            pw.print( "`" );
            pw.print( associationTable );
            pw.print( "`" );
            pw.println( " (`identity`)," );

         }

         pw.println( " PRIMARY KEY (`owner_id`, `link_id`)" );

         pw.println( ") ENGINE='InnoDB'  DEFAULT CHARSET='utf8' COLLATE='utf8_swedish_ci';" );

         pw.println();
         pw.println();
      }
   }

   private void createForeignKeys() throws ClassNotFoundException
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
            pw.print( "ALTER TABLE " );
            pw.print( "`" );
            pw.print( tableName() );
            pw.print( "`" );
            pw.print( " ADD CONSTRAINT " );
            pw.print( "`" );
            pw.print( "FK_" );
            pw.print( associationName );
            pw.print( "_" );
            final int hashCode = ( tableName() + associationName ).hashCode();
            pw.print( hashCode > 0 ? hashCode : -1 * hashCode );
            pw.print( "`" );
            pw.print( " FOREIGN KEY (" );
            pw.print( "`" );
            pw.print( associationName );
            pw.print( "`" );
            pw.print( ") REFERENCES " );
            pw.print( "`" );
            pw.print( associationTable );
            pw.print( "`" );
            pw.print( " (`identity`);" );
            pw.println();
            pw.println();
         }

      }

   }

   private void createMainTable() throws ClassNotFoundException
   {
      final StringBuilder mainTable = new StringBuilder();
      mainTable.append( "CREATE TABLE " )
              .append( escapeSqlColumnOrTable( tableName() ) )
              .
      pw.print( " (" );
      pw.println();

      for ( PropertyType property : entityType.properties() )
      {

         if ( property.qualifiedName().name().equals( "identity" ) )
         {
            pw.println( " `identity` VARCHAR(255) NOT NULL," );
         } else
         {
            pw.print( " " );
            pw.print( "`" );
            pw.print( toSnackCaseFromCamelCase( property.qualifiedName().name() ) );
            pw.print( "`" );
            pw.print( " " );
            pw.print( detectType( property ) );
            pw.print( " NULL," );
            pw.println();
         }

      }


      for ( AssociationType association : entityType.associations() )
      {
         final String associationName = toSnackCaseFromCamelCase( association.qualifiedName().name() );

         pw.print( " " );
         pw.print( "`" );
         pw.print( associationName );
         pw.print( "`" );
         pw.print( " VARCHAR(255) NULL," );
         pw.println();

      }

      pw.println( " PRIMARY KEY (`identity`) " );

      pw.println( ") ENGINE='InnoDB'  DEFAULT CHARSET='utf8' COLLATE='utf8_swedish_ci';" );

      pw.println();
      pw.println();


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
