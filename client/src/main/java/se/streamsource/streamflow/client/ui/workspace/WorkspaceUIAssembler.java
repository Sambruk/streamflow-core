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

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;

/**
 * JAVADOC
 */
public class WorkspaceUIAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addObjects(
                WorkspaceNode.class,
                UserNode.class,
                UserInboxNode.class,
                UserAssignmentsNode.class,
                UserDelegationsNode.class,
                UserWaitingForNode.class,
                ProjectsNode.class,
                ProjectNode.class,
                ProjectInboxNode.class,
                ProjectAssignmentsNode.class,
                ProjectDelegationsNode.class,
                ProjectWaitingForNode.class,
                LabelMenu.class);

        UIAssemblers.addViews(module, TableSelectionView.class);

        UIAssemblers.addModels(module, LabelsModel.class);

        UIAssemblers.addMV(module,
                WorkspaceModel.class,
                WorkspaceView.class);

        // User
        UIAssemblers.addMV(module,
                UserInboxModel.class,
                UserInboxView.class);

        UIAssemblers.addMV(module,
                UserAssignmentsModel.class,
                UserAssignmentsView.class);

        UIAssemblers.addMV(module,
                UserDelegationsModel.class,
                UserDelegationsView.class);

        UIAssemblers.addMV(module,
                UserWaitingForModel.class,
                UserWaitingForView.class);

        // Project
        UIAssemblers.addMV(module,
                ProjectInboxModel.class,
                ProjectInboxView.class);

        UIAssemblers.addMV(module,
                ProjectAssignmentsModel.class,
                ProjectAssignmentsView.class);

        UIAssemblers.addMV(module,
                ProjectDelegationsModel.class,
                ProjectDelegationsView.class);

        UIAssemblers.addMV(module,
                ProjectWaitingForModel.class,
                ProjectWaitingForView.class);

        UIAssemblers.addDialogs(module, UserOrProjectSelectionDialog.class, ProjectSelectionDialog.class);
        
        UIAssemblers.addDialogs(module, AddCommentDialog.class);

        UIAssemblers.addMV(module,
                TaskDetailModel.class,
                TaskDetailView.class);

        UIAssemblers.addMV(module,
                TaskCommentsModel.class,
                TaskCommentsView.class);

        UIAssemblers.addMV(module,
                TaskGeneralModel.class,
                TaskGeneralView.class);

    }
}
