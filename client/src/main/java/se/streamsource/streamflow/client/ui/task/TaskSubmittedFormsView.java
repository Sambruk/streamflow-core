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
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.task.TaskSubmittedFormsClientResource;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.task.SubmittedFormListDTO;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * JAVADOC
 */
public class TaskSubmittedFormsView
        extends JPanel
{
    private TaskSubmittedFormsModel model;
    private JXList submittedForms;

    @Service
    DialogService dialogs;

    @Structure
    ObjectBuilderFactory obf;

    private SimpleDateFormat formatter = new SimpleDateFormat(i18n.text(WorkspaceResources.date_format));

    public TaskSubmittedFormsView(@Service ApplicationContext context)
    {
        super(new BorderLayout());

        ActionMap am = context.getActionMap(this);
        setActionMap(am);
        setMinimumSize(new Dimension(150, 0));

        submittedForms = new JXList();
        submittedForms.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1)
            {
                SubmittedFormListDTO listDTO = (SubmittedFormListDTO) o;
                String dateString = formatter.format(listDTO.submissionDate().get());
                String listItem = dateString + ":" + listDTO.form().get() + " (" + listDTO.submitter().get() +")";
                return super.getListCellRendererComponent(jList, listItem, i, b, b1);
            }
        });
        JScrollPane submittedFormsScollPane = new JScrollPane();
        submittedFormsScollPane.setViewportView(submittedForms);

        add(submittedFormsScollPane, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        add(toolbar, BorderLayout.SOUTH);
    }

    @org.jdesktop.application.Action
    public void add() throws IOException, ResourceException
    {
        FormSubmissionWizard dialog =
                obf.newObjectBuilder(FormSubmissionWizard.class).
                        use(model.getTaskFormDefinitionsResource(), model.getTaskSubmittedFormsClientResource()).newInstance();

        JFrame frame = new JFrame(i18n.text(WorkspaceResources.form_submit_wizard));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = frame.getContentPane();
        contentPane.add(dialog);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    public void setModel(TaskSubmittedFormsModel model)
    {
        this.model = model;
        submittedForms.setModel(model);
    }

    public JList getSubmittedFormsList()
    {
        return submittedForms;
    }

    public TaskSubmittedFormsClientResource getResource()
    {
        return model.getTaskSubmittedFormsClientResource();
    }

    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        if (b)
        {
            if (model != null)
            {
                model.refresh();
            }
        }
    }
}