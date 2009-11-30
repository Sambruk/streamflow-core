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

package se.streamsource.streamflow.client.ui.administration.projects.forms;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldTypes;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FieldValueCommentEditView
    extends JScrollPane
{


    public FieldValueCommentEditView(@Service ApplicationContext context,
                                     @Uses FieldValueEditModel model)
    {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel fieldPanel = new JPanel();
        FieldDefinitionValue value = model.getField();

        FormLayout formLayout = new FormLayout(
                "pref, 4dlu, 150dlu","");

        DefaultFormBuilder formBuilder = new DefaultFormBuilder(formLayout, fieldPanel);

        formBuilder.append("Type:", new JLabel(i18n.text(FieldTypes.comment)));
        formBuilder.append("Mandatory", new Checkbox());
        formBuilder.append("Name", new TextField(value.description().get()));
        TextArea textArea = new TextArea(value.note().get());
        //textArea.setLineWrap(true);
        //textArea.setWrapStyleWord(true);
        formBuilder.append("Description", textArea);
        panel.add(fieldPanel, BorderLayout.CENTER);

        setViewportView(panel);
    }
}