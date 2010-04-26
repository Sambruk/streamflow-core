/**
 *
 * Copyright 2009-2010 Streamsource AB
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
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;

import java.util.List;

import static se.streamsource.streamflow.infrastructure.event.DomainEvent.*;

/**
 * JAVADOC
 */
@Mixins(AccessPoints.Mixin.class)
public interface AccessPoints
{
   AccessPoint createAccessPoint( String name, Project project, CaseType caseType, @Optional List<Label> labels );

   interface Data
   {
      @Aggregated
      ManyAssociation<AccessPoint> accessPoints();

      AccessPoint createdAccessPoint( DomainEvent event, String id, Project project, CaseType caseType, @Optional List<Label> labels );

      void addedAccessPoint( DomainEvent event, AccessPoint accessPoint );
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
         AccessPoint ap = createdAccessPoint( CREATE, idGen.generate( Identity.class), project, caseType, labels );

         addedAccessPoint( CREATE, ap );
         ap.changeDescription( name );

         return ap;
      }

      public AccessPoint createdAccessPoint( DomainEvent event, String id, Project project, CaseType caseType, @Optional List<Label> labels  )
      {
         EntityBuilder<AccessPoint> entityBuilder = uowf.currentUnitOfWork().newEntityBuilder( AccessPoint.class, id );
         entityBuilder.instance().addProject( project );
         entityBuilder.instance().addCaseType( caseType );
         for (Label label : labels)
         {
            entityBuilder.instance().addLabel( label );
         }

         return entityBuilder.newInstance();
      }

   }
}