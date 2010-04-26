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

package se.streamsource.streamflow.client.ui.administration.casetypes;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsView;

/**
 * JAVADOC
 */
public class CaseTypeAdministrationAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      UIAssemblers.addViews( module, CaseTypesAdminView.class );

      UIAssemblers.addDialogs( module, SelectCaseTypesDialog.class );

      UIAssemblers.addMV( module, SelectedCaseTypesModel.class, SelectedCaseTypesView.class );

      UIAssemblers.addMV( module, FormsModel.class, FormsView.class );

      UIAssemblers.addMV( module, FormModel.class, FormView.class );

      UIAssemblers.addMV( module, CaseTypesModel.class,
            CaseTypesView.class );

      UIAssemblers.addMV( module, CaseTypeModel.class, CaseTypeView.class );
   }
}