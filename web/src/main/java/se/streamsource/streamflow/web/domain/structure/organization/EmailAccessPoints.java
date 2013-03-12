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
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.organization.EmailAccessPointEntity;

/**
 * JAVADOC
 */
@Mixins(EmailAccessPoints.Mixin.class)
public interface EmailAccessPoints
{
   EmailAccessPoint createEmailAccessPoint(String email);

   boolean removeEmailAccessPoint(EmailAccessPoint accessPoint);

   EmailAccessPoint getEmailAccessPoint(String email);

   interface Data
   {
      @Aggregated
      ManyAssociation<EmailAccessPoint> emailAccessPoints();
   }

   interface Events
   {
      EmailAccessPoint createdEmailAccessPoint(@Optional DomainEvent event, String id);

      void removedEmailAccessPoint(@Optional DomainEvent event, EmailAccessPoint accessPoint);
   }

   abstract class Mixin
         implements EmailAccessPoints, Events
   {
      @This
      Data data;

      @Structure
      Module module;

      @Service
      IdentityGenerator idGen;

      public EmailAccessPoint createEmailAccessPoint( String email )
      {
         for (EmailAccessPoint accessPoint : data.emailAccessPoints())
         {
            if (accessPoint.getDescription().equals( email ))
            {
               throw new IllegalArgumentException( "accesspoint_already_exists" );
            }
         }

         EmailAccessPoint ap = createdEmailAccessPoint( null, idGen.generate( Identity.class ) );

         ap.changeDescription( email );

         ap.synchronizeTemplates();

         return ap;
      }

      public EmailAccessPoint createdEmailAccessPoint( @Optional DomainEvent event, String id )
      {
         EntityBuilder<EmailAccessPointEntity> entityBuilder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( EmailAccessPointEntity.class, id );
         entityBuilder.instance().subject().set("[{caseid}] {subject}");

         // TODO Default templates
         
         EmailAccessPoint eap = entityBuilder.newInstance();
         data.emailAccessPoints().add(eap);

         return eap;
      }

      public boolean removeEmailAccessPoint( EmailAccessPoint accessPoint )
      {
         if (!data.emailAccessPoints().contains( accessPoint ))
            return false;

         removedEmailAccessPoint( null, accessPoint );
         accessPoint.removeEntity();
         return true;
      }

      public EmailAccessPoint getEmailAccessPoint(String email)
      {
         for (EmailAccessPoint emailAccessPoint : data.emailAccessPoints())
         {
            if (emailAccessPoint.getDescription().equalsIgnoreCase(email))
               return emailAccessPoint;
         }

         throw new IllegalArgumentException("No access point defined for "+email);
      }
   }
}
