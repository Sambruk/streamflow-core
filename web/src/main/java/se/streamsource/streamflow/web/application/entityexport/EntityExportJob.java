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

import org.apache.commons.beanutils.MethodUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Iterables;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entitystore.helpers.JSONEntityState;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Task for export from cache to SQL. It uses {@link EntityExportHelper} to achieve this goals.
 * <br/>
 * Main while loop is bounded with {@link EntityExportService#SIZE_THRESHOLD} and connection created
 * for every entity to prevent memory leak, detected in testing.
 * <br/>
 * {@link EntityExportService#SIZE_THRESHOLD} is 1000 because tests shows this optimal value.
 */
public class EntityExportJob implements Runnable
{

   public static final AtomicBoolean FINISHED = new AtomicBoolean( true );

   private static final Logger logger = LoggerFactory.getLogger( EntityExportJob.class );

   private EntityExportService entityExportService;

   private ModuleSPI module;

   private ServiceReference<DataSource> dataSource;

   @Override
   public void run()
   {
      FINISHED.set( false );

      try (final Connection connection = dataSource.get().getConnection())
      {
          connection.setAutoCommit(false);

         int counterLimit = 0;
         String nextEntity;
         while ( entityExportService.isExported()
                 && (nextEntity = entityExportService.getNextEntity(connection)) != null
                 && EntityExportService.SIZE_THRESHOLD != counterLimit )
         {
               if ( nextEntity.isEmpty() )
               {
                  logger.info( "Entity doesn't exist in cache." );
               }

               final JSONObject entity = new JSONObject( nextEntity );

               final String type = entity.optString( "type" );

               if ( type.isEmpty() )
               {
                  throw new IllegalStateException( "JSON must include type property." );
               }

               final EntityDescriptor entityDescriptor = module.entityDescriptor( type );
               final EntityType entityType = entityDescriptor.entityType();

             final JSONObject propertiesHolder = entity.getJSONObject(JSONEntityState.JSON_KEY_PROPERTIES);

             final Iterable<PropertyType> existsProperties =
                       getNotNullProperties(propertiesHolder, entityType.properties() );
               final Iterable<AssociationType> existsAssociations =
                       getNotNullProperties( entity.getJSONObject(JSONEntityState.JSON_KEY_ASSOCIATIONS), entityType.associations() );
               final Iterable<ManyAssociationType> existsManyAssociations =
                       getNotNullProperties( entity.getJSONObject(JSONEntityState.JSON_KEY_MANYASSOCIATIONS), entityType.manyAssociations() );

               Map<String, Object> subProps = new HashMap<>();

               for ( PropertyType existsProperty : existsProperties )
               {
                  final QualifiedName qualifiedName = existsProperty.qualifiedName();
                  final Object jsonStructure = propertiesHolder.get( qualifiedName.name() );

                  if ( jsonStructure instanceof JSONObject || jsonStructure instanceof JSONArray )
                  {
                     subProps.put( qualifiedName.name(), existsProperty.type().fromJSON( jsonStructure, module ) );
                  }
               }

               final EntityExportHelper entityExportHelper = new EntityExportHelper();

               entityExportHelper.setExistsProperties( existsProperties );
               entityExportHelper.setExistsAssociations( existsAssociations );
               entityExportHelper.setExistsManyAssociations( existsManyAssociations );
               entityExportHelper.setSubProps( subProps );
               entityExportHelper.setConnection( connection );
               entityExportHelper.setEntity( entity );
               entityExportHelper.setAllProperties( entityType.properties() );
               entityExportHelper.setAllManyAssociations( entityType.manyAssociations() );
               entityExportHelper.setAllAssociations( entityType.associations() );
               entityExportHelper.setClassName( type );
               entityExportHelper.setModule( module );
               entityExportHelper.setDbVendor( entityExportService.getDbVendor() );
               entityExportHelper.setTables( entityExportService.getTables() );
               entityExportHelper.setSchemaInfoFileAbsPath( entityExportService.getSchemaInfoFileAbsPath() );
               entityExportHelper.setShowSql( entityExportService.configuration().showSql().get() );
               entityExportService.setTables( entityExportHelper.help() );

               entityExportService.savedSuccess( entity.getString("identity"), entity.getLong("modified"), connection );

               counterLimit++;
         }

      } catch ( Exception e )
      {
          logger.error( "Unexpected error: ", e );
      }

       FINISHED.set( true );
   }

   private <T> Iterable<T> getNotNullProperties( final JSONObject entity, Iterable<T> iterable )
   {
      return Iterables.filter( new Specification<T>()
      {
         @Override
         public boolean satisfiedBy( T item )
         {
            String name;
            try
            {
               name = getQualifiedName( item );
            } catch ( Exception e )
            {
               logger.error( "Error: ", e );
               return false;
            }
            final Object prop = entity.opt( name );
            if ( prop == null || entity.isNull( name ) )
            {
               return false;
            }
            final String json = prop.toString();
            return !jsonEmpty( json );
         }

         private String getQualifiedName( T item )
                 throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
         {
            return ( ( QualifiedName ) MethodUtils.invokeExactMethod( item, "qualifiedName", null ) ).name();
         }

      }, iterable );

   }

   private boolean jsonEmpty( String json )
   {
      return json.isEmpty() || json.equals( "{}" ) || json.equals( "[]" );
   }

   public void setEntityExportService( EntityExportService entityExportService )
   {
      this.entityExportService = entityExportService;
   }

   public void setModule( ModuleSPI module )
   {
      this.module = module;
   }

   public void setDataSource( ServiceReference<DataSource> dataSource )
   {
      this.dataSource = dataSource;
   }
}
