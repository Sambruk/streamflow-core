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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * JAVADOC
 */
public class SharedInboxTaskDetailView
        extends JTabbedPane
{
    public SharedInboxTaskDetailView(@Service ApplicationContext appContext,
                                     @Service TaskGeneralModel generalModel,
                                     @Service TaskCommentsModel commentsModel,
                                     @Structure ObjectBuilderFactory obf)
    {
        super(JTabbedPane.LEFT);

        TaskCommentsView commentsView = obf.newObjectBuilder(TaskCommentsView.class).use(commentsModel).newInstance();
        TaskGeneralView generalView = obf.newObjectBuilder(TaskGeneralView.class).use(generalModel).newInstance();

        setUI(new BasicTabbedPaneUI());

/*
        addTab(i18n.text(SharedResources.general_tab), icon, generalView);
        addTab(i18n.text(SharedResources.metadata_tab), i18n.icon(Icons.metadata), new JLabel("TODO"));
        addTab(i18n.text(SharedResources.comments_tab), i18n.icon(Icons.comments), commentsView);
        addTab(i18n.text(SharedResources.attachments_tab), i18n.icon(Icons.attachments), new JLabel("TODO"));
*/

        addTab(null, i18n.icon(Icons.general), generalView, i18n.text(SharedResources.general_tab));
        addTab(null, i18n.icon(Icons.metadata), new JLabel("Metadata"), i18n.text(SharedResources.metadata_tab));
        addTab(null, i18n.icon(Icons.comments), commentsView, i18n.text(SharedResources.comments_tab));
        addTab(null, i18n.icon(Icons.attachments), new JLabel("Attachments"), i18n.text(SharedResources.attachments_tab));
    }

    @Override
    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);
        getSelectedComponent().setVisible(aFlag);
    }
}