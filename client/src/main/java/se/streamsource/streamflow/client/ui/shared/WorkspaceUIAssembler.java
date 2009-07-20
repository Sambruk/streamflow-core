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

package se.streamsource.streamflow.client.ui.shared;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableMultipleSelectionModel;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSingleSelectionModel;

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
                UsersIndividualSearch.class,
                ProjectsIndividualSearch.class);

        UIAssemblers.addModels(module, TableMultipleSelectionModel.class,
                TableSingleSelectionModel.class);
        UIAssemblers.addViews(module, TableSelectionView.class);

        UIAssemblers.addMV(module,
                WorkspaceModel.class,
                WorkspaceView.class);


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

        //UIAssemblers.addViews(module, TaskView.class);
        UIAssemblers.addDialogs(module, AddTaskDialog.class,
                ForwardTasksDialog.class,
                DelegateTasksDialog.class);
        UIAssemblers.addDialogs(module, AddCommentDialog.class);

        UIAssemblers.addMV(module,
                UserInboxTaskDetailModel.class,
                UserInboxTaskDetailView.class);

        UIAssemblers.addMV(module,
                UserDelegationsTaskDetailModel.class,
                UserDelegationsTaskDetailView.class);

        UIAssemblers.addMV(module,
                UserAssignmentsTaskDetailModel.class,
                UserAssignmentsTaskDetailView.class);

        UIAssemblers.addMV(module,
                UserWaitingForTaskDetailModel.class,
                UserWaitingForTaskDetailView.class);

        UIAssemblers.addMV(module,
                TaskCommentsModel.class,
                TaskCommentsView.class);

        UIAssemblers.addMV(module,
                TaskGeneralModel.class,
                TaskGeneralView.class);
                //SharedInboxGeneralTaskDetailView.class);

    }
}
