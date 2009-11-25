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
import com.jgoodies.forms.layout.ConstantSize;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class FormSubmitView
        extends JScrollPane
{
    private DefaultFormBuilder formBuilder;
    FormLayout formLayout = new FormLayout(
            "pref, 4dlu, 150dlu","");

    private Map<EntityReference, TextField> fields;

    @Structure
    ValueBuilderFactory vbf;
    private FormSubmitModel model;
    private JPanel panel;

    public FormSubmitView(@Service ApplicationContext context)
    {
        ActionMap am = context.getActionMap(this);
        setActionMap(am);

        panel = new JPanel();
        setViewportView(panel);

        fields = new HashMap<EntityReference, TextField>();
    }

    public void setModel(FormSubmitModel model)
    {
        this.model = model;
        if (model != null)
        {
            panel.removeAll();
            fields.clear();
            formBuilder = new DefaultFormBuilder(formLayout, panel);
            ConstantSize lineGap = new ConstantSize(10 , ConstantSize.MILLIMETER);
            formBuilder.setLineGapSize(lineGap);

            for (ListItemValue value : model.getFields())
            {
                TextField textField = new TextField(30);
                fields.put(value.entity().get(), textField);
                formBuilder.append(value.description().get(), textField);
            }
            panel.revalidate();
            panel.repaint();
        }
    }

    public SubmitFormDTO getSubmitFormDTO()
    {
        ValueBuilder<SubmitFormDTO> submittedFormBuilder = vbf.newValueBuilder(SubmitFormDTO.class);
        ValueBuilder<SubmittedFieldValue> fieldBuilder =vbf.newValueBuilder(SubmittedFieldValue.class);
        java.util.List<SubmittedFieldValue> fields = new ArrayList<SubmittedFieldValue>();

        for (Map.Entry<EntityReference, TextField> stringComponentEntry : this.fields.entrySet())
        {
            fieldBuilder.prototype().field().set(stringComponentEntry.getKey());
            fieldBuilder.prototype().value().set(stringComponentEntry.getValue().getText());
            fields.add(fieldBuilder.newInstance());
        }

        submittedFormBuilder.prototype().values().set(fields);
        submittedFormBuilder.prototype().form().set(model.formReference);
        return submittedFormBuilder.newInstance();
    }
}