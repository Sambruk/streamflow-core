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

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * JAVADOC
 */
public class TaskDetailView
        extends JTabbedPane
{
    public TaskDetailView(@Service ApplicationContext appContext,
                          @Uses TaskCommentsView commentsView,
                          @Uses TaskGeneralView generalView,
                                     @Structure ObjectBuilderFactory obf)
    {
        super(JTabbedPane.LEFT);

        setUI(new BasicTabbedPaneUI());

        addTab(null, i18n.icon(Icons.general), generalView, i18n.text(WorkspaceResources.general_tab));
        addTab(null, i18n.icon(Icons.metadata), new JLabel("Metadata"), i18n.text(WorkspaceResources.metadata_tab));
        addTab(null, i18n.icon(Icons.comments), commentsView, i18n.text(WorkspaceResources.comments_tab));
        addTab(null, i18n.icon(Icons.attachments), new JLabel("Attachments"), i18n.text(WorkspaceResources.attachments_tab));
    }

    @Override
    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);
        getSelectedComponent().setVisible(aFlag);
    }
}