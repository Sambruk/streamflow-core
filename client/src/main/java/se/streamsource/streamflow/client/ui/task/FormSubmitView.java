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
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.FieldValue;
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
        extends JPanel
{

    private CardLayout layout = new CardLayout();
    private DefaultFormBuilder formBuilder;
    private JPanel form;
    FormLayout formLayout = new FormLayout(
                "pref, 4dlu, 50dlu",
                "");

    private Map<ListItemValue, TextField> fields;

    @Structure
    ValueBuilderFactory vbf;
    private FormSubmitModel model;

    public FormSubmitView(@Service ApplicationContext context)
    {
        ActionMap am = context.getActionMap(this);
        setActionMap(am);

        setLayout(layout);


        form = new JPanel();
        JScrollPane scrollPane = new JScrollPane(form);

        fields = new HashMap<ListItemValue, TextField>();

        add(new JPanel(), "EMPTY");
        add(scrollPane, "CONTACT");

    }

    public void setModel(FormSubmitModel model)
    {
        this.model = model;
        if (model != null)
        {
            form.removeAll();
            fields.clear();
            formBuilder = new DefaultFormBuilder(formLayout, form);
            formBuilder.setDefaultDialogBorder();

            for (ListItemValue value : model.getFields())
            {
                formBuilder.append(new JLabel(value.description().get()));
                fields.put(value, new TextField(30));
                formBuilder.append(fields.get(value));
                formBuilder.nextLine();
            }

            layout.show(this, "CONTACT");
        } else
        {
            layout.show(this, "EMPTY");
        }

    }

    public SubmitFormDTO getSubmitFormDTO()
    {
        ValueBuilder<SubmitFormDTO> submittedFormBuilder = vbf.newValueBuilder(SubmitFormDTO.class);
        ValueBuilder<FieldValue> fieldBuilder =vbf.newValueBuilder(FieldValue.class);
        java.util.List<FieldValue> fields = new ArrayList<FieldValue>();

        for (Map.Entry<ListItemValue, TextField> stringComponentEntry : this.fields.entrySet())
        {
            fieldBuilder.prototype().field().set(stringComponentEntry.getKey().entity().get());
            fieldBuilder.prototype().value().set(stringComponentEntry.getValue().getText());
            fields.add(fieldBuilder.newInstance());
        }

        submittedFormBuilder.prototype().values().set(fields);
        submittedFormBuilder.prototype().form().set(model.formReference);
        return submittedFormBuilder.newInstance();
    }
}