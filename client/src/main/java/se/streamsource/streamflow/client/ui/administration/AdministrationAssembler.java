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

package se.streamsource.streamflow.client.ui.administration;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.form.FormsAssembler;
import se.streamsource.streamflow.client.ui.administration.groups.GroupAdministrationAssembler;
import se.streamsource.streamflow.client.ui.administration.label.LabelsAssembler;
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationsAdministrationAssembler;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorAdministrationAssembler;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectAdministrationAssembler;
import se.streamsource.streamflow.client.ui.administration.roles.RoleAdministrationAssembler;
import se.streamsource.streamflow.client.ui.administration.tasktypes.TaskTypeAdministrationAssembler;
import se.streamsource.streamflow.client.ui.administration.users.UserAdministrationAssembler;
import se.streamsource.streamflow.client.ui.workspace.TestConnectionTask;

/**
 * JAVADOC
 */
public class AdministrationAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( AdministrationWindow.class ).visibleIn( Visibility.layer );

      module.addObjects( AdministrationNode.class,
            AccountAdministrationNode.class,
            OrganizationAdministrationNode.class,
            OrganizationalUnitAdministrationNode.class,
            OrganizationAdministrationModel.class );

      UIAssemblers.addViews( module,
            AdministrationView.class );
      UIAssemblers.addMV( module,
            OrganizationalUnitAdministrationModel.class,
            OrganizationalUnitAdministrationView.class );
      UIAssemblers.addMV( module,
            OrganizationAdministrationModel.class,
            OrganizationAdministrationView.class );
      UIAssemblers.addMV( module,
            AdministrationModel.class,
            AdministrationOutlineView.class );

      UIAssemblers.addViews( module, ProfileDialog.class );

      UIAssemblers.addMV( module,
            AccountModel.class,
            AccountView.class );
      UIAssemblers.addDialogs( module,
            ChangePasswordDialog.class,
            SelectOrganizationalUnitDialog.class,
            SelectOrganizationOrOrganizationalUnitDialog.class,
            SelectLinksDialog.class );
      UIAssemblers.addTasks( module, TestConnectionTask.class );

      UIAssemblers.addModels( module, LinksQueryListModel.class );

      // Other administration modules
      new LabelsAssembler().assemble( module.layerAssembly().moduleAssembly( "Labels" ) );
      new UserAdministrationAssembler().assemble( module.layerAssembly().moduleAssembly( "Users" ) );
      new OrganizationsAdministrationAssembler().assemble( module.layerAssembly().moduleAssembly( "Organizations" ) );
      new GroupAdministrationAssembler().assemble( module.layerAssembly().moduleAssembly( "Groups" ) );
      new ProjectAdministrationAssembler().assemble( module.layerAssembly().moduleAssembly( "Projects" ) );
      new TaskTypeAdministrationAssembler().assemble( module.layerAssembly().moduleAssembly( "Task types" ) );
      new RoleAdministrationAssembler().assemble( module.layerAssembly().moduleAssembly( "Roles" ) );
      new FormsAssembler().assemble( module.layerAssembly().moduleAssembly( "Forms" ) );
      new AdministratorAdministrationAssembler().assemble( module.layerAssembly().moduleAssembly( "Administrators" ) );
   }
}
