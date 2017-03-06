package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Iterables;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.entity.caselog.CaseLogEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.ResolutionEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * JAVADOC
 */
@Mixins(EntityExportJob.Mixin.class)
public interface EntityExportJob extends Job, TransientComposite
{

   abstract class Mixin
           implements EntityExportJob
   {
      private final Logger logger = LoggerFactory.getLogger( EntityExportJob.class );

      @Service
      private EntityExportService entityExportService;

      @Structure
      ModuleSPI moduleSPI;

      @Service
      ServiceReference<DataSource> dataSource;

      @Override
      public void execute( JobExecutionContext context ) throws JobExecutionException
      {

         Connection connection = null;

         try
         {

            while ( entityExportService.isExported() && entityExportService.hasNextEntity() )
            {
               final String nextEntity = entityExportService.getNextEntity();

               if ( nextEntity.isEmpty() )
               {
                  logger.info( "Entity doesn't exist in cache." );
               }

               final JSONObject entity = new JSONObject( nextEntity );

               final String description = entity.optString( "_description" );

               //for tests.
               if ( description.equals( AttachmentEntity.class.getName() )
                       || description.equals( CaseLogEntity.class.getName() )
                       || description.equals( CaseTypeEntity.class.getName() )
                       || description.equals( CaseEntity.class.getName() )
                       ) {


                  if ( description.isEmpty() )
                  {
                     throw new IllegalStateException( "JSON must include _description property." );
                  }

                  final EntityDescriptor entityDescriptor = moduleSPI.entityDescriptor( description );
                  final EntityType entityType = entityDescriptor.entityType();

                  final Iterable<PropertyType> existsProperties =
                          getNotNullProperties( entity, entityType.properties() );
                  final Iterable<AssociationType> existsAssociations =
                          getNotNullProperties( entity, entityType.associations() );
                  final Iterable<ManyAssociationType> existsManyAssociations =
                          getNotNullProperties( entity, entityType.manyAssociations() );

                  Map<String, Object> subProps = new HashedMap();

                  for ( PropertyType existsProperty : existsProperties )
                  {
                     final QualifiedName qualifiedName = existsProperty.qualifiedName();
                     final Object jsonStructure = entity.get( qualifiedName.name() );

                     if ( jsonStructure instanceof JSONObject || jsonStructure instanceof JSONArray) {
                        subProps.put( qualifiedName.name(), existsProperty.type().fromJSON( jsonStructure, moduleSPI ) );
                     }
                  }

                  final EntityExportHelper entityExportHelper = new EntityExportHelper();
                  entityExportHelper.setExistsProperties( existsProperties );
                  entityExportHelper.setExistsAssociations( existsAssociations );
                  entityExportHelper.setExistsManyAssociations( existsManyAssociations );;
                  entityExportHelper.setSubProps( subProps );
                  connection = dataSource.get().getConnection();
                  entityExportHelper.setConnection( connection );
                  entityExportHelper.setEntity( entity );
                  entityExportHelper.setAllProperties( entityType.properties() );
                  entityExportHelper.setClassName( description );

                  entityExportHelper.help();

                  entityExportService.savedSuccess();

               } else {
                  break;
               }


            }

         } catch ( Exception e )
         {
            throw new JobExecutionException( e );
         } finally
         {
            try
            {
               if ( connection != null && !connection.isClosed() )
               {
                  connection.close();
               }
            } catch ( SQLException e ) {
               logger.error( "Error:", e );
            }

         }

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



   }


}
