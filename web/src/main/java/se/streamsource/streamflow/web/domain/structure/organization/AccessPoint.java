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

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * JAVADOC
 */
@Mixins(AccessPoint.Mixin.class)
public interface AccessPoint
      extends
      Describable
{
   void addProject( Project project );
   void addCaseType( CaseType caseType );
   void addLabel( Label label );

   interface Data
   {
      Association<Project> project();
      Association<CaseType> caseType();
      ManyAssociation<Label> labels();

      void addedProject( DomainEvent event, Project project );
      void addedCaseType( DomainEvent event, CaseType caseType );
      void addedLabel( DomainEvent event, Label label );
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

      public void addLabel( Label label )
      {
         if ( !labels().contains( label ) )
         {
            addedLabel( DomainEvent.CREATE, label );
         }
      }

      public void addedLabel( DomainEvent event, Label label )
      {
         labels().add( label );
      }
   }
}