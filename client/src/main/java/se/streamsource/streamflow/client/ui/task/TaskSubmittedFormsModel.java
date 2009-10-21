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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.task.TaskSubmittedFormsClientResource;
import se.streamsource.streamflow.resource.task.SubmittedFormListDTO;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * List of contacts for a task
 */
public class TaskSubmittedFormsModel
        extends AbstractListModel
        implements Refreshable

{
    @Structure
    ValueBuilderFactory vbf;

    @Uses
    TaskSubmittedFormsClientResource taskSubmittedForms;

    List<SubmittedFormListDTO> submittedForms = Collections.emptyList();

    public void refresh()
    {
        try
        {
            submittedForms = taskSubmittedForms.taskSubmittedForms().forms().get();
            fireContentsChanged(this, 0, getSize());
        } catch (Exception e)
        {
            throw new OperationException(TaskResources.could_not_refresh, e);
        }
    }

    public TaskSubmittedFormsClientResource getTaskSubmittedFormsClientResource()
    {
        return taskSubmittedForms;
    }

    public int getSize()
    {
        return submittedForms.size();
    }

    public Object getElementAt(int i)
    {
        return submittedForms.get(i);
    }

}