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
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.*;
import java.awt.*;

/**
 * Select a name for something.
 */
public class FormCreationDialog
        extends JPanel
{

    FormLayout formLayout = new FormLayout(
            "pref, 4dlu, 150dlu","");
    private TextField textField;
    private JComboBox box;


    public FormCreationDialog(@Service ApplicationContext context)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));

        JPanel panel = new JPanel();
        DefaultFormBuilder formBuilder = new DefaultFormBuilder(formLayout, panel);
        ConstantSize lineGap = new ConstantSize(10 , ConstantSize.MILLIMETER);
        formBuilder.setLineGapSize(lineGap);

        textField = new TextField();
        formBuilder.append(i18n.text(AdministrationResources.name_label), textField);
        add(panel, BorderLayout.CENTER);
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

    public String getName()
    {
        return textField.getText();
    }
}