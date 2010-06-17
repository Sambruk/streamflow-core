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
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * JAVADOC
 */
@Mixins(AccessPoint.Mixin.class)
public interface AccessPoint
      extends
      Labelable, Describable, Removable
{
   void addProject( Project project );
   void addCaseType( CaseType caseType );
   void removeCaseType();

   interface Data
   {
      @Optional
      Association<Project> project();

      @Optional
      Association<CaseType> caseType();

      void addedProject( DomainEvent event, Project project );
      void addedCaseType( DomainEvent event, CaseType caseType );
      void removedCaseType( DomainEvent event, CaseType caseType );
   }

   abstract class Mixin
      implements AccessPoint, Data
   {
      public void addProject( Project project )
      {
         addedProject( DomainEvent.CREATE, project );
      }

      public void addedProject( DomainEvent event, Project project )
      {
         project().set( project );
      }

      public void addCaseType( CaseType caseType )
      {
         addedCaseType( DomainEvent.CREATE, caseType );
      }

      public void addedCaseType( DomainEvent event, CaseType caseType )
      {
         caseType().set( caseType );
      }

      public void removeCaseType()
      {
         if( caseType().get() != null )
         {
            removedCaseType( DomainEvent.CREATE, caseType().get() );
         }
      }

      public void removedCaseType( DomainEvent event, CaseType caseType )
      {
         caseType().set( null );
      }
   }
}