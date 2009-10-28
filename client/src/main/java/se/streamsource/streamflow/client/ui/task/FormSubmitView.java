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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FormSubmitView
        extends JPanel
{

    private CardLayout layout = new CardLayout();
    private DefaultFormBuilder formBuilder;
    private JPanel form;
    FormLayout formLayout = new FormLayout(
                "200dlu",
                "");


    public FormSubmitView(@Service ApplicationContext context,
                         @Structure ObjectBuilderFactory obf)
    {
        ActionMap am = context.getActionMap(this);
        setActionMap(am);

        setLayout(layout);


        form = new JPanel();
        JScrollPane scrollPane = new JScrollPane(form);

        add(new JPanel(), "EMPTY");
        add(scrollPane, "CONTACT");

    }

    public void setModel(FormSubmitModel model)
    {
        if (model != null)
        {
            form.removeAll();

            formBuilder = new DefaultFormBuilder(formLayout, form);
            formBuilder.setDefaultDialogBorder();

            for (ListItemValue value : model.getFields())
            {
                formBuilder.append(new JLabel(value.description().get()));
                formBuilder.append(new TextField(30));
                formBuilder.nextLine();
                form.doLayout();
            }

            layout.show(this, "CONTACT");
        } else
        {
            layout.show(this, "EMPTY");
        }

    }
}