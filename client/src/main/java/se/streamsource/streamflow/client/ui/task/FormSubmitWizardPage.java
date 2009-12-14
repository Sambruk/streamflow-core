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
import org.netbeans.spi.wizard.WizardPage;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class FormSubmitWizardPage
    extends WizardPage
{
    private DefaultFormBuilder formBuilder;
    FormLayout formLayout = new FormLayout(
            "pref, 4dlu, 80dlu","");

    private Map<EntityReference, TextField> allFields;

    @Structure
    ValueBuilderFactory vbf;

    private FormSubmitModel model;

    private JPanel panel;

    public FormSubmitWizardPage(@Service ApplicationContext context,
                                @Uses FormSubmitModel model)
    {
        ActionMap am = context.getActionMap(this);
        setActionMap(am);
        setLayout(new FlowLayout());
        allFields = new HashMap<EntityReference, TextField>();
        setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane();
        panel = new JPanel();
        scroll.setViewportView(panel);
        add(scroll,  BorderLayout.CENTER);
        this.model = model;
    }

    public void updateView(String pageId)
    {
        panel.removeAll();
        formBuilder = new DefaultFormBuilder(formLayout, panel);

        for (ListItemValue value : model.fieldsForPage(pageId))
        {
            TextField textField = new TextField();
            allFields.put(value.entity().get(), textField);
            formBuilder.append(value.description().get(), textField);
        }
        revalidate();
        repaint();
    }


    private SubmitFormDTO getSubmitFormDTO()
    {
        ValueBuilder<SubmitFormDTO> submittedFormBuilder = vbf.newValueBuilder(SubmitFormDTO.class);
        ValueBuilder<SubmittedFieldValue> fieldBuilder =vbf.newValueBuilder(SubmittedFieldValue.class);
        java.util.List<SubmittedFieldValue> fields = new ArrayList<SubmittedFieldValue>();

        for (Map.Entry<EntityReference, TextField> stringComponentEntry : this.allFields.entrySet())
        {
            fieldBuilder.prototype().field().set(stringComponentEntry.getKey());
            fieldBuilder.prototype().value().set(stringComponentEntry.getValue().getText());
            fields.add(fieldBuilder.newInstance());
        }

      submittedFormBuilder.prototype().values().set( fields );
      submittedFormBuilder.prototype().form().set( model.formEntityReference() );
      return submittedFormBuilder.newInstance();
   }

    public Map<EntityReference, TextField> getEnteredFields()
    {
        return allFields;
    }

    public void submit()
    {
        model.submit(getSubmitFormDTO());
    }
}