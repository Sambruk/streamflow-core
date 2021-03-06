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
package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(SelectedCaseTypes.Mixin.class)
public interface SelectedCaseTypes
   extends SelectedCaseTypesQueries
{
   void addSelectedCaseType( CaseType caseType );

   void removeSelectedCaseType( CaseType caseType );

   boolean hasSelectedCaseType( CaseType caseType );

   interface Data
   {
      ManyAssociation<CaseType> selectedCaseTypes();

      void selectedCaseTypeAdded( @Optional DomainEvent event, CaseType caseType );

      void selectedCaseTypeRemoved( @Optional DomainEvent event, CaseType caseType );
   }

   abstract class Mixin
         implements SelectedCaseTypes, Data
   {
      @Structure
      Module module;

      public void addSelectedCaseType( CaseType caseType )
      {
         selectedCaseTypeAdded( null, caseType );
      }

      public void removeSelectedCaseType( CaseType caseType )
      {
         selectedCaseTypeRemoved( null, caseType );
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