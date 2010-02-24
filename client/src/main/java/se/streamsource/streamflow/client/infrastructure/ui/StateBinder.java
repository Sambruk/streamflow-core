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
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.property.PropertyInstance;
import se.streamsource.streamflow.client.ui.task.MultiSelectPanel;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * JAVADOC
 */
public class StateBinder
      extends Observable
{
   ResourceBundle errorMessages;
   Map<Class<? extends Component>, Binder> binders = new HashMap<Class<? extends Component>, Binder>();
   Set<Binding> bindings = new HashSet<Binding>();

   Set<Converter> converters = new HashSet<Converter>();

   public StateBinder()
   {
      Binder defaultBinder = new DefaultBinder( this );
      registerBinder( defaultBinder,
            JLabel.class,
            JTextField.class,
            JFormattedTextField.class,
            JTextArea.class,
            JScrollPane.class,
            JPasswordField.class,
            JCheckBox.class,
            JXDatePicker.class,
            JComboBox.class,
            MultiSelectPanel.class );

      errorMessages = ResourceBundle.getBundle( getClass().getName() );
   }

   public void setResourceMap( final ResourceMap resourceMap )
   {
      errorMessages = new ResourceBundle()
      {
         protected Object handleGetObject( String key )
         {
            return resourceMap.getString( key );
         }

         public Enumeration<String> getKeys()
         {
            return Collections.enumeration( resourceMap.keySet() );
         }
      };
   }

   public void addConverter( Converter converter )
   {
      converters.add( converter );
   }

   public void registerBinder( Binder binder, Class<? extends Component>... componentTypes )
   {
      for (Class<? extends Component> componentType : componentTypes)
      {
         binders.put( componentType, binder );
      }
   }

   public <T extends Component> T bind( T component, final Property property )
   {
      Binder binder = binders.get( component.getClass() );

      if (binder == null)
         throw new IllegalArgumentException( "No binder registered for component type:" + component.getClass().getSimpleName() );

      Binding binding;
      if (property instanceof BinderPropertyInstance)
      {
         BinderPropertyInstance binderProperty = (BinderPropertyInstance) property;
         binding = binder.bind( component, binderProperty.accessor() );
      } else
      {
         binding = binder.bind( component, property );
      }

      bindings.add( binding );

      return component;

   }

   public <T> T updateWith( T source )
   {
      for (Binding binding : bindings)
      {
         binding.updateWith( source );
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

   public void handleException( Component component, Exception e )
   {
      component.requestFocus();

      if (e instanceof ConstraintViolationException)
      {
         ConstraintViolationException cve = (ConstraintViolationException) e;
         String[] messages = cve.getLocalizedMessages( errorMessages );
         JXErrorPane errorPane = new JXErrorPane();
         StringBuilder message = new StringBuilder( "<html>" );
         for (String s : messages)
         {
            message.append( s + "<br/>" );
         }
         message.append( "</html>" );
         JLabel messageLabel = new JLabel( message.toString() );
         JOptionPane.showMessageDialog( component, messageLabel, errorMessages.getString( "property_constraint_violation" ), JOptionPane.ERROR_MESSAGE );
/*
            JXDialog dialog = new JXDialog(WindowUtils.findJFrame(component), messageLabel);
            dialog.pack();
            dialog.setLocationRelativeTo(component);
            dialog.setVisible(true);
*/
      } else
      {
         Thread.currentThread().getUncaughtExceptionHandler().uncaughtException( Thread.currentThread(), e );
      }
   }

   public <T> T bindingTemplate( Class<T> mixinType )
   {
      return (T) Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{mixinType}, new InvocationHandler()
      {
         public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
         {
            if (Property.class.isAssignableFrom( method.getReturnType() ))
            {
               PropertyInstance propertyInstance = new BinderPropertyInstance( method, new GenericPropertyInfo( method ), null, null );
               return propertyInstance;
            } else
               return null;
         }
      } );
   }

   public Component[] boundComponents()
   {
      List<Component> components = new ArrayList<Component>();
      for (Binding binding : bindings)
      {
         components.add( binding.component );
      }
      return components.toArray( new Component[components.size()] );
   }

   private static class BinderPropertyInstance
         extends PropertyInstance
   {
      private Method accessor;

      public BinderPropertyInstance( Method accessor, GenericPropertyInfo genericPropertyInfo, Object o, ConstraintsCheck o1 )
      {
         super( genericPropertyInfo, o, o1 );
         this.accessor = accessor;
      }

      public Method accessor()
      {
         return accessor;
      }
   }

   interface Binder
   {
      Binding bind( Component component, Object property );

      void updateComponent( Component component, Object value );
   }

   public class Binding
   {
      private Binder binder;
      private Object source;
      private Component component;
      private Object property;
      private StateBinder stateBinder;

      Binding( Binder binder, Component component, Object property, StateBinder stateBinder )
      {
         this.binder = binder;
         this.component = component;
         this.property = property;
         this.stateBinder = stateBinder;
      }

      void updateWith( Object source )
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
            Object newValue = property().get();

            if (newValue != null)
            {
               // Try all converters
               Object convertedValue;
               for (Converter converter : converters)
               {
                  convertedValue = converter.toComponent( newValue );
                  if (convertedValue != newValue)
                  {
                     newValue = convertedValue;
                     break;
                  }
               }
            }

            binder.updateComponent( component, newValue );
         } catch (Exception e)
         {
            stateBinder.handleException( component, e );
         }
      }

      void updateProperty( Object newValue )
            throws IllegalArgumentException
      {
         Property<Object> objectProperty = property();
         if (objectProperty == null)
            return;

         if (newValue != null)
         {
            // Try all converters
            Object convertedValue;
            for (Converter converter : converters)
            {
               convertedValue = converter.fromComponent( newValue );
               if (convertedValue != newValue)
               {
                  newValue = convertedValue;
                  break;
               }
            }
         }

         if (objectProperty.get() == null && newValue == null)
            return;
         if (objectProperty.get() != null && objectProperty.get().equals( newValue ))
            return; // Do nothing

         objectProperty.set( newValue );
         setChanged();
         notifyObservers( objectProperty );
      }

      private Property<Object> property()
      {
         if (property instanceof Method)
         {
            try
            {
               return (Property<Object>) ((Method) property).invoke( source );
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

      public DefaultBinder( StateBinder stateBinder )
      {
         this.stateBinder = stateBinder;
      }

      public Binding bind( Component component, Object property )
      {
         final Binding binding = new Binding( this, component, property, stateBinder );

         if (component instanceof JPasswordField)
         {
            final JPasswordField passwordField = (JPasswordField) component;
            passwordField.setInputVerifier( new PropertyInputVerifier( binding ) );

            passwordField.addActionListener( new ActionListener()
            {
               public void actionPerformed( ActionEvent e )
               {
                  KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent( passwordField );
               }
            } );

            return binding;
         } else if (component instanceof JTextField)
         {
            final JTextField textField = (JTextField) component;

            textField.setInputVerifier( new PropertyInputVerifier( binding ) );

            textField.addActionListener( new ActionListener()
            {
               public void actionPerformed( ActionEvent e )
               {
                  KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent( textField );
               }
            } );

            return binding;
         } else if (component instanceof JTextArea)
         {
            final JTextArea textArea = (JTextArea) component;

            textArea.setInputVerifier( new PropertyInputVerifier( binding ) );

            return binding;
         } else if (component instanceof JScrollPane)
         {
            JScrollPane pane = (JScrollPane) component;
            return bind( pane.getViewport().getView(), property );
         } else if (component instanceof JCheckBox)
         {
            final JCheckBox checkBox = (JCheckBox) component;
            checkBox.addActionListener( new ActionListener()
            {
               public void actionPerformed( ActionEvent e )
               {
                  binding.updateProperty( checkBox.isSelected() );
               }
            } );
            return binding;
         } else if (component instanceof JLabel)
         {
            // Do nothing
            return binding;
         } else if (component instanceof JXDatePicker)
         {
            final JXDatePicker datePicker = (JXDatePicker) component;
            datePicker.setInputVerifier( new PropertyInputVerifier( binding ) );

            datePicker.addPropertyChangeListener( new PropertyChangeListener()
            {
               public void propertyChange( PropertyChangeEvent e )
               {
                  if ("date".equals( e.getPropertyName() ))
                  {
                     binding.updateProperty( e.getNewValue() );
                  }
               }
            } );

            return binding;
         } else if (component instanceof JComboBox)
         {
            final JComboBox comboBox = (JComboBox) component;

            comboBox.addActionListener( new ActionListener() {

               public void actionPerformed( ActionEvent actionEvent )
               {
                  binding.updateProperty( ((JComboBox)actionEvent.getSource()).getSelectedItem() );
               }
            });
            return binding;
         } else if (component instanceof MultiSelectPanel)
         {
            final MultiSelectPanel multi = (MultiSelectPanel) component;
            multi.addActionPerformedListener( new ActionListener() {

               public void actionPerformed( ActionEvent actionEvent )
               {
                  binding.updateProperty( multi.getChecked() );
               }
            });

            return binding;
         }


         throw new IllegalArgumentException( "Could not bind to component of type " + component.getClass().getName() );
      }

      public void updateComponent( Component component, Object value )
      {
         if (component instanceof JPasswordField)
         {
            JPasswordField passwordField = (JPasswordField) component;
            passwordField.setText( value == null ? "" : value.toString() );
         } else if (component instanceof JTextComponent)
         {
            JTextComponent textField = (JTextComponent) component;
            String text = value == null ? "" : value.toString();
            textField.setText( text );
            textField.setCaretPosition( 0 );

         } else if (component instanceof JCheckBox)
         {
            JCheckBox checkBox = (JCheckBox) component;
            checkBox.setSelected( ((Boolean) value) );
         } else if (component instanceof JLabel)
         {
            JLabel label = (JLabel) component;
            label.setText( (String) value == null ? "" : value.toString() );
         } else if (component instanceof JXDatePicker)
         {
            JXDatePicker datePicker = (JXDatePicker) component;

            if ( value instanceof String)
            {
               datePicker.setDate( DateFunctions.fromString( (String) value ));
            } else
            {
               datePicker.setDate( (Date) value );
            }
         } else if (component instanceof JComboBox)
         {
            JComboBox box = (JComboBox) component;
            box.setSelectedItem( value );
         } else if (component instanceof MultiSelectPanel)
         {
            MultiSelectPanel multi = (MultiSelectPanel) component;
            multi.setChecked( (String) value );
         }
      }
   }

   class PropertyInputVerifier
         extends InputVerifier
   {
      private Binding binding;

      PropertyInputVerifier( Binding binding )
      {
         this.binding = binding;
      }

      IllegalArgumentException exception;

      public boolean verify( JComponent input )
      {
         try
         {
            Object value = null;
            if (input instanceof JTextComponent)
            {
               value = ((JTextComponent) input).getText();
            } else if (input instanceof JXDatePicker)
            {
               value = ((JXDatePicker) input).getDate();
            }
            binding.updateProperty( value );
            return true;
         } catch (IllegalArgumentException e)
         {
            exception = e;
            return false;
         }
      }

      @Override
      public boolean shouldYieldFocus( JComponent input )
      {
         boolean result = super.shouldYieldFocus( input );

         if (!result)
         {
            Window window = WindowUtils.findWindow( input );
            StringBuilder message = new StringBuilder( i18n.text( AdministrationResources.invalid_value ) );

            if (exception instanceof ConstraintViolationException)
            {
               ConstraintViolationException ex = (ConstraintViolationException) exception;
               String[] messages = ex.getLocalizedMessages( errorMessages );
               message = new StringBuilder( "<html>" );
               for (String s : messages)
               {
                  message.append( "<p>" + s + "</p>" );
               }
               message.append( "</html>" );
            }

            JLabel main = new JLabel( message.toString() );

            JXDialog dialog;
            if (window instanceof Frame)
               dialog = new JXDialog( (Frame) window, main );
            else
               dialog = new JXDialog( (Dialog) window, main );

            dialog.setModal( true );

            dialog.pack();
            dialog.setLocationRelativeTo( SwingUtilities.windowForComponent( input ) );
            dialog.setVisible( true );
         }

         return result;
      }
   }

   public interface Converter
   {
      Object toComponent( Object value );

      Object fromComponent( Object value );
   }
}
