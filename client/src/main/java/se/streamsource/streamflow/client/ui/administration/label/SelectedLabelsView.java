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

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemComparator;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class SelectedLabelsView
        extends JPanel
{
    @Service
    DialogService dialogs;

    @Uses
    Iterable<NameDialog> nameDialogs;

    @Uses
    ObjectBuilder<SelectLabelsDialog> labelsDialogs;

    public JList labelList;

    private SelectedLabelsModel modelSelected;

    public SelectedLabelsView(@Service ApplicationContext context, @Uses SelectedLabelsModel modelSelected )
    {
        super(new BorderLayout());
        this.modelSelected = modelSelected;

        ActionMap am = context.getActionMap(this);
        setActionMap(am);

        labelList = new JList( new EventListModel<ListItemValue>(new SortedList<ListItemValue>(modelSelected.getLabelList(), new ListItemComparator())));

        labelList.setCellRenderer(new ListItemListCellRenderer());

        add(new JScrollPane(labelList), BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));
        add(toolbar, BorderLayout.SOUTH);
        labelList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("remove")));

        addAncestorListener( new RefreshWhenVisible( modelSelected, this) );
    }

    @Action
    public void add()
    {
/*
        NameDialog dialog = nameDialogs.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog, text(AdministrationResources.add_label_title));
        String name = dialog.name();
        if (name != null)
        {
            modelSelected.createLabel( name);
        }
*/
        SelectLabelsDialog dialog = labelsDialogs.use( modelSelected.getPossibleLabels() ).newInstance();

        dialogs.showOkCancelHelpDialog( this, dialog );

        if (dialog.getSelectedLabels() != null)
        {
            for (ListItemValue listItemValue : dialog.getSelectedLabels())
            {
                modelSelected.addLabel( listItemValue.entity().get() );
            }
            modelSelected.refresh();
        }

    }

    @Action
    public void remove()
    {
        ListItemValue selected = (ListItemValue) labelList.getSelectedValue();
        modelSelected.removeLabel( selected.entity().get() );
        modelSelected.refresh();
    }
}