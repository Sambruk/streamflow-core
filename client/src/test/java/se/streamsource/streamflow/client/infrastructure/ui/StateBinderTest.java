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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import se.streamsource.streamflow.application.shared.inbox.NewSharedTaskCommand;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

public class StateBinderTest
        extends AbstractQi4jTest
{

    private StateBinder binder;
    private ValueBuilder<NewSharedTaskCommand> commandBuilder;

    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addValues(NewSharedTaskCommand.class);
    }


    @Test
    public void testTextFieldEnter() throws IllegalAccessException, NoSuchFieldException
    {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("200dlu",""), new JPanel());
        builder.setDefaultDialogBorder();

        binder = new StateBinder();
        NewSharedTaskCommand template = binder.bindingTemplate(NewSharedTaskCommand.class);

        JTextField field = (JTextField) TEXTFIELD.newField();
        binder.bind(field, template.description());

        commandBuilder = valueBuilderFactory.newValueBuilder(NewSharedTaskCommand.class);

        binder.updateWith(commandBuilder.prototype());

        String descriptionText = "Task description";
        field.setText(descriptionText);

        simulateKey(new KeyEvent(field, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),0,KeyEvent.VK_ENTER, '\n'), field);

        NewSharedTaskCommand task = commandBuilder.newInstance();

        Assert.assertThat(task.description().get(), CoreMatchers.equalTo(descriptionText));
    }

    @Test
    public void testTextField() throws IllegalAccessException, NoSuchFieldException
    {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("200dlu",""), new JPanel());
        builder.setDefaultDialogBorder();

        binder = new StateBinder();
        NewSharedTaskCommand template = binder.bindingTemplate(NewSharedTaskCommand.class);

        JTextField field = (JTextField) TEXTFIELD.newField();
        binder.bind(field, template.description());

        commandBuilder = valueBuilderFactory.newValueBuilder(NewSharedTaskCommand.class);

        binder.updateWith(commandBuilder.prototype());

        String descriptionText = "Task description";
        field.setText(descriptionText);
        field.dispatchEvent(new FocusEvent(field, FocusEvent.FOCUS_FIRST));
        field.dispatchEvent(new FocusEvent(field, FocusEvent.FOCUS_LAST));

        NewSharedTaskCommand task = commandBuilder.newInstance();

        Assert.assertThat(task.description().get(), CoreMatchers.equalTo(descriptionText));
    }


    private void simulateKey(KeyEvent e, Component c) throws NoSuchFieldException, IllegalAccessException
    {
        Field f = AWTEvent.class.getDeclaredField("focusManagerIsDispatching");
        f.setAccessible(true);
        f.set(e, Boolean.TRUE);
        c.dispatchEvent(e);
    }

    @Test
    public void testPassword()
    {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("200dlu",""), new JPanel());
        builder.setDefaultDialogBorder();

        binder = new StateBinder();
        NewSharedTaskCommand template = binder.bindingTemplate(NewSharedTaskCommand.class);

        JTextField field = (JTextField) PASSWORD.newField();

        binder.bind(field, template.description());

        commandBuilder = valueBuilderFactory.newValueBuilder(NewSharedTaskCommand.class);

        binder.updateWith(commandBuilder.prototype());

        String descriptionText = "Task description";
        field.setText(descriptionText);
        field.dispatchEvent(new FocusEvent(field, FocusEvent.FOCUS_FIRST));
        field.dispatchEvent(new FocusEvent(field, FocusEvent.FOCUS_LAST));

        NewSharedTaskCommand task = commandBuilder.newInstance();

        Assert.assertThat(task.description().get(), CoreMatchers.equalTo(descriptionText));
    }

    @Test
    public void testTextArea()
    {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("200dlu",""), new JPanel());
        builder.setDefaultDialogBorder();

        binder = new StateBinder();
        NewSharedTaskCommand template = binder.bindingTemplate(NewSharedTaskCommand.class);

        JScrollPane pane = (JScrollPane) TEXTAREA.newField();
        JTextArea textArea = (JTextArea) pane.getViewport().getView();

        binder.bind(pane, template.description());

        commandBuilder = valueBuilderFactory.newValueBuilder(NewSharedTaskCommand.class);

        binder.updateWith(commandBuilder.prototype());

        String descriptionText = "Task description";
        textArea.setText(descriptionText);
        textArea.dispatchEvent(new FocusEvent(textArea, FocusEvent.FOCUS_FIRST));
        textArea.dispatchEvent(new FocusEvent(textArea, FocusEvent.FOCUS_LAST));

        NewSharedTaskCommand task = commandBuilder.newInstance();

        Assert.assertThat(task.description().get(), CoreMatchers.equalTo(descriptionText));
    }
}
