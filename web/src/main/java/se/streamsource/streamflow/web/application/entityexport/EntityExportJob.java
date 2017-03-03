package se.streamsource.streamflow.web.application.entityexport;

import org.json.JSONObject;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

      @Override
      public void execute( JobExecutionContext context ) throws JobExecutionException
      {
         try
         {

            if ( entityExportService.isExported() && entityExportService.hasNextEntity() )
            {
               final String nextEntity = entityExportService.getNextEntity();

               if ( nextEntity.isEmpty() ) {
                  logger.info( "Entity doesn't exist in cache." );
               }

               final JSONObject jsonObject = new JSONObject( nextEntity );

               final String description = jsonObject.optString( "_description" );

               if ( description.isEmpty() ) {
                  throw new IllegalStateException( "JSON must include _description property." );
               }


               final EntityDescriptor entityDescriptor = moduleSPI.entityDescriptor( description );

               final EntityType entityType = entityDescriptor.entityType();

               List<PropertyType> propertyTypeList = new LinkedList<>(  );
               for ( PropertyType property : entityType.properties() )
               {
                  propertyTypeList.add( property );
               }

               List<AssociationType> associationTypeList = new LinkedList<>(  );
               for ( AssociationType associationType : entityType.associations() )
               {
                  associationTypeList.add( associationType );
               }

               List<ManyAssociationType> manyAssociationTypeList = new LinkedList<>(  );
               for ( ManyAssociationType manyAssociationType : entityType.manyAssociations() )
               {
                  manyAssociationTypeList.add( manyAssociationType );
               }

               final TypeName type = entityType.type();

               entityType.toString();
            }

         } catch ( Exception e )
         {
            throw new JobExecutionException( e );
         }

      }


   }


}
