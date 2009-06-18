/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

public class StateBinderTest
        extends AbstractQi4jTest
{

    private String descriptionText = "Task description";

    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addValues(NewTestCommand.class);
    }

    @Test
    public void testTextFieldEnter() throws IllegalAccessException, NoSuchFieldException
    {
        JTextField field = (JTextField) TEXTFIELD.newField();
        ValueBuilder<NewTestCommand>  builder = bind(field);
        field.setText(descriptionText);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(
                field,
                new KeyEvent(field, KeyEvent.KEY_PRESSED, 0,0,KeyEvent.VK_ENTER, '\n'));

        Assert.assertThat(builder.newInstance().text().get(),
                          CoreMatchers.equalTo(descriptionText));
    }

    @Test
    public void testTextField() throws IllegalAccessException, NoSuchFieldException
    {
        JTextField field = (JTextField) TEXTFIELD.newField();
        ValueBuilder<NewTestCommand> builder = bind(field);

        field.setText(descriptionText);
        field.dispatchEvent(new FocusEvent(field, FocusEvent.FOCUS_FIRST));
        field.dispatchEvent(new FocusEvent(field, FocusEvent.FOCUS_LAST));

        Assert.assertThat(builder.newInstance().text().get(),
                          CoreMatchers.equalTo(descriptionText));
    }

    @Test
    public void testPassword()
    {
        JTextField field = (JTextField) PASSWORD.newField();
        ValueBuilder<NewTestCommand> builder = bind(field);

        field.setText(descriptionText);
        field.dispatchEvent(new FocusEvent(field, FocusEvent.FOCUS_FIRST));
        field.dispatchEvent(new FocusEvent(field, FocusEvent.FOCUS_LAST));

        Assert.assertThat(builder.newInstance().text().get(),
                          CoreMatchers.equalTo(descriptionText));
    }

    @Test
    public void testPasswordEnter() throws IllegalAccessException, NoSuchFieldException
    {
        JTextField field = (JTextField) PASSWORD.newField();
        ValueBuilder<NewTestCommand> builder = bind(field);
        field.setText(descriptionText);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(
                field,
                new KeyEvent(field, KeyEvent.KEY_PRESSED, 0,0,KeyEvent.VK_ENTER, '\n'));

        Assert.assertThat(builder.newInstance().text().get(),
                          CoreMatchers.equalTo(descriptionText));
    }


    @Test
    public void testTextArea()
    {
        JScrollPane pane = (JScrollPane) TEXTAREA.newField();
        JTextArea textArea = (JTextArea) pane.getViewport().getView();
        ValueBuilder<NewTestCommand> builder = bind(textArea);
        textArea.setText(descriptionText);
        textArea.dispatchEvent(new FocusEvent(textArea, FocusEvent.FOCUS_FIRST));
        textArea.dispatchEvent(new FocusEvent(textArea, FocusEvent.FOCUS_LAST));

        Assert.assertThat(builder.newInstance().text().get(),
                          CoreMatchers.equalTo(descriptionText));
    }


    @Test
    public void testCheckbox()
    {
        /*
        JCheckBox checkbox = (JCheckBox) CHECKBOX.newField();
        ValueBuilder<NewTestCommand> builder = bind(checkbox);

        boolean newValue = !checkbox.isSelected();
        checkbox.setSelected(newValue);
        checkbox.dispatchEvent(new ActionEvent(checkbox, ActionEvent.ACTION_PERFORMED, ""));


        Assert.assertThat(builder.newInstance().isChecked().get(),
                          CoreMatchers.equalTo(newValue));
      */
    }


    private ValueBuilder<NewTestCommand> bind(Component component) {
        StateBinder binder = new StateBinder();
        NewTestCommand template = binder.bindingTemplate(NewTestCommand.class);

        if (component instanceof JCheckBox)
        {
            binder.bind(component, template.isChecked());
        } else
        {
            binder.bind(component, template.text());
        }

        ValueBuilder<NewTestCommand> commandBuilder = valueBuilderFactory.newValueBuilder(NewTestCommand.class);

        binder.updateWith(commandBuilder.prototype());

        return commandBuilder;
    }
}
