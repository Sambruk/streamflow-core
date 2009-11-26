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

package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemComparator;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemFilterator;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;

public class LabelSelectionView
        extends JPanel
{
    public AutoCompleteSupport support;
    public JTextField filterEdit;
    public JList list;
    public EventListModel listModel;

    public LabelSelectionView()
    {
        setLayout( new BorderLayout() );

        filterEdit = new JTextField( 20 );

        list = new JList();
        list.setCellRenderer( new ListItemListCellRenderer() );

        JScrollPane listPane = new JScrollPane(list);

        add(filterEdit, BorderLayout.NORTH);
        add(listPane, BorderLayout.CENTER);
    }

    public JList getList()
    {
        return list;
    }

    public void setLabelSelectionModel( LabelSelectionModel model )
    {
        SortedList<ListItemValue> sortedIssues = new SortedList<ListItemValue>( model.getList(), new ListItemComparator() );
        FilterList<ListItemValue> textFilteredIssues = new FilterList<ListItemValue>( sortedIssues, new TextComponentMatcherEditor( filterEdit, new ListItemFilterator() ) );
        listModel = new EventListModel<ListItemValue>(textFilteredIssues);

        list.setModel( listModel );

        filterEdit.setText( "" );
    }
}
