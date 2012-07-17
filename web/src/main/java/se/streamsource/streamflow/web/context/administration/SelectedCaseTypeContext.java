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
package se.streamsource.streamflow.web.context.administration;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * JAVADOC
 */
public class SelectedCaseTypeContext
      implements DeleteContext
{
   public void delete()
   {
      SelectedCaseTypes caseTypes = role( SelectedCaseTypes.class );
      CaseType caseType = role( CaseType.class );
      caseTypes.removeSelectedCaseType( caseType );
   }
}
