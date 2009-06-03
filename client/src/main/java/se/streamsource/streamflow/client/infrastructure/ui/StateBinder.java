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

import org.jdesktop.application.ResourceMap;
import org.jdesktop.swingx.JXErrorPane;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.property.PropertyInstance;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * JAVADOC
 */
public class StateBinder
{
    ResourceBundle errorMessages;
    Map<Class<? extends Component>, Binder> binders = new HashMap<Class<? extends Component>, Binder>();
    Set<Binding> bindings = new HashSet<Binding>();

    public StateBinder()
    {
        Binder defaultBinder = new DefaultBinder(this);
        registerBinder(defaultBinder,
                JTextField.class,
                JTextArea.class,
                JScrollPane.class,
                JPasswordField.class,
                JCheckBox.class);

        errorMessages = ResourceBundle.getBundle(getClass().getName());
    }

    public void setResourceMap(final ResourceMap resourceMap)
    {
        errorMessages = new ResourceBundle()
        {
            protected Object handleGetObject(String key)
            {
                return resourceMap.getString(key);
            }

            public Enumeration<String> getKeys()
            {
                return Collections.enumeration(resourceMap.keySet());
            }
        };
    }

    public void registerBinder(Binder binder, Class<? extends Component>... componentTypes)
    {
        for (Class<? extends Component> componentType : componentTypes)
        {
            binders.put(componentType, binder);
        }
    }

    public <T extends Component> T bind(T component, final Property property)
    {
        Binder binder = binders.get(component.getClass());

        if (binder == null)
            throw new IllegalArgumentException("No binder registered for component type:" + component.getClass().getSimpleName());

        Binding binding;
        if (property instanceof BinderPropertyInstance)
        {
            BinderPropertyInstance binderProperty = (BinderPropertyInstance) property;
            binding = binder.bind(component, binderProperty.accessor());
        } else
        {
            binding = binder.bind(component, property);
        }

        bindings.add(binding);

        return component;

    }

    public <T> T updateWith(T source)
    {
        for (Binding binding : bindings)
        {
            binding.updateWith(source);
        }

        return source;
    }

    public void update()
    {
        for (Binding binding : bindings)
        {
            binding.update();
        }
    }

    public void handleException(Component component, Exception e)
    {
        component.requestFocus();

        if (e instanceof ConstraintViolationException)
        {
            ConstraintViolationException cve = (ConstraintViolationException) e;
            String[] messages = cve.getLocalizedMessages(errorMessages);
            JXErrorPane errorPane = new JXErrorPane();
            String message = "<html>";
            for (String s : messages)
            {
                message += s + "<br/>";
            }
            message += "</html>";
            JLabel messageLabel = new JLabel(message);
            JOptionPane.showMessageDialog(component, messageLabel, errorMessages.getString("property_constraint_violation"), JOptionPane.ERROR_MESSAGE);
/*
            JXDialog dialog = new JXDialog(WindowUtils.findJFrame(component), messageLabel);
            dialog.pack();
            dialog.setLocationRelativeTo(component);
            dialog.setVisible(true);
*/
        } else
        {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
    }

    public <T> T bindingTemplate(Class<T> mixinType)
    {
        return (T) Proxy.newProxyInstance(mixinType.getClassLoader(), new Class[]{mixinType}, new InvocationHandler()
        {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                if (Property.class.isAssignableFrom(method.getReturnType()))
                {
                    PropertyInstance propertyInstance = new BinderPropertyInstance(method, new GenericPropertyInfo(method), null, null);
                    return propertyInstance;
                } else
                    return null;
            }
        });
    }

    public Component[] boundComponents()
    {
        List<Component> components = new ArrayList<Component>();
        for (Binding binding : bindings)
        {
            components.add(binding.component);
        }
        return components.toArray(new Component[components.size()]);
    }

