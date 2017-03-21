package se.streamsource.streamflow.web.application.entityexport;

import org.joda.time.DateTime;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.runtime.types.CollectionType;
import org.qi4j.runtime.types.MapType;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.value.ValueDescriptor;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.api.administration.form.FieldValue;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

   private Set<PropertyInfo> manyImplFields;
   private Set<String> subPropertyClasses;
   private int embeddedCounter = 1;

   private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

   public void create() throws Exception
   {

      try
      {

         manyImplFields = new HashSet<>();
         subPropertyClasses = new HashSet<>();

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

         createCollectionTables( true );
         createCollectionTables( false );

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

         manyImplFields = Collections.unmodifiableSet( manyImplFields );

      } finally
      {
         if ( connection != null && !connection.isClosed() )
         {
            connection.close();
         }
      }

   }

   private void createCollectionTables( boolean isMap ) throws SQLException
   {

      final StringBuilder collectionTable = new StringBuilder();

      collectionTable
              .append( "CREATE TABLE " )
              .append( escapeSqlColumnOrTable( isMap ? "property_map" : "property_collection" ) )
              .append( " (" )
              .append( LINE_SEPARATOR )
              .append( " " )
              .append( escapeSqlColumnOrTable( "id" ) )
              .append( " INT(11) NOT NULL AUTO_INCREMENT," )
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
              .append( escapeSqlColumnOrTable( "property_value" ) )
              .append( " " )
              .append( stringSqlType( Integer.MAX_VALUE ) )
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
   }

   public Set<PropertyInfo> getManyImplFields()
   {
      return manyImplFields;
   }

   private void createSubPropsTables() throws SQLException, ClassNotFoundException, NoSuchFieldException
   {
      for ( PropertyType property : entityType.properties() )
      {
         final boolean isCollection = property.type() instanceof CollectionType;
         final boolean isMap = property.type() instanceof MapType;

         if ( isCollection || isMap )
         {

            boolean isCollectionOfValues = isCollection
                    && ( ( CollectionType ) property.type() ).collectedType().isValue();

            if ( isCollectionOfValues )
            {

               createSubPropertyTable( ( ( CollectionType ) property.type() ).collectedType().type().name() );

               final StringBuilder collectionTable = new StringBuilder();
               final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( property.qualifiedName().name() ) + "_cross_ref";

               collectionTable
                       .append( "CREATE TABLE " )
                       .append( escapeSqlColumnOrTable( tableName ) )
                       .append( " (" )
                       .append( LINE_SEPARATOR )

                       .append( " " )
                       .append( escapeSqlColumnOrTable( "identity" ) )
                       .append( stringSqlType( 255 ) )
                       .append( " NOT NULL," )
                       .append( LINE_SEPARATOR )

                       .append( " " )
                       .append( escapeSqlColumnOrTable( "embedded" ) )
                       .append( " INT(11) NOT NULL," )
                       .append( LINE_SEPARATOR )


                       .append( " CONSTRAINT FK_embedded_" )
                       .append( embeddedCounter++ )
                       .append( " FOREIGN KEY (" )
                       .append( escapeSqlColumnOrTable( "embedded" ) )
                       .append( ") REFERENCES " )
                       .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( classSimpleName( ( ( CollectionType ) property.type() ).collectedType().type().name() ) ) ) )
                       .append( " (" )
                       .append( escapeSqlColumnOrTable( "id" ) )
                       .append( ")," )
                       .append( LINE_SEPARATOR )


                       .append( " PRIMARY KEY (" )
                       .append( escapeSqlColumnOrTable( "identity" ) )
                       .append( "," )
                       .append( escapeSqlColumnOrTable( "embedded" ) )
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
               final StringBuilder collectionTable = new StringBuilder();
               final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( property.qualifiedName().name() ) + "_cross_ref";

               collectionTable
                       .append( "CREATE TABLE " )
                       .append( escapeSqlColumnOrTable( tableName ) )
                       .append( " (" )
                       .append( LINE_SEPARATOR )

                       .append( " " )
                       .append( escapeSqlColumnOrTable( "identity" ) )
                       .append( stringSqlType( 255 ) )
                       .append( " NOT NULL," )
                       .append( LINE_SEPARATOR )

                       .append( " " )
                       .append( escapeSqlColumnOrTable( "embedded" ) )
                       .append( " INT(11) NOT NULL," )
                       .append( LINE_SEPARATOR )


                       .append( " CONSTRAINT FK_embedded_" )
                       .append( embeddedCounter++ )
                       .append( " FOREIGN KEY (" )
                       .append( escapeSqlColumnOrTable( "embedded" ) )
                       .append( ") REFERENCES " )
                       .append( escapeSqlColumnOrTable( isMap ? "property_map" : "property_collection" ) )
                       .append( " (" )
                       .append( escapeSqlColumnOrTable( "id" ) )
                       .append( ")," )
                       .append( LINE_SEPARATOR )


                       .append( " PRIMARY KEY (" )
                       .append( escapeSqlColumnOrTable( "identity" ) )
                       .append( "," )
                       .append( escapeSqlColumnOrTable( "embedded" ) )
                       .append( ") " )
                       .append( LINE_SEPARATOR )
                       .append( tableEnd() )
                       .append( LINE_SEPARATOR );

               final Statement statement = connection.createStatement();

               statement.executeUpdate( collectionTable.toString() );
               statement.close();

               logger.info( collectionTable.toString() );
            }

         } else if ( property.type().isValue() )
         {
            createSubPropertyTable( property.type().type().name() );

            final StringBuilder addColumn = new StringBuilder();
            final String associationName = property.qualifiedName().name();
            addColumn.append( "ALTER TABLE " )
                    .append( escapeSqlColumnOrTable( tableName() ) )
                    .append( " ADD " )
                    .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( associationName ) ) )
                    .append( " INT(11) NULL;" );

            final Statement addColumnStatement = connection.createStatement();

            addColumnStatement.executeUpdate( addColumn.toString() );
            addColumnStatement.close();

            logger.info( addColumn.toString() );

            final int hashCode = ( tableName() + associationName ).hashCode();

            final StringBuilder foreignKey = new StringBuilder();
            foreignKey.append( "ALTER TABLE " )
                    .append( escapeSqlColumnOrTable( tableName() ) )
                    .append( " ADD CONSTRAINT " )
                    .append( "FK_" )
                    .append( toSnackCaseFromCamelCase( associationName ) )
                    .append( "_" )
                    .append( hashCode > 0 ? hashCode : -1 * hashCode )
                    .append( " FOREIGN KEY (" )
                    .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( associationName ) ) )
                    .append( ") REFERENCES " )
                    .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( classSimpleName( property.type().type().name() ) ) ) )
                    .append( " (" )
                    .append( escapeSqlColumnOrTable( "id" ) )
                    .append( ");" );

            final Statement foreignKeyStatement = connection.createStatement();

            foreignKeyStatement.executeUpdate( foreignKey.toString() );

            foreignKeyStatement.close();

            logger.info( foreignKey.toString() );

         }
      }

   }

   private void createSubPropertyTable( String propertyClassName ) throws ClassNotFoundException, SQLException, NoSuchFieldException
   {

      if ( subPropertyClasses.add( propertyClassName ) && !propertyClassName.contains( "java." ) && !propertyClassName.equals(EntityReference.class.getName()) )
      {
         final Class clazz = Class.forName( propertyClassName );

         final ArrayList<Class> valuesSubType = new ArrayList<>();

         final Reflections reflections =
                 new Reflections( clazz.getPackage().getName() );

         final Set<Class> subTypesOfClazz = reflections.getSubTypesOf( clazz );

         for ( Class subType : subTypesOfClazz )
         {
            if ( subType.isInterface() )
            {
               valuesSubType.add( subType );
            }
         }

         final String tableName = toSnackCaseFromCamelCase( classSimpleName( propertyClassName ) );

         final ValueDescriptor valueDescriptor = module.valueDescriptor( propertyClassName );

         final Set<PropertyDescriptor> properties = valueDescriptor.state().properties();

         for (Class valuesSubTypeClazz : valuesSubType) {
            properties.addAll( module.valueDescriptor( valuesSubTypeClazz.getName() ).state().properties() );
         }

         final StringBuilder valueTable = new StringBuilder();
         valueTable.append( "CREATE TABLE " )
                 .append( escapeSqlColumnOrTable( tableName ) )
                 .append( " (" )
                 .append( LINE_SEPARATOR );

         valueTable
                 .append( " " )
                 .append( escapeSqlColumnOrTable( "id" ) )
                 .append( " " )
                 .append( detectSqlType( Integer.class ) )
                 .append( " NOT NULL," )
                 .append( LINE_SEPARATOR );

         boolean allow = false;
         for ( PropertyDescriptor property : properties )
         {

            final String columnName = toSnackCaseFromCamelCase( property.qualifiedName().name() );

            allow = true;
            final Class type;
            if ( property.type() instanceof Class )
            {
               type = ( Class ) property.type();

               if ( type.isEnum() || !type.getName().contains( "streamflow" ) )
               {

                  valueTable
                          .append( " " )
                          .append( escapeSqlColumnOrTable( columnName ) )
                          .append( " " )
                          .append( type.equals( EntityReference.class ) ? stringSqlType(255) :  detectSqlType( type ) )
                          .append( " NULL," )
                          .append( LINE_SEPARATOR );

               } else
               {

                  createSubPropertyTable( type.getName() );

                  valueTable
                          .append( " " )
                          .append( escapeSqlColumnOrTable( columnName ) )
                          .append( " " )
                          .append( detectSqlType( Integer.class ) )
                          .append( " NOT NULL," )
                          .append( LINE_SEPARATOR );

                  final int hashCode = ( columnName + tableName ).hashCode();

                  valueTable
                          .append( " CONSTRAINT FK_owner_" )
                          .append( hashCode >= 0 ? hashCode : ( -1 * hashCode ) )
                          .append( " FOREIGN KEY (" )
                          .append( escapeSqlColumnOrTable( columnName ) )
                          .append( ") REFERENCES " )
                          .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( type.getSimpleName() ) ) )
                          .append( " (" )
                          .append( escapeSqlColumnOrTable( "id" ) )
                          .append( ")," )
                          .append( LINE_SEPARATOR );

               }
            } else
            {
               final ParameterizedType paramType = ( ParameterizedType ) property.type();
               final Class rawType = ( Class ) paramType.getRawType();
               final boolean isMap = rawType.equals( Map.class );

               final Class actualType = ( Class ) paramType.getActualTypeArguments()[0];
               if ( !isMap && ( actualType.isEnum() || !actualType.getName().contains( "streamflow" ) && !actualType.getName().contains( "java." ) ) )
               {
                  createSubPropertyTable( actualType.getName() );

                  final StringBuilder collectionTable = new StringBuilder();
                  final String collectionTableName = tableName + "_" + toSnackCaseFromCamelCase( columnName ) + "_cross_ref";

                  collectionTable
                          .append( "CREATE TABLE " )
                          .append( escapeSqlColumnOrTable( collectionTableName ) )
                          .append( " (" )
                          .append( LINE_SEPARATOR )

                          .append( " " )
                          .append( escapeSqlColumnOrTable( "owner_id" ) )
                          .append( detectSqlType( Integer.class ) )
                          .append( " NOT NULL," )
                          .append( LINE_SEPARATOR )

                          .append( " " )
                          .append( escapeSqlColumnOrTable( "link_id" ) )
                          .append( detectSqlType( Integer.class ) )
                          .append( " NOT NULL," )
                          .append( LINE_SEPARATOR );

                  if (!actualType.equals(EntityReference.class))
                  {
                     final int hashCodeOwner = ( tableName + columnName + "owner" ).hashCode();
                     collectionTable
                             .append( " CONSTRAINT FK_owner_" )
                             .append( hashCodeOwner >= 0 ? hashCodeOwner : ( -1 * hashCodeOwner ) )
                             .append( " FOREIGN KEY (" )
                             .append( escapeSqlColumnOrTable( "owner_id" ) )
                             .append( ") REFERENCES " )
                             .append( escapeSqlColumnOrTable( tableName ) )
                             .append( " (" )
                             .append( escapeSqlColumnOrTable( "id" ) )
                             .append( ")," )
                             .append( LINE_SEPARATOR );

                     final int hashCodeLink = (tableName + columnName + "link" ).hashCode();
                     collectionTable
                             .append( " CONSTRAINT FK_link_" )
                             .append( hashCodeLink >= 0 ? hashCodeLink : ( -1 * hashCodeLink ) )
                             .append( " FOREIGN KEY (" )
                             .append( escapeSqlColumnOrTable( "link_id" ) )
                             .append( ") REFERENCES " )
                             .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( actualType.getSimpleName() ) ) )
                             .append( " (" )
                             .append( escapeSqlColumnOrTable( "id" ) )
                             .append( ")," )
                             .append( LINE_SEPARATOR );
                  }

                  collectionTable
                          .append( " PRIMARY KEY (" )
                          .append( escapeSqlColumnOrTable( "owner_id" ) )
                          .append( "," )
                          .append( escapeSqlColumnOrTable( "link_id" ) )
                          .append( ")" )
                          .append( LINE_SEPARATOR )
                          .append( tableEnd() );

                  final Statement statement = connection.createStatement();

                  statement.executeUpdate( collectionTable.toString() );
                  statement.close();

                  logger.info( collectionTable.toString() );

               } else
               {

                  final StringBuilder collectionTable = new StringBuilder();
                  final String collectionTableName = tableName + "_" + toSnackCaseFromCamelCase( property.qualifiedName().name() ) + "_cross_ref";

                  collectionTable
                          .append( "CREATE TABLE " )
                          .append( escapeSqlColumnOrTable( collectionTableName ) )
                          .append( " (" )
                          .append( LINE_SEPARATOR )

                          .append( " " )
                          .append( escapeSqlColumnOrTable( "id" ) )
                          .append( detectSqlType( Integer.class ) )
                          .append( " NOT NULL," )
                          .append( LINE_SEPARATOR )

                          .append( " " )
                          .append( escapeSqlColumnOrTable( "embedded" ) )
                          .append( " INT(11) NOT NULL," )
                          .append( LINE_SEPARATOR )


                          .append( " CONSTRAINT FK_embedded_" )
                          .append( embeddedCounter++ )
                          .append( " FOREIGN KEY (" )
                          .append( escapeSqlColumnOrTable( "embedded" ) )
                          .append( ") REFERENCES " )
                          .append( escapeSqlColumnOrTable( isMap ? "property_map" : "property_collection" ) )
                          .append( " (" )
                          .append( escapeSqlColumnOrTable( "id" ) )
                          .append( ")," )
                          .append( LINE_SEPARATOR )


                          .append( " PRIMARY KEY (" )
                          .append( escapeSqlColumnOrTable( "id" ) )
                          .append( "," )
                          .append( escapeSqlColumnOrTable( "embedded" ) )
                          .append( ") " )
                          .append( LINE_SEPARATOR )
                          .append( tableEnd() )
                          .append( LINE_SEPARATOR );

                  final Statement statement = connection.createStatement();

                  statement.executeUpdate( collectionTable.toString() );
                  statement.close();

                  logger.info( collectionTable.toString() );
               }


            }


         }


         if ( allow )
         {
            valueTable
                    .append( " PRIMARY KEY (" )
                    .append( escapeSqlColumnOrTable( "id" ) )
                    .append( ") " )
                    .append( LINE_SEPARATOR )
                    .append( tableEnd() )
                    .append( LINE_SEPARATOR );

            final Statement statement = connection.createStatement();

            statement.executeUpdate( valueTable.toString() );
            statement.close();

            logger.info( valueTable.toString() );

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
                    .append( stringSqlType( 255 ) )
                    .append( " NOT NULL," )
                    .append( LINE_SEPARATOR );
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
            return ");";//") CHARACTER SET utf8 COLLATE utf8_unicode_ci;";
         default:
            return ");";//") ENGINE='InnoDB'  DEFAULT CHARSET='utf8' COLLATE='utf8_unicode_ci';";
      }
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

   static class PropertyInfo
   {
      private String declaringClassName;
      private String name;

      public PropertyInfo()
      {
      }

      public PropertyInfo( String declaringClassName, String name )
      {
         this.declaringClassName = declaringClassName;
         this.name = name;
      }

      public String getDeclaringClassName()
      {
         return declaringClassName;
      }

      public String getName()
      {
         return name;
      }

      @Override
      public boolean equals( Object o )
      {
         if ( this == o )
            return true;
         if ( o == null || getClass() != o.getClass() )
            return false;

         PropertyInfo that = ( PropertyInfo ) o;

         if ( declaringClassName != null ? !declaringClassName.equals( that.declaringClassName ) : that.declaringClassName != null )
            return false;
         return name != null ? name.equals( that.name ) : that.name == null;
      }

      @Override
      public int hashCode()
      {
         int result = declaringClassName != null ? declaringClassName.hashCode() : 0;
         result = 31 * result + ( name != null ? name.hashCode() : 0 );
         return result;
      }
   }

}
