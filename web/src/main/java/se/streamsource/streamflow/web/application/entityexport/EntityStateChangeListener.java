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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.property.PropertyType;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.util.Primitives;
import se.streamsource.streamflow.web.infrastructure.scheduler.QuartzSchedulerService;

import java.util.Collections;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * JAVADOC
 */
public class EntityStateChangeListener
        implements StateChangeListener
{
   private static final Logger logger = LoggerFactory.getLogger( EntityExportService.class.getName() );

   @This
   Configuration<EntityExportConfiguration> thisConfig;

   @Service
   EntityExportService entityExportService;

   @Service
   QuartzSchedulerService schedulerService;

   private JobKey jobKey;

   @Override
   public void notifyChanges( Iterable<EntityState> changedStates )
   {

      if ( thisConfig.configuration().enabled().get() )
      {

         try
         {
            for ( EntityState changedState : changedStates )
            {
               if ( !changedState.status().equals( EntityStatus.LOADED ) ) {
                  if ( changedState.status().equals( EntityStatus.REMOVED ) )
                  {
                     final JSONObject object = new JSONObject();
                     object
                             .put( "identity", changedState.identity() )
                             .put( "_removed", true );
                     entityExportService.saveToCache( object.toString() );
                  } else
                  {
                     entityExportService.saveToCache( toJSON( changedState ) );
                  }
               }
            }

            if ( jobKey == null ) {
               final JobDetail entityExportJob = newJob( EntityExportJob.class )
                       .storeDurably()
                       .withIdentity( "entityexportjob", "entityexportgroup" )
                       .build();
               schedulerService.addJob( entityExportJob );
               jobKey = entityExportJob.getKey();
            }

            if ( !schedulerService.isExecuting( jobKey ) )
            {
               schedulerService.triggerJob( jobKey );
            }

         } catch ( Exception e )
         {
            logger.error( "Unexpected error.", e );
         }
      }

   }

   /**
    * <pre>
    * {
    *  "_identity": "ENTITY-IDENTITY",
    *  "_types": [ "All", "Entity", "types" ],
    *  "_modified": 123,
    *  "_description": "Main entity type",
    *  "property.name": property.value,
    *  "association.name": "ASSOCIATED-IDENTITY",
    *  "manyassociation.name": [ "ASSOCIATED", "IDENTITIES" ]
    * }
    * </pre>
    */
   public String toJSON( EntityState state )
   {
      JSONObject json;
      try
      {
         json = new JSONObject();

         json.put( "_identity", state.identity() );

         EntityDescriptor entityDesc = state.entityDescriptor();
         EntityType entityType = entityDesc.entityType();

         json.put( "_type", entityType.toString() );
         json.put( "_modified", state.lastModified() );
         json.put( "_description", state.entityDescriptor().toString() );

         // Properties
         for ( PropertyType propType : entityType.properties() )
         {
            if ( propType.queryable() )
            {
               String key = propType.qualifiedName().name();
               Object value = state.getProperty( propType.qualifiedName() );
               if ( value == null || Primitives.isPrimitiveValue( value ) )
               {
                  json.put( key, value );
               } else
               {
                  // TODO Theses tests are pretty fragile, find a better way to fix this, Jackson API should behave better
                  String serialized = propType.type().toJSON( value ).toString();
                  if ( serialized.startsWith( "{" ) )
                  {
                     json.put( key, new JSONObject( serialized ) );
                  } else if ( serialized.startsWith( "[" ) )
                  {
                     json.put( key, new JSONArray( serialized ) );
                  } else
                  {
                     json.put( key, serialized );
                  }
               }
            }
         }

         // Associations
         for ( AssociationDescriptor assocDesc : entityDesc.state().associations() )
         {
            if ( assocDesc.associationType().queryable() )
            {
               String key = assocDesc.qualifiedName().name();
               EntityReference associated = state.getAssociation( assocDesc.qualifiedName() );
               Object value;
               if ( associated == null )
               {
                  value = null;
               } else
               {
                  value = new JSONObject( Collections.singletonMap( "identity", associated.identity() ) );
               }
               json.put( key, value );
            }
         }

         // ManyAssociations
         for ( ManyAssociationDescriptor manyAssocDesc : entityDesc.state().manyAssociations() )
         {
            if ( manyAssocDesc.manyAssociationType().queryable() )
            {
               String key = manyAssocDesc.qualifiedName().name();
               JSONArray array = new JSONArray();
               ManyAssociationState associateds = state.getManyAssociation( manyAssocDesc.qualifiedName() );
               for ( EntityReference associated : associateds )
               {
                  array.put( new JSONObject( Collections.singletonMap( "identity", associated.identity() ) ) );
               }
               json.put( key, array );
            }
         }

         return json.toString();
      } catch ( JSONException e )
      {
         logger.info( "Faild to convert Entity to Json: " + state.identity(), e );
         throw new RuntimeException( "Faild to convert Entity to Json: " + state.identity(), e );
      }
   }

}
