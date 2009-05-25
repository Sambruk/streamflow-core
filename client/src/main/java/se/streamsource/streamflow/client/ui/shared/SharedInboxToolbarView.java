/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.FlowLayout;

/**
 * JAVADOC
 */
public class SharedInboxToolbarView
        extends JPanel
        implements TreeSelectionListener
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Service
    private StreamFlowApplication app;

    @Service
    private SharedInboxModel inboxModel;

    @Service
    DialogService dialogs;

    public SharedInboxToolbarView(@Service ActionMap am)
    {
        super(new FlowLayout());
        setBorder(BorderFactory.createEtchedBorder());
        setActionMap(am);

        javax.swing.Action addAction = getActionMap().get("addSharedTask");
        add(new JButton(addAction));
        javax.swing.Action removeAction = getActionMap().get("removeSharedTask");
        add(new JButton(removeAction));

        addAction.setEnabled(true);
        removeAction.setEnabled(false);
    }

    public void valueChanged(TreeSelectionEvent e)
    {
        getActionMap().get("removeSharedTask").setEnabled(e.getPath() != null);
    }
}