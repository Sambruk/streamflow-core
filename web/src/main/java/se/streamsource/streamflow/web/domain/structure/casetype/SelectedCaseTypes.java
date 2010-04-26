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

package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(SelectedCaseTypes.Mixin.class)
public interface SelectedCaseTypes
{
   void addSelectedCaseType( CaseType caseType );

   void removeSelectedCaseType( CaseType caseType );

   boolean hasSelectedCaseType( CaseType caseType );

   interface Data
   {
      ManyAssociation<CaseType> selectedCaseTypes();

      void selectedCaseTypeAdded( DomainEvent event, CaseType caseType );

      void selectedCaseTypeRemoved( DomainEvent event, CaseType caseType );
   }

   abstract class Mixin
         implements SelectedCaseTypes, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      public void addSelectedCaseType( CaseType caseType )
      {
         selectedCaseTypeAdded( DomainEvent.CREATE, caseType );
      }

      public void removeSelectedCaseType( CaseType caseType )
      {
         selectedCaseTypeRemoved( DomainEvent.CREATE, caseType );
      }

      public boolean hasSelectedCaseType( CaseType caseType )
      {
         return selectedCaseTypes().contains( caseType );
      }

      public void selectedCaseTypeAdded( DomainEvent event, CaseType caseType )
      {
         selectedCaseTypes().add( caseType );
      }

      public void selectedCaseTypeRemoved( DomainEvent event, CaseType caseType )
      {
         selectedCaseTypes().remove( caseType );
      }
   }
}