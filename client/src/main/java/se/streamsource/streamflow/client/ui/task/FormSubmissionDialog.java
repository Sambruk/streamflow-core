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
import se.streamsource.streamflow.client.resource.task.TaskFormDefinitionsClientResource;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * JAVADOC
 */
public class FormSubmissionDialog
        extends JPanel
{
    Dimension dialogSize = new Dimension(600, 300);

    @Structure
    ObjectBuilderFactory obf;

    public FormSubmissionDialog(@Service ApplicationContext context,
                                @Uses final FormsListView formsListView,
                                @Uses final FormSubmitView formSubmitView)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));

        setPreferredSize(dialogSize);

        add(formsListView, BorderLayout.WEST);
        add(formSubmitView, BorderLayout.CENTER);

        final JList formList = formsListView.getFormList();

        formList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    ListItemValue value = (ListItemValue) formList.getSelectedValue();
                    if (value != null)
                    {
                        TaskFormDefinitionsClientResource resource = formsListView.getResource();
                        FormSubmitModel model =
                                obf.newObjectBuilder(FormSubmitModel.class).
                                        use(resource.formDefinition(value.entity().get().identity())).newInstance();
                        formSubmitView.setModel(model);
                    } else
                    {
                        formSubmitView.setModel(null);
                    }
                }

            }
        });


    }

    @Action
    public void execute()
    {

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}