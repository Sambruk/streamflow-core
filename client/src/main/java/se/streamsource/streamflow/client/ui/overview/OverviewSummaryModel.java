/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.overview;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.OperationException;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.resource.users.overview.OverviewClientResource;
import static se.streamsource.streamflow.client.ui.overview.OverviewResources.*;
import se.streamsource.streamflow.resource.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class OverviewSummaryModel
    extends AbstractTableModel
{

    @Uses
    OverviewClientResource resource;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    ObjectBuilderFactory obf;


    private List<ProjectSummaryDTO> projectOverviews;

    private String[] columnNames;
    private Class[] columnClasses;


    public OverviewSummaryModel()
    {
        columnNames = new String[]{text(project_column_header), text(inbox_column_header),
                text(assigned_column_header), text(total_column_header)};
        columnClasses = new Class[]{String.class, Integer.class, Integer.class, Integer.class};
    }
    public OverviewClientResource getResource()
    {
        return resource;
    }

    @Override
    public Class<?> getColumnClass(int column)
    {
        return columnClasses[column];
    }

    @Override
    public String getColumnName(int column)
    {
        return columnNames[column];
    }

    public int getRowCount()
    {
        if (projectOverviews != null)
            return projectOverviews.size();
        else
            return 0;
    }

    public int getColumnCount()
    {
        return 4;
    }


    public Object getValueAt(int rowIndex, int column)
    {
        ProjectSummaryDTO projectOverview = projectOverviews.get(rowIndex);
        int total = projectOverview.inboxCount().get() + projectOverview.assignedCount().get();

        if (projectOverview == null)
            return null;

        switch (column)
        {
            case 0:
                return new StringBuilder(projectOverview.project().get()).toString();
            case 1:
                return new StringBuilder(projectOverview.inboxCount().get());
            case 2:
                return new StringBuilder(projectOverview.assignedCount().get());
            case 3:
                return new StringBuilder(total);

        }

        return null;
    }


    public void refresh() {
        try
        {
            ProjectSummaryListDTO newResource = (ProjectSummaryListDTO) getResource().overview();
            boolean same = newResource.equals(projectOverviews);
            if (!same)
            {
                projectOverviews = newResource.projectOverviews().get();
                fireTableDataChanged();
            }
        } catch(Exception e)
        {
            throw new OperationException(OverviewResources.could_not_refresh, e);
        } 
    }
}
