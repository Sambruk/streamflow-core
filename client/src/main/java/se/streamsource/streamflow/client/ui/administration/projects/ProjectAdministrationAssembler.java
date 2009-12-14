/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration.projects;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FieldCreationDialog;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FieldValueEditModel;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FieldValueTextEditView;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FieldsModel;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FieldsView;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FormEditAdminView;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FormsAdminView;

/**
 * JAVADOC
 */
public class ProjectAdministrationAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      UIAssemblers.addViews( module, ProjectAdminView.class );


      UIAssemblers.addMV( module, ProjectsModel.class,
            ProjectsView.class );

      UIAssemblers.addModels( module, ProjectModel.class );

      UIAssemblers.addViews( module, ProjectView.class, FormsAdminView.class, FormEditAdminView.class );

      UIAssemblers.addMV( module, ProjectMembersModel.class,
            ProjectMembersView.class );

      UIAssemblers.addMV( module, FieldsModel.class, FieldsView.class );

      UIAssemblers.addMV( module, FieldValueEditModel.class, FieldValueTextEditView.class );

      UIAssemblers.addDialogs( module, FieldCreationDialog.class );
   }
}