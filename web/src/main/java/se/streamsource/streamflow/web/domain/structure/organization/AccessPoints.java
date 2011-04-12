/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.entity.association.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.unitofwork.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.domain.entity.organization.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.label.*;
import se.streamsource.streamflow.web.domain.structure.project.*;

import java.util.*;

/**
 * JAVADOC
 */
@Mixins(AccessPoints.Mixin.class)
public interface
      AccessPoints
{
   void createAccessPoint( String name );

   AccessPoint createAccessPoint( String name, Project project, CaseType caseType, @Optional List<Label> labels );

   boolean removeAccessPoint( AccessPoint accessPoint );

   interface Data
   {
      @Aggregated
      ManyAssociation<AccessPoint> accessPoints();

      AccessPoint createdAccessPoint( @Optional DomainEvent event, String id, Project project, CaseType caseType, @Optional List<Label> labels );

      void addedAccessPoint( @Optional DomainEvent event, AccessPoint accessPoint );

      void removedAccessPoint( @Optional DomainEvent event, AccessPoint accessPoint );
   }

   abstract class Mixin
         implements AccessPoints, Data
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Service
      IdentityGenerator idGen;

      public AccessPoint createAccessPoint( String name, Project project, CaseType caseType, @Optional List<Label> labels )
      {
         AccessPoint ap = createdAccessPoint( null, idGen.generate( Identity.class ), project, caseType, labels );

         addedAccessPoint( null, ap );
         ap.changeDescription( name );

         return ap;
      }

      public void createAccessPoint( String name )
      {
         // Check if access point already exists
         List<AccessPoint> accessPoints = accessPoints().toList();
         for (AccessPoint accessPoint : accessPoints)
         {
            if (accessPoint.getDescription().equals( name ))
            {
               throw new IllegalArgumentException( "accesspoint_already_exists" );
            }
         }

         AccessPoint ap = createdAccessPoint( null, idGen.generate( Identity.class ) );

         addedAccessPoint( null, ap );
         ap.changeDescription( name );
      }

      public AccessPoint createdAccessPoint( @Optional DomainEvent event, String id )
      {
         EntityBuilder<AccessPoint> entityBuilder = uowf.currentUnitOfWork().newEntityBuilder( AccessPoint.class, id );

         return entityBuilder.newInstance();
      }

      public AccessPoint createdAccessPoint( @Optional DomainEvent event, String id, Project project, CaseType caseType, @Optional List<Label> labels )
      {
         EntityBuilder<AccessPointEntity> entityBuilder = uowf.currentUnitOfWork().newEntityBuilder( AccessPointEntity.class, id );
         entityBuilder.instance().project().set( project );
         entityBuilder.instance().caseType().set( caseType );
         for (Label label : labels)
         {
            entityBuilder.instance().labels().add( label );
         }

         return entityBuilder.newInstance();
      }

      public boolean removeAccessPoint( AccessPoint accessPoint )
      {
         if (!accessPoints().contains( accessPoint ))
            return false;

         removedAccessPoint( null, accessPoint );
         accessPoint.removeEntity();
         return true;
      }
   }
}