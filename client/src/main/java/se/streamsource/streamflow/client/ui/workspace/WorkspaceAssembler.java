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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;

/**
 * JAVADOC
 */
public class WorkspaceAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      UIAssemblers.addViews( module, AccountSelectionView.class );
      module.addObjects( WorkspaceWindow.class ).visibleIn( Visibility.layer );

      module.addObjects(
            WorkspaceNode.class,
            WorkspaceUserNode.class,
            WorkspaceUserInboxNode.class,
            WorkspaceUserAssignmentsNode.class,
            WorkspaceUserDelegationsNode.class,
            WorkspaceUserWaitingForNode.class,
            WorkspaceProjectsNode.class,
            WorkspaceProjectNode.class,
            WorkspaceProjectInboxNode.class,
            WorkspaceProjectAssignmentsNode.class,
            WorkspaceProjectDelegationsNode.class,
            WorkspaceProjectWaitingForNode.class );

      UIAssemblers.addModels( module, ProjectSelectorModel2.class );

      UIAssemblers.addMV( module,
            WorkspaceModel.class,
            WorkspaceView.class );

      // User
      UIAssemblers.addMV( module,
            WorkspaceUserInboxModel.class,
            WorkspaceUserInboxView.class );

      UIAssemblers.addMV( module,
            WorkspaceUserAssignmentsModel.class,
            WorkspaceUserAssignmentsView.class );

      UIAssemblers.addMV( module,
            WorkspaceUserDelegationsModel.class,
            WorkspaceUserDelegationsView.class );

      UIAssemblers.addMV( module,
            WorkspaceUserWaitingForModel.class,
            WorkspaceUserWaitingForView.class );

      // Project
      UIAssemblers.addMV( module,
            WorkspaceProjectInboxModel.class,
            WorkspaceProjectInboxView.class );

      UIAssemblers.addMV( module,
            WorkspaceProjectAssignmentsModel.class,
            WorkspaceProjectAssignmentsView.class );

      UIAssemblers.addMV( module,
            WorkspaceProjectDelegationsModel.class,
            WorkspaceProjectDelegationsView.class );

      UIAssemblers.addMV( module,
            WorkspaceProjectWaitingForModel.class,
            WorkspaceProjectWaitingForView.class );

      UIAssemblers.addDialogs( module, SelectUserOrProjectDialog.class, SelectUserOrProjectDialog2.class, ProjectSelectionDialog.class );

   }
}
