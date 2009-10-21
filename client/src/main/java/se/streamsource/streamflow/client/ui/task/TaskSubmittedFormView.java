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

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class TaskSubmittedFormView
        extends JPanel
{
    private JPanel form;
    private JTable fieldValues;
    private CardLayout layout = new CardLayout();

    public TaskSubmittedFormView()
    {
        setLayout(layout);

        form = new JPanel(new BorderLayout());
        fieldValues = new JTable();
        form.add(fieldValues, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane(form);

        add(new JPanel(), "EMPTY");
        add(scrollPane, "FORM");
    }

    public void setModel(TaskSubmittedFormModel model)
    {
        if (model != null)
        {
            //form.removeAll();
            // write header information
            fieldValues.setModel(model);

            layout.show(this, "FORM");
        } else
        {
            layout.show(this, "EMPTY");
        }
    }

}