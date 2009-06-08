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

package se.streamsource.streamflow.client.ui.shared;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

/**
 * JAVADOC
 */
public class SharedInboxTaskDetailView
        extends JTabbedPane
{
    public SharedInboxTaskDetailView(@Service ApplicationContext appContext,
                                     @Service SharedInboxGeneralTaskDetailView generalView,
                                     @Service InboxTaskCommentsView commentsView,
                                     @Service final TaskGeneralModel generalModel)
    {
        addTab(i18n.text(SharedResources.general_tab), generalView);
        addTab(i18n.text(SharedResources.metadata_tab), new JLabel("TODO"));
        addTab(i18n.text(SharedResources.comments_tab), commentsView);
        addTab(i18n.text(SharedResources.attachments_tab), new JLabel("TODO"));
    }

    @Override
    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);
        getSelectedComponent().setVisible(aFlag);
    }
}