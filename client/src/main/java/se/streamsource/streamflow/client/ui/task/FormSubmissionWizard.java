/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.resource.task.TaskSubmittedFormsClientResource;
import se.streamsource.streamflow.client.resource.task.TaskFormDefinitionsClientResource;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FormSubmissionWizard
        extends JPanel
{
    Dimension dialogSize = new Dimension(600, 300);

    @Structure
    ObjectBuilderFactory obf;

    private FormSubmitView formSubmitView;
    private CardLayout layout = new CardLayout();
    private JPanel panel;

    @Uses
    TaskSubmittedFormsClientResource submittedFormsResource;
    private FormsListView formsListView;
    private JButton previous;
    private JButton next;
    private JButton submit;

    public FormSubmissionWizard(@Service ApplicationContext context,
                                @Uses final FormsListView formsListView,
                                @Uses final FormSubmitView formSubmitView)
    {
        super(new BorderLayout());
        ActionMap am = context.getActionMap(this);
        setActionMap(am);
        setActionMap(context.getActionMap(this));
        setPreferredSize(dialogSize);
        this.formSubmitView = formSubmitView;
        this.formsListView = formsListView;

        panel = new JPanel(layout);
        panel.add(formsListView, "SELECTFORM");
        panel.add(formSubmitView, "INPUTFORM");
        add(panel, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout());
        previous = new JButton(am.get("previous"));
        toolbar.add(previous);
        submit = new JButton(am.get("submit"));
        toolbar.add(submit);
        toolbar.add(new JButton(am.get("cancel")));
        next = new JButton(am.get("next"));
        toolbar.add(next);
        add(toolbar, BorderLayout.SOUTH);

        formsListView.getFormList().getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("next")));
        previous.setEnabled(false);
        submit.setEnabled(false);
    }

    @Action
    public void submit()
    {
        SubmitFormDTO submitDTO = formSubmitView.getSubmitFormDTO();

        try
        {
            submittedFormsResource.submitForm(submitDTO);
        } catch (ResourceException e)
        {
            throw new OperationException(WorkspaceResources.could_not_submit_form, e);
        }

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void next()
    {
        JList formList = formsListView.getFormList();
        ListItemValue value = (ListItemValue) formList.getSelectedValue();
        if (value != null)
        {
            TaskFormDefinitionsClientResource resource = formsListView.getResource();
            FormSubmitModel model =
                    obf.newObjectBuilder(FormSubmitModel.class).
                            use(resource.formDefinition(value.entity().get().identity()), value.entity().get()).newInstance();
            formSubmitView.setModel(model);
        } else
        {
            formSubmitView.setModel(null);
        }
        next.setEnabled(false);
        previous.setEnabled(true);
        submit.setEnabled(true);
        layout.show(panel, "INPUTFORM");
    }

    @Action
    public void previous()
    {
        next.setEnabled(true);
        previous.setEnabled(false);
        submit.setEnabled(false);
        layout.show(panel, "SELECTFORM");
    }
}