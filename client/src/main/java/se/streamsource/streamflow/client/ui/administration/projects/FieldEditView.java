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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FieldEditView
    extends JScrollPane
{


    public FieldEditView(@Service ApplicationContext context,
                         @Uses FieldEditModel model)
    {
        JPanel panel = new JPanel(new BorderLayout());
        ActionMap am = context.getActionMap(this);

        JPanel fieldPanel = new JPanel();
        model.refresh();
        FieldDefinitionValue value = model.getField();

        FormLayout formLayout = new FormLayout(
                "pref, 4dlu, 150dlu","");

        DefaultFormBuilder formBuilder = new DefaultFormBuilder(formLayout, fieldPanel);
        ConstantSize lineGap = new ConstantSize(10 , ConstantSize.MILLIMETER);
        formBuilder.setLineGapSize(lineGap);

        formBuilder.append("Value Definition:", new JLabel(value.valueDefinition().get()));
        formBuilder.append("Description", new TextField(value.description().get()));
        TextArea textArea = new TextArea(value.note().get());
        //textArea.setLineWrap(true);
        //textArea.setWrapStyleWord(true);
        formBuilder.append("Note", textArea);
        panel.add(fieldPanel, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));
        panel.add(toolbar, BorderLayout.SOUTH);

        setViewportView(panel);
    }

    @org.jdesktop.application.Action
    public void add()
    {
        
    }

    @org.jdesktop.application.Action
    public void remove()
    {

    }
}