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

package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.*;
import org.qi4j.api.entity.association.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.domain.structure.label.*;

/**
 * JAVADOC
 */
@Mixins(TypedCase.Mixin.class)
public interface TypedCase
{
   void changeCaseType( @Optional CaseType newCaseType );

   interface Data
   {
      @Optional
      Association<CaseType> caseType();

      void changedCaseType( @Optional DomainEvent event, @Optional CaseType caseType );
   }

   abstract class Mixin
         implements Data, TypedCase
   {
      @This
      Labelable labelable;

      public void changeCaseType( @Optional CaseType newCaseType )
      {
         changedCaseType( null, newCaseType );
      }

      public void changedCaseType( @Optional DomainEvent event, @Optional CaseType caseType )
      {
         CaseType currentCaseType = caseType().get();
         if ((currentCaseType == null && caseType != null) || (currentCaseType!=null && !currentCaseType.equals( caseType )))
         {
            labelable.retainLabels( currentCaseType, caseType );

            caseType().set( caseType );
         }
      }
   }
}
