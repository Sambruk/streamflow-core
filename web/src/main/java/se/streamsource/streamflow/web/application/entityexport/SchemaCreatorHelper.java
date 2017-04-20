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

import org.json.JSONException;
import org.qi4j.runtime.types.CollectionType;
import org.qi4j.runtime.types.MapType;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.property.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ruslan on 09.03.17.
 */
public class SchemaCreatorHelper extends AbstractExportHelper
{
   private static final Logger logger = LoggerFactory.getLogger( SchemaCreatorHelper.class );

   private EntityType entityType;
   private EntityInfo entityInfo;

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

            if ( !tables.containsKey( tableName() ) )
            {
               final String mainTable = mainTableCreateStatement();

               try ( final Statement statement = connection.createStatement() )
               {
                  statement.executeUpdate( mainTable );
               }

               if ( showSql )
               {
                  logger.info( mainTable );
               }

               saveTablesState();
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

            createForeignKeys();

         }
      }

   }

   private void createForeignKeys() throws ClassNotFoundException, SQLException, IOException, JSONException {
      for ( AssociationType association : entityType.associations() )
      {
         final String associationName = toSnakeCaseFromCamelCase( association.qualifiedName().name() );
         String associationTable = null;

         final Class<?> clazz = Class.forName( association.type().name() );

         int i = 0;
         for ( EntityInfo info : EntityInfo.values() )
         {
            if ( clazz.isAssignableFrom( info.getEntityClass() ) )
            {
               associationTable = toSnakeCaseFromCamelCase( info.getClassSimpleName() );
               i++;
            }
         }


         if ( i == 1 )
         {
            final int hashCode = ( tableName() + associationName ).hashCode();

            final String foreignKeyName = "FK_" + associationName + "_" + ( hashCode > 0 ? hashCode : -1 * hashCode );

            if ( !tables.containsKey( foreignKeyName ) )
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

               try ( final Statement statement = connection.createStatement() )
               {
                  statement.executeUpdate( foreignKey.toString() );
               }

               tables.put( foreignKeyName, new HashSet<String>() );

               saveTablesState();

               if ( showSql )
               {
                  logger.info( foreignKey.toString() );
               }

            }
         }

      }

   }

   private String mainTableCreateStatement() throws ClassNotFoundException, SQLException
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
                       .append( escapeSqlColumnOrTable( toSnakeCaseFromCamelCase( property.qualifiedName().name() ) ) )
                       .append( " " )
                       .append( detectSqlType( clazz ) )
                       .append( " NULL," )
                       .append( LINE_SEPARATOR );

               final Set<String> columns = tables.get( tableName() );
               columns.add( toSnakeCaseFromCamelCase( property.qualifiedName().name() ) );
            }

         }

      }


      for ( AssociationType association : entityType.associations() )
      {
         final String associationName = toSnakeCaseFromCamelCase( association.qualifiedName().name() );

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

      return mainTableCreate.toString();
   }

   @Override
   protected String tableName()
   {
      return toSnakeCaseFromCamelCase( entityInfo.getClassSimpleName() );
   }
}
