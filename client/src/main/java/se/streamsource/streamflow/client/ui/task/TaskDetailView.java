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

package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class TaskDetailView
        extends JTabbedPane
{
    private TaskCommentsView commentsView;
    private TaskGeneralView generalView;
    private TaskContactsAdminView contactsView;
    private TaskSubmittedFormsAdminView formsView;
    private TaskEffectiveFieldsValueView effectiveView;
    private TaskModel model;

    public TaskDetailView(@Service ApplicationContext appContext,
                          @Uses TaskGeneralView generalView,
                          @Uses TaskCommentsView commentsView,
                          @Uses TaskContactsAdminView contactsView,
                          @Uses TaskFormsAdminView formsAdminView,
                          @Structure ObjectBuilderFactory obf)
    {
        super(JTabbedPane.BOTTOM);

        setFocusable(true);

        this.commentsView = commentsView;
        this.generalView = generalView;
        this.contactsView = contactsView;
        this.formsView = formsAdminView.getSubmittedFormsView();
        this.effectiveView = formsAdminView.getEffectiveFieldsValueView();

        addTab(i18n.text(WorkspaceResources.general_tab), i18n.icon(Icons.general), generalView, i18n.text(WorkspaceResources.general_tab));
        addTab(i18n.text(WorkspaceResources.contacts_tab), i18n.icon(Icons.projects), contactsView, i18n.text(WorkspaceResources.contacts_tab));
        addTab(i18n.text(WorkspaceResources.comments_tab), i18n.icon(Icons.comments), commentsView, i18n.text(WorkspaceResources.comments_tab));
        addTab(i18n.text(WorkspaceResources.metadata_tab), i18n.icon(Icons.metadata), formsAdminView, i18n.text(WorkspaceResources.metadata_tab));
        addTab(i18n.text(WorkspaceResources.attachments_tab), i18n.icon(Icons.attachments), new JLabel("Attachments"), i18n.text(WorkspaceResources.attachments_tab));

        setMnemonicAt(0, KeyEvent.VK_1);
        setMnemonicAt(1, KeyEvent.VK_2);
        setMnemonicAt(2, KeyEvent.VK_3);
        setMnemonicAt(3, KeyEvent.VK_4);
        setMnemonicAt(4, KeyEvent.VK_5);

        setFocusable(true);
//        setFocusCycleRoot(true);

/*
        addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                getSelectedComponent().requestFocusInWindow();
            }

            public void focusLost(FocusEvent e)
            {
            }
        });
*/
    }

    public void setTaskModel( TaskModel model)
    {
        this.model = model;
        generalView.setModel(model.general());
        commentsView.setModel(model.comments());
        contactsView.setModel(model.contacts());
        formsView.setModel(model.forms());
        effectiveView.setModel(model.effectiveValues());

        validateTree();
        setEnabled(true);

        if (getSelectedIndex() == -1)
        {
            setSelectedIndex(0);
        }
    }

    public TaskModel getTaskModel()
    {
        return model;
    }

    @Override
    public void setSelectedIndex(int index)
    {
        if (model == null && index != -1)
            return; // Ignore since no model is set

        super.setSelectedIndex(index);
    }
}