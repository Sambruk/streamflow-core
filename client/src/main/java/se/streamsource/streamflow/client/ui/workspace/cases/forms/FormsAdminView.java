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

package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;


/**
 * JAVADOC
 */
public class FormsAdminView
      extends JTabbedPane
{
   public FormsAdminView( @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      addTab( i18n.text( WorkspaceResources.effective_fields_tab ), obf.newObjectBuilder( CaseEffectiveFieldsValueView.class).use( client ).newInstance() );
      addTab( i18n.text( WorkspaceResources.submitted_forms_tab ), obf.newObjectBuilder( SubmittedFormsAdminView.class).use( client ).newInstance() );
   }
}