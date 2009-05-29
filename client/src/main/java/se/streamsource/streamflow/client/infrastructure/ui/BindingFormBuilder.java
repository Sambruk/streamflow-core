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

package se.streamsource.streamflow.client.infrastructure.ui;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.qi4j.api.property.Property;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.awt.Component;

/**
 * JAVADOC
 */
public class BindingFormBuilder
{
    // Field types
    public enum Fields
    {
        TEXTFIELD
                {
                    public Component newField()
                    {
                        return new JTextField(30);
                    }
                },
        PASSWORD
                {
                    public Component newField()
                    {
                        return new JPasswordField(15);
                    }
                },
        TEXTAREA
                {
                    public Component newField()
                    {
                        return new JScrollPane(new JTextArea(10, 30));
                    }
                },
        CHECKBOX
                {
                    public Component newField()
                    {
                        return new JCheckBox();
                    }
                };

        public abstract Component newField();
    }

    private DefaultFormBuilder formBuilder;
    private ResourceMap resourceMap;
    private StateBinder stateBinder;

    public BindingFormBuilder(DefaultFormBuilder formBuilder, StateBinder stateBinder)
    {
        this(formBuilder, stateBinder, null);
    }

    public BindingFormBuilder(DefaultFormBuilder formBuilder, StateBinder stateBinder, ResourceMap resourceMap)
    {
        this.formBuilder = formBuilder;
        this.resourceMap = resourceMap;
        this.stateBinder = stateBinder;
    }

    public BindingFormBuilder appendLine(Enum resourceKey, Fields fieldType, Property property, Object... args)
    {
        String resource = getResource(resourceKey, args);

        JLabel label = formBuilder.append(resource);
        formBuilder.nextLine();
        Component component = stateBinder.bind(fieldType.newField(), property);
        formBuilder.append(component);
        label.setLabelFor(component);
        formBuilder.nextLine();

        return this;
    }

    public BindingFormBuilder appendButtonLine(Action buttonAction)
    {
        JButton button = new JButton(buttonAction);
        formBuilder.append(button);
        formBuilder.nextLine();
        return this;
    }

    public BindingFormBuilder appendToggleButtonLine(Action buttonAction)
    {
        JToggleButton button = new JToggleButton(buttonAction);
        formBuilder.append(button);
        formBuilder.nextLine();
        return this;
    }

    public BindingFormBuilder appendSeparator(Enum resourceKey)
    {
        formBuilder.appendSeparator(getResource(resourceKey));
        return this;
    }


    private String getResource(Enum resourceKey, Object... args)
    {
        String key = resourceKey.toString();

        String resource = resourceMap == null ? null : resourceMap.getString(key, args);
        if (resource == null)
        {
            ResourceMap map = Application.getInstance().getContext().getResourceMap(resourceKey.getClass());
            resource = map.getString(key, args);
        }

        if (resource == null)
        {
            resource = "#" + key;
        }
        return resource;
    }
}


