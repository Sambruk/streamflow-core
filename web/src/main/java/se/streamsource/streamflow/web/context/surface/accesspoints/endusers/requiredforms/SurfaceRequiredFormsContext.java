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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.requiredforms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;

/**
 * JAVADOC
 */
public class SurfaceRequiredFormsContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      TypedCase.Data typedCase = RoleMap.role( TypedCase.Data.class );

      SelectedForms.Data forms = (SelectedForms.Data) typedCase.caseType().get();

      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      builder.command( "createformdraft" );

      builder.addDescribables( forms.selectedForms() );

      return builder.newLinks();
   }

   public void createformdraft( EntityValue formReference )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
      Form form = uow.get( Form.class, formReference.entity().get() );

      Case aCase = RoleMap.role( Case.class );
      aCase.createFormDraft( form );
   }
}