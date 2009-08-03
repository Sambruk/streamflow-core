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

package se.streamsource.streamflow.client.ui.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.DetailView;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.Dimension;

/**
 * JAVADOC
 */
public class AdministrationView
        extends JSplitPane
{
    @Structure
    ObjectBuilderFactory obf;

    @Structure
    UnitOfWorkFactory uowf;

    public AdministrationView(@Uses AdministrationOutlineView adminOutlineView)
    {
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(getMinimumSize());

        setLeftComponent(adminOutlineView);
        adminOutlineView.setMinimumSize(new Dimension(200, 400));
        setRightComponent(new JLabel("Bar"));

        setDividerLocation(200);
        setResizeWeight(0);

        adminOutlineView.getTree().addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                if (e.getPath() != null)
                {
                    final DetailView admin = (DetailView) e.getPath().getLastPathComponent();

                    try
                    {
                        JComponent detail = admin.detailView();
                        setRightComponent(detail);
                    } catch (ResourceException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
}
