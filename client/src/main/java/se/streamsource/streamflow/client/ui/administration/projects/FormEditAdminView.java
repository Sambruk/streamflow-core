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
import com.jgoodies.forms.layout.FormLayout;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionClientResource;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.FormValue;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;


/**
 * JAVADOC
 */
public class FormEditAdminView
        extends JSplitPane
{

    FormLayout formLayout = new FormLayout(
            "pref, 4dlu, 150dlu","");

    public FormEditAdminView(@Uses FieldsView fieldsView,
                             @Uses FormValue form,
                             @Uses final ProjectFormDefinitionClientResource formResource,
                             @Structure final ObjectBuilderFactory obf)
    {
        super();

        JPanel leftPanel = new JPanel(new BorderLayout());

        DefaultFormBuilder formBuilder = new DefaultFormBuilder(formLayout, leftPanel);

        formBuilder.append("Name", new TextField(form.description().get()));
        formBuilder.append("Description", new TextArea(form.note().get()));

        formBuilder.append("Fields", fieldsView);


        setLeftComponent(leftPanel);
        setRightComponent(new JPanel());

        setDividerLocation(400);

        final JList list = fieldsView.getFieldList();

        fieldsView.getFieldList().addListSelectionListener(new ListSelectionListener()
        {

            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    int idx = list.getSelectedIndex();

                    if (idx < list.getModel().getSize() && idx >= 0)
                    {
                        try
                        {
                            FieldDefinitionValue definitionValue = formResource.fields().field(idx).field();
                            FieldValue value = definitionValue.fieldValue().get();
                            setRightComponent(
                                    obf.newObjectBuilder(ValueDefinitionTextEditView.class).
                                            use(definitionValue).newInstance());

                            /*
                            if (value instanceof TextFieldValue)
                            {
                                setRightComponent(
                                        obf.newObjectBuilder(ValueDefinitionTextEditView.class).
                                                use(definitionValue).newInstance());
                            } else if (value instanceof FieldValue)
                            {
                                setRightComponent(
                                        obf.newObjectBuilder(ValueDefinitionNumberEditView.class).
                                                use(definitionValue).newInstance());
                            } else if (value instanceof FieldValue)
                            {
                                setRightComponent(
                                        obf.newObjectBuilder(ValueDefinitionDateEditView.class).
                                                use(definitionValue).newInstance());
                            } else if (value instanceof FieldValue)
                            {
                                setRightComponent(
                                        obf.newObjectBuilder(ValueDefinitionSingleSelectionEditView.class).
                                                use(definitionValue).newInstance());
                            } else if (value instanceof FieldValue)
                            {
                                setRightComponent(
                                        obf.newObjectBuilder(ValueDefinitionMultiSelectionEditView.class).
                                                use(definitionValue).newInstance());
                            } else if (value instanceof FieldValue)
                            {
                                setRightComponent(
                                        obf.newObjectBuilder(ValueDefinitionCommentEditView.class).
                                                use(definitionValue).newInstance());
                            } else
                            {
                                setRightComponent(new JPanel());
                            }*/

                        } catch (ResourceException e1)
                        {
                            e1.printStackTrace();
                        }
                    } else
                    {
                        setRightComponent(new JPanel());
                    }
                }

            }
        });
    }

}