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
package se.streamsource.streamflow.web.rest.resource.workspace.cases;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.workspace.cases.general.CasePossibleFormContext;
import se.streamsource.streamflow.web.context.workspace.cases.general.CasePossibleFormsContext;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;

/**
 * JAVADOC
 */
public class CasePossibleFormsResource
      extends CommandQueryResource
      implements SubResources
{
   public CasePossibleFormsResource( )
   {
      super( CasePossibleFormsContext.class );
   }

   public void resource( String segment )
   {
      SelectedForms.Data data = possibleForms();
      
      if (data != null)
         findManyAssociation( data.selectedForms(), segment );
      else
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND);

      subResourceContexts( CasePossibleFormContext.class );
   }

   private SelectedForms.Data possibleForms()
   {
      SelectedForms.Data forms = null;
      TypedCase.Data typedCase = RoleMap.role( TypedCase.Data.class );

      CaseType caseType = typedCase.caseType().get();

      if (caseType != null)
      {
         forms = (SelectedForms.Data) caseType;
      }
      return forms;
   }
}
