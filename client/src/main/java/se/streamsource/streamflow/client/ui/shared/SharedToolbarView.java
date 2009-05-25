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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.StreamFlowApplication;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.FlowLayout;

/**
 * JAVADOC
 */
public class SharedToolbarView
        extends JPanel
        implements TreeSelectionListener
{
    private
    @Service
    StreamFlowApplication app;

    public SharedToolbarView(@Service ApplicationContext context)
    {
        super(new FlowLayout());
        setBorder(BorderFactory.createEtchedBorder());
        setActionMap(context.getActionMap(this));

        javax.swing.Action addAction = getActionMap().get("add");
        add(new JButton(addAction));
        javax.swing.Action removeAction = getActionMap().get("remove");
        add(new JButton(removeAction));

        addAction.setEnabled(false);
        removeAction.setEnabled(false);
    }

    @Action
    public void add()
    {
        // TODO
        JOptionPane.showMessageDialog(app.getMainFrame(), "NYI");
    }

    @Action
    public void remove()
    {
        // TODO
        JOptionPane.showMessageDialog(app.getMainFrame(), "NYI");
    }

    public void valueChanged(TreeSelectionEvent e)
    {
    }
}
