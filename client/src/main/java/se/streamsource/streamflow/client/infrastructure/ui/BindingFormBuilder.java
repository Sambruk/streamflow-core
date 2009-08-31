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
import org.jdesktop.swingx.JXDatePicker;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * JAVADOC
 */
public class BindingFormBuilder
{
    // Field types
    public enum Fields
    {
        LABEL
                {
                    public Component newField()
                    {
                        return new JLabel();
                    }
                },
        TEXTFIELD
                {
                    public Component newField()
                    {
                        return new JTextField(30);
                    }
                },
        FORMATTEDTEXTFIELD
                {
                    public Component newField()
                    {
                        return new JFormattedTextField(30);
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
                },
        DATEPICKER
                {
                    public Component newField()
                    {
                        return new JXDatePicker();
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
        return appendLine(resourceKey, fieldType.newField(), property, stateBinder, args);
    }

    public BindingFormBuilder appendLine(Enum resourceKey, Component component, Property property, Object... args)
    {
        return appendLine(resourceKey, component, property, stateBinder, args);
    }

    public BindingFormBuilder appendLine(Enum resourceKey, Fields fieldType, Property property, StateBinder stateBinderIn, Object... args)
    {
        return appendLine(resourceKey, fieldType.newField(), property, stateBinderIn, args);
    }

    public BindingFormBuilder appendLine(Enum resourceKey, Component component, Property property, StateBinder stateBinderIn, Object... args)
    {
        String resource = getResource(resourceKey, args);

        JLabel label = formBuilder.append(resource);
        formBuilder.nextLine();
        stateBinderIn.bind(component, property);
        formBuilder.append(component);
        label.setLabelFor(component);
        formBuilder.nextLine();

        if(component instanceof JXDatePicker)
        {
            // Limit pickable dates to future 
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            ((JXDatePicker)component).getMonthView().setLowerBound(calendar.getTime());
            // Set date format
            ((JXDatePicker)component).setFormats(new SimpleDateFormat(getResource(WorkspaceResources.date_format)));
        }
        
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

    public BindingFormBuilder appendLine(Component component)
    {
        formBuilder.append(component);
        formBuilder.nextLine();
        return this;
    }

    public BindingFormBuilder appendSeparator(Enum resourceKey)
    {
        formBuilder.appendSeparator(getResource(resourceKey));
        return this;
    }

    public BindingFormBuilder appendFormattedTextField(Enum resourceKey, Enum resourceKeyPattern, Property property, StateBinder stateBinderIn, Object... args)
    {
        String resource = getResource(resourceKey, args);
        String pattern = getResource(resourceKeyPattern, args);

        JLabel label = formBuilder.append(resource);
        formBuilder.nextLine();
        JFormattedTextField component = new JFormattedTextField(new RegexPatternFormatter(pattern));
        stateBinderIn.bind(component, property);
        formBuilder.append(component);
        label.setLabelFor(component);
        formBuilder.nextLine();

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


