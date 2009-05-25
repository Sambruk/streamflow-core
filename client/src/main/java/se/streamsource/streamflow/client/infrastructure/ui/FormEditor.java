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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Container;

/**
 * JAVADOC
 */
public class FormEditor
{
    Container formContainer;
    boolean editing = false;

    public FormEditor(Container formContainer)
    {
        this.formContainer = formContainer;
        init(formContainer);
        view();
    }

    private void init(Container formContainer)
    {
        Component[] components = formContainer.getComponents();
        for (Component component : components)
        {
            if (component instanceof JPanel)
            {
                init((Container) component);
            } else if (component instanceof JPasswordField)
            {
                setVisible(component, false);
            } else if (component instanceof JTextComponent)
            {
                final JTextComponent text = (JTextComponent) component;
                text.getDocument().addDocumentListener(new DocumentListener()
                {
                    public void insertUpdate(DocumentEvent e)
                    {
                        changedUpdate(e);
                    }

                    public void removeUpdate(DocumentEvent e)
                    {
                        changedUpdate(e);
                    }

                    public void changedUpdate(DocumentEvent e)
                    {
                        if (text.getText().equals(""))
                        {
                            setVisible(text, false);
                        } else
                        {
                            if (!text.isVisible())
                            {
                                setVisible(text, true);
                            }
                        }
                    }
                });
            }
        }
    }

    public boolean isEditing()
    {
        return editing;
    }

    public void view()
    {
        editing = false;
        view(formContainer);
    }

    private void view(Container formContainer)
    {
        Component[] components = formContainer.getComponents();
        for (Component component : components)
        {
            if (component instanceof JPanel)
            {
                view((Container) component);
            } else if (component instanceof JLabel)
            {
                // Do nothing
            } else if (component instanceof JPasswordField)
            {
                setVisible(component, false);
            } else if (component instanceof JComponent)
            {
                final JComponent text = (JComponent) component;
                text.setEnabled(false);
            }
        }

        formContainer.doLayout();
    }

    public void edit()
    {
        editing = true;
        edit(formContainer);
    }

    private void edit(Container formContainer)
    {
        Component[] components = formContainer.getComponents();
        for (Component component : components)
        {
            if (component instanceof JPanel)
            {
                edit((Container) component);
            } else if (component instanceof JLabel)
            {
                // Do nothing
            } else if (component instanceof JPasswordField)
            {
                final JPasswordField passwordField = (JPasswordField) component;
                setVisible(component, true);
            } else if (component instanceof JComponent)
            {
                final JComponent text = (JComponent) component;
                text.setEnabled(true);
            }
        }
    }

    private void setVisible(Component component, boolean b)
    {
        component.setVisible(b);
        JLabel label = (JLabel) ((JComponent) component).getClientProperty("labeledBy");
        if (label != null)
            label.setVisible(b);
    }
}
