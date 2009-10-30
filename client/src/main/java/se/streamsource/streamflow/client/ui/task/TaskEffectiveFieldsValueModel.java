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
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.task.TaskSubmittedFormsClientResource;
import se.streamsource.streamflow.domain.form.EffectiveFieldValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

/**
 * List of contacts for a task
 */
public class TaskEffectiveFieldsValueModel
        extends AbstractTableModel
        implements Refreshable, EventListener, EventHandler

{

    String[] columnNames = {
            i18n.text(WorkspaceResources.field_date),
            i18n.text(WorkspaceResources.field_name),
            i18n.text(WorkspaceResources.field_value),
            i18n.text(WorkspaceResources.field_submitter)
    };

    private SimpleDateFormat formatter = new SimpleDateFormat(i18n.text(WorkspaceResources.date_format));

    @Structure
    ValueBuilderFactory vbf;

    @Uses
    TaskSubmittedFormsClientResource taskSubmittedForms;

    List<EffectiveFieldValue> effectiveFields = Collections.emptyList();

    EventHandlerFilter eventFilter = new EventHandlerFilter(this, "formSubmitted");

    public void refresh()
    {
        try
        {
            effectiveFields = taskSubmittedForms.effectiveFields().fields().get();
        } catch (Exception e)
        {
            throw new OperationException(TaskResources.could_not_refresh, e);
        }
    }

    public TaskSubmittedFormsClientResource getTaskSubmittedFormsClientResource()
    {
        return taskSubmittedForms;
    }

    public int getRowCount()
    {
        return effectiveFields==null?0:effectiveFields.size();
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public Object getValueAt(int row, int col)
    {
        EffectiveFieldValue value = effectiveFields.get(row);

        switch(col)
        {
            case 0:
                return formatter.format(value.submissionDate().get());
            case 1:
                return value.field().get().toString();
            case 2:
                return value.value().get();
            case 3:
                return value.submitter().get().toString();
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    @Override
    public String getColumnName(int i)
    {
        return columnNames[i];
    }

    public void notifyEvent( DomainEvent event )
    {
        eventFilter.handleEvent( event );
    }

    public boolean handleEvent( DomainEvent event )
    {
        if (taskSubmittedForms.getRequest().getResourceRef().getParentRef().getLastSegment().equals( event.entity().get()))
        {
            Logger.getLogger("workspace").info("Refresh effective field");
            refresh();
        }

        return false;
    }
}