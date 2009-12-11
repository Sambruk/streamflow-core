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

package se.streamsource.streamflow.client.ui.administration.label;

import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.FilteredList;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class SelectLabelsDialog
    extends JPanel
{
    public FilteredList filteredList = new FilteredList();
    public List<ListItemValue> selected;

    public SelectLabelsDialog( @Service ApplicationContext context, @Uses EventList<ListItemValue> list)
    {
        setActionMap( context.getActionMap(this ));

        setLayout( new BorderLayout() );

        filteredList.setEventList( list );
        add(filteredList, BorderLayout.CENTER);
    }

    public Iterable<ListItemValue> getSelectedLabels()
    {
        return selected;
    }

    @Action
    public void execute()
    {
        selected = new ArrayList<ListItemValue>();
        for (Object item : filteredList.getList().getSelectedValues())
        {
            selected.add( (ListItemValue) item );
        }

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}
