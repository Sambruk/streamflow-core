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

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class OverviewSummaryView
    extends JPanel
{
    @Service
    protected DialogService dialogs;

    @Service
    protected StreamFlowApplication application;

    protected JXTable overviewSummaryTable;
    protected OverviewSummaryModel model;

    public void init(@Service ApplicationContext context,
                     @Uses final OverviewSummaryModel model,
                     @Structure final ObjectBuilderFactory obf,
                     @Structure ValueBuilderFactory vbf)
    {
        this.model = model;
        model.refresh();
        setLayout(new BorderLayout());

        ActionMap am = context.getActionMap(OverviewSummaryView.class, this);
        setActionMap(am);


        // Table
        overviewSummaryTable = new JXTable(model);
        overviewSummaryTable.getActionMap().getParent().setParent(am);
        overviewSummaryTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
          KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        overviewSummaryTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
          KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

        JScrollPane overviewSummaryScrollPane = new JScrollPane(overviewSummaryTable);

        overviewSummaryTable.setAutoCreateColumnsFromModel(false);

        JPanel toolBar = new JPanel();
        addToolbarButton(toolBar, "export");

        add(overviewSummaryScrollPane, BorderLayout.CENTER);
        add(toolBar, BorderLayout.SOUTH);
        
        addFocusListener(new FocusAdapter()
        {
            public void focusGained(FocusEvent e)
            {
                overviewSummaryTable.requestFocusInWindow();
            }
        });
    }


    protected Action addToolbarButton(JPanel toolbar, String name)
    {
        ActionMap am = getActionMap();
        Action action = am.get(name);
        action.putValue(Action.SMALL_ICON, i18n.icon((ImageIcon) action.getValue(Action.SMALL_ICON), 16));
        toolbar.add(new JButton(action));
        return action;
    }

    @org.jdesktop.application.Action
    public void export() throws ResourceException
    {
        //TODO export - choose Excel or PDF - do export
    }


}
