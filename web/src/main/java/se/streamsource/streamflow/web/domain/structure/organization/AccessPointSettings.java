/**
 *
 * Copyright 2009-2012 Streamsource AB
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
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * JAVADOC
 */
@Mixins(AccessPointSettings.Mixin.class)
public interface AccessPointSettings
{
   void changedProject(Project project);
   void changedCaseType(CaseType caseType);
   void removeCaseType();

   interface Data
   {
      @Optional
      Association<Project> project();

      @Optional
      Association<CaseType> caseType();
   }

   interface Events
   {
      void changedProject( @Optional DomainEvent event, Project project );
      void changedCaseType( @Optional DomainEvent event, CaseType caseType );
      void removedCaseType( @Optional DomainEvent event, CaseType caseType );
   }

   abstract class Mixin
      implements AccessPointSettings, Events
   {
      @This
      Data data;

      public void changedProject(Project project)
      {
         changedProject( null, project );
      }

      public void changedProject( @Optional DomainEvent event, Project project )
      {
         data.project().set( project );
      }

      public void changedCaseType(CaseType caseType)
      {
         changedCaseType( null, caseType );
      }

      public void changedCaseType( @Optional DomainEvent event, CaseType caseType )
      {
         data.caseType().set( caseType );
      }

      public void removeCaseType()
      {
         if( data.caseType().get() != null )
         {
            removedCaseType( null, data.caseType().get() );
         }
      }

      public void removedCaseType( @Optional DomainEvent event, CaseType caseType )
      {
         data.caseType().set( null );
      }
   }
}