    private static class BinderPropertyInstance
            extends PropertyInstance
    {
        private Method accessor;

        public BinderPropertyInstance(Method accessor, GenericPropertyInfo genericPropertyInfo, Object o, ConstraintsCheck o1)
        {
            super(genericPropertyInfo, o, o1);
            this.accessor = accessor;
        }

        public Method accessor()
        {
            return accessor;
        }
    }

    interface Binder
    {
        Binding bind(Component component, Object property);

        void updateComponent(Component component, Object value);
    }

    class Binding
    {
        private Binder binder;
        private Object source;
        private Component component;
        private Object property;
        private StateBinder stateBinder;

        Binding(Binder binder, Component component, Object property, StateBinder stateBinder)
        {
            this.binder = binder;
            this.component = component;
            this.property = property;
            this.stateBinder = stateBinder;
        }

        void updateWith(Object source)
        {
            this.source = source;

            update();
        }

        void update()
        {
            if (source == null)
                return;

            try
            {
                Object value = property().get();
                binder.updateComponent(component, value);
            } catch (Exception e)
            {
                stateBinder.handleException(component, e);
            }
        }

        void updateProperty(Object newValue)
        {
            Property<Object> objectProperty = property();
            if (objectProperty == null)
                return;

            try
            {
                // TODO Value conversion

                objectProperty.set(newValue);
            } catch (Exception e)
            {
                // Reset value
                binder.updateComponent(component, property().get());

                stateBinder.handleException(component, e);
            }
        }

        private Property<Object> property()
        {
            if (property instanceof Method)
            {
                try
                {
                    return (Property<Object>) ((Method) property).invoke(source);
                } catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                } catch (InvocationTargetException e)
                {
                    e.printStackTrace();
                }
                return null; // Should not be possible...
            } else
            {
                return (Property<Object>) property;
            }
        }
    }

    private class DefaultBinder
            implements Binder
    {
        private StateBinder stateBinder;

        public DefaultBinder(StateBinder stateBinder)
        {
            this.stateBinder = stateBinder;
        }

        public Binding bind(Component component, Object property)
        {
            // TODO Pluggable handlers
            final Binding binding = new Binding(this, component, property, stateBinder);
            if (component instanceof JTextField)
            {
                final JTextField textField = (JTextField) component;
                component.addFocusListener(new FocusAdapter()
                {
                    public void focusLost(FocusEvent e)
                    {
                        binding.updateProperty(textField.getText());
                    }
                });

                return binding;
            } else if (component instanceof JPasswordField)
            {
                final JPasswordField passwordField = (JPasswordField) component;
                component.addFocusListener(new FocusAdapter()
                {
                    public void focusLost(FocusEvent e)
                    {
                        binding.updateProperty(new String(passwordField.getPassword()));
                    }
                });

                return binding;
            } else if (component instanceof JTextArea)
            {
                final JTextArea textArea = (JTextArea) component;
                component.addFocusListener(new FocusAdapter()
                {
                    public void focusLost(FocusEvent e)
                    {
                        binding.updateProperty(new String(textArea.getText()));
                    }
                });

                return binding;
            } else if (component instanceof JScrollPane)
            {
                JScrollPane pane = (JScrollPane) component;
                return bind(pane.getViewport().getView(), property);
            } else if (component instanceof JCheckBox)
            {
                final JCheckBox checkBox = (JCheckBox) component;
                checkBox.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        binding.updateProperty(checkBox.isSelected());
                    }
                });
                return binding;
            }

            throw new IllegalArgumentException("Could not bind to component of type " + component.getClass().getName());
        }

        public void updateComponent(Component component, Object value)
        {
            if (component instanceof JTextComponent)
            {
                JTextComponent textField = (JTextComponent) component;
                String text = value.toString();
                textField.setText(text);

            } else if (component instanceof JPasswordField)
            {
                JPasswordField passwordField = (JPasswordField) component;
                passwordField.setText(value.toString());
            } else if (component instanceof JCheckBox)
            {
                JCheckBox checkBox = (JCheckBox) component;
                checkBox.setSelected(((Boolean) value));
            }
        }
    }
}
