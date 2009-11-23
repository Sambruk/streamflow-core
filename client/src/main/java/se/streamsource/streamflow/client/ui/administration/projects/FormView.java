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

package se.streamsource.streamflow.client.ui.administration.projects;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FormView
    extends JPanel
{
    private AdministrationView adminView;
    private FormValue form;
    FormLayout formLayout = new FormLayout(
            "pref, 4dlu, 150dlu","");


    public FormView(@Service ApplicationContext context,
                    @Uses FormValue form,
                    @Uses AdministrationView adminView)
    {
        super(new BorderLayout());

        ActionMap am = context.getActionMap(this);

        this.adminView = adminView;
        this.form = form;

        JTextArea textArea = new JTextArea(form.note().get());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        add(textArea, BorderLayout.CENTER);
        add(new JButton(am.get("edit")), BorderLayout.SOUTH);

    }

    @org.jdesktop.application.Action
    public void edit()
    {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultFormBuilder formBuilder = new DefaultFormBuilder(formLayout, panel);
        ConstantSize lineGap = new ConstantSize(10 , ConstantSize.MILLIMETER);
        formBuilder.setLineGapSize(lineGap);

        formBuilder.append("Description", new TextField(form.description().get()));
        formBuilder.append("Note", new TextArea(form.note().get()));


        formBuilder.append("", new JSeparator());

        formBuilder.append("Field Value");
        formBuilder.append("Field Name");

        for (ListItemValue value : form.fields().get().items().get())
        {
            TextField textField = new TextField(value.description().get());
            //fields.put(value.entity().get(), textField);
            JComboBox box = new JComboBox();
            box.setModel(new ValueDefinitionSelectionModel());
            formBuilder.append(box, textField);
        }

        adminView.show( panel );
    }

}
