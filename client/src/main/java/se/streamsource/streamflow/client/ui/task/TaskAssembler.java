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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.AssemblyException;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;

/**
 * JAVADOC
 */
public class TaskAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        UIAssemblers.addViews(module, TasksDetailView.class, TableSelectionView.class, TaskContactsAdminView.class,
                TaskFormsAdminView.class, TaskSubmittedFormsAdminView.class, TaskEffectiveFieldsValueView.class);

        UIAssemblers.addDialogs(module, AddCommentDialog.class);

        UIAssemblers.addModels( module, TasksModel.class );

        UIAssemblers.addMV(module,
                TaskModel.class,
                TaskDetailView.class);

        UIAssemblers.addMV(module,
                TaskCommentsModel.class,
                TaskCommentsView.class);

        UIAssemblers.addMV(module,
                TaskContactsModel.class,
                TaskContactsView.class);

        UIAssemblers.addMV(module,
                TaskContactModel.class,
                TaskContactView.class);

        UIAssemblers.addMV(module,
                TaskGeneralModel.class,
                TaskGeneralView.class);

        UIAssemblers.addMV(module,
                TaskSubmittedFormsModel.class,
                TaskSubmittedFormsView.class);

        UIAssemblers.addMV(module,
                TaskSubmittedFormModel.class,
                TaskSubmittedFormView.class);
    }
}
