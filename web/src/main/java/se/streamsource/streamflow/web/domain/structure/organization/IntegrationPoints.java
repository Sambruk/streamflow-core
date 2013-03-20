/**
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(IntegrationPoints.Mixin.class)
public interface
      IntegrationPoints
{
   void createIntegrationPoint( String name );

   boolean removeIntegrationPoint( IntegrationPoint integrationPoint );

   interface Data
   {
      @Aggregated
      ManyAssociation<IntegrationPoint> integrationPoints();

      IntegrationPoint createdIntegrationPoint( @Optional DomainEvent event, String id );

      void addedIntegrationPoint( @Optional DomainEvent event, IntegrationPoint integrationPoint );

      void removedIntegrationPoint( @Optional DomainEvent event, IntegrationPoint integrationPoint );
   }

   abstract class Mixin
         implements IntegrationPoints, Data
   {
      @Structure
      Module module;

      @Service
      IdentityGenerator idGen;


      public void createIntegrationPoint( final String name )
      {
         // Check if integration point already exists

        if( Iterables.count( Iterables.filter( new Specification<IntegrationPoint>()
         {
            public boolean satisfiedBy( IntegrationPoint item )
            {
               return item.getDescription().equalsIgnoreCase( name );
            }
         }, integrationPoints() )) > 0 )
        {
           throw new IllegalArgumentException( "integrationpoint_already_exists" );
        }

         IntegrationPoint ip = createdIntegrationPoint( null, idGen.generate( Identity.class ) );

         addedIntegrationPoint( null, ip );
         ip.changeDescription( name );
      }

      public IntegrationPoint createdIntegrationPoint( @Optional DomainEvent event, String id )
      {
         EntityBuilder<IntegrationPoint> entityBuilder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( IntegrationPoint.class, id );

         return entityBuilder.newInstance();
      }


      public boolean removeIntegrationPoint(IntegrationPoint integrationPoint )
      {
         if (!integrationPoints().contains( integrationPoint ))
            return false;

         removedIntegrationPoint( null, integrationPoint );
         integrationPoint.removeEntity();
         return true;
      }
   }
}