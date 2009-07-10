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
public class SharedUIAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addObjects(
                SharedNode.class,
                SharedUserNode.class,
                SharedUserAllInboxesNode.class,
                SharedUserAllAssignmentsNode.class,
                SharedUserAllDelegationsNode.class,
                SharedUserAllWaitingForNode.class,
                SharedUserInboxNode.class,
                SharedUserAssignmentsNode.class,
                SharedUserDelegationsNode.class,
                SharedUserWaitingForNode.class,
                ProjectsNode.class,
                SharedProjectNode.class,
                SharedProjectsAllProjectsNode.class,
                SharedProjectInboxNode.class,
                SharedProjectAssignmentsNode.class,
                SharedProjectDelegationsNode.class,
                SharedProjectWaitingForNode.class,
                UsersIndividualSearch.class,
                ProjectsIndividualSearch.class);

        UIAssemblers.addModels(module, TableMultipleSelectionModel.class,
                TableSingleSelectionModel.class);
        UIAssemblers.addViews(module, TableSelectionView.class);

        UIAssemblers.addMV(module,
                SharedModel.class,
                SharedView.class);


        UIAssemblers.addMV(module,
                SharedInboxModel.class,
                SharedInboxView.class);

        UIAssemblers.addMV(module,
                SharedAssignmentsModel.class,
                SharedAssignmentsView.class);

        UIAssemblers.addMV(module,
                SharedDelegationsModel.class,
                SharedDelegationsView.class);

        UIAssemblers.addMV(module,
                SharedWaitingForModel.class,
                SharedWaitingForView.class);

        //UIAssemblers.addViews(module, SharedTaskView.class);
        UIAssemblers.addDialogs(module, AddSharedTaskDialog.class,
                ForwardSharedTasksDialog.class,
                DelegateSharedTasksDialog.class);
        UIAssemblers.addDialogs(module, AddCommentDialog.class);

        UIAssemblers.addMV(module,
                SharedInboxTaskDetailModel.class,
                SharedInboxTaskDetailView.class);

        UIAssemblers.addMV(module,
                SharedDelegationsTaskDetailModel.class,
                SharedDelegationsTaskDetailView.class);

        UIAssemblers.addMV(module,
                SharedAssignmentsTaskDetailModel.class,
                SharedAssignmentsTaskDetailView.class);

        UIAssemblers.addMV(module,
                SharedWaitingForTaskDetailModel.class,
                SharedWaitingForTaskDetailView.class);

        UIAssemblers.addMV(module,
                TaskCommentsModel.class,
                TaskCommentsView.class);

        UIAssemblers.addMV(module,
                TaskGeneralModel.class,
                TaskGeneralView.class);
                //SharedInboxGeneralTaskDetailView.class);

    }
}
