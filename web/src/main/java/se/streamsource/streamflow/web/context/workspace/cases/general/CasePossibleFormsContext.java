/*
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

package se.streamsource.streamflow.web.context.workspace.cases.general;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;

import java.util.Collections;
import java.util.List;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class CasePossibleFormsContext
      implements IndexContext<List<Form>>
{
   public List<Form> index()
   {
      CaseType caseType = role( TypedCase.Data.class ).caseType().get();

      if (caseType != null)
      {
         return ((SelectedForms.Data) caseType).selectedForms().toList();
      } else {
         return Collections.emptyList();
      }
   }
}