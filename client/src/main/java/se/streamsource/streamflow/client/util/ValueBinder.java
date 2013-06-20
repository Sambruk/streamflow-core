/**
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.util;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXLabel;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.util.ListMap;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.workspace.cases.general.RemovableLabel;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.*;

/**
 * Bind components to value names to allow them to be updated from a given source
 */
public class ValueBinder
{
   @Structure
   Qi4jSPI spi;

   Map<Class<? extends Component>, Binder> binders = new HashMap<Class<? extends Component>, Binder>();
   ListMap<String, Binding> bindings = new ListMap<String, Binding>();

   public ValueBinder()
   {
      Binder defaultBinder = new DefaultBinder();
      registerBinder( defaultBinder,
            JXLabel.class,
            JLabel.class,
            JTextField.class,
            JTextArea.class,
            JTextPane.class,
            JEditorPane.class,
            JScrollPane.class,
            JPasswordField.class,
            JCheckBox.class,
            JXDatePicker.class,
            JComboBox.class,
            JRadioButton.class,
            RemovableLabel.class,
            StreamflowJXColorSelectionButton.class);
   }

   public void registerBinder( Binder binder, Class<? extends Component>... componentTypes )
   {
      for (Class<? extends Component> componentType : componentTypes)
      {
         binders.put( componentType, binder );
      }
   }

   public <T extends Component> T bind( String name, T component )
   {
      return bind(name, component, null);
   }

   public <T extends Component> T bind( String name, T component, Converter converter)
   {
      Component boundComponent = component;
      if (boundComponent instanceof JScrollPane)
      {
         boundComponent = ((JScrollPane)boundComponent).getViewport().getView();
      }

      Binder binder = binders.get( boundComponent.getClass() );

      if (binder == null)
         throw new IllegalArgumentException( "No binder registered for component type:" + boundComponent.getClass().getSimpleName() );

      bindings.add(name, new Binding(converter, boundComponent, binder));

      return component;

   }

   public void update( ValueComposite source )
   {
      spi.getState( source ).visitProperties( new StateHolder.StateVisitor<RuntimeException>()
      {
         public void visitProperty( QualifiedName name, Object value ) throws RuntimeException
         {
            Iterable<Binding> binding = bindings.get( name.name() );
            if (binding != null)
               for (Binding binding1 : binding)
               {
                  binding1.update( value );
               }
         }
      } );
   }

   public void update(Form form)
   {
      for (Parameter parameter : form)
      {
         List<Binding> bindings1 = bindings.get(parameter.getName());
         if (bindings1 != null)
            for (Binding binding : bindings1)
            {
               binding.update(parameter.getValue());
            }
      }
   }

   public void update( String name, Object value )
   {
      List<Binding> bindings1 = bindings.get(name);
      if (bindings1 != null)
         for (Binding binding : bindings1)
         {
            binding.update( value );
         }
   }

   public class Binding
   {
      Converter converter;
      Component component;
      Binder binder;

      public Binding( Converter converter, Component component, Binder binder )
      {
         this.converter = converter;
         this.component = component;
         this.binder = binder;
      }

      void update(Object value)
      {
         if (converter != null)
            value = converter.toComponent( value );

         binder.updateComponent( component, value );
      }
   }

   public interface Binder
   {
      void updateComponent( Component component, Object value );
   }

   private class DefaultBinder
         implements Binder
   {
      public DefaultBinder()
      {
      }

      public void updateComponent( Component component, Object value )
      {

         if (component instanceof JLabel)
         {
            JLabel label = (JLabel) component;
            label.setText( value == null ? "" : value.toString() );
         } else if (component instanceof JPasswordField)
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

            if (value instanceof String)
            {
               value = Boolean.parseBoolean(value.toString());
            }

            checkBox.setSelected( ((Boolean) value) );
         } else if (component instanceof JLabel)
         {
            JLabel label = (JLabel) component;
            label.setText( value == null ? "" : value.toString() );
         } else if (component instanceof JXDatePicker)
         {
            JXDatePicker datePicker = (JXDatePicker) component;

            if (value instanceof String)
            {
               if (!((String) value).isEmpty())
               {
                  datePicker.setDate( DateFunctions.fromString( (String) value ) );
               }
            } else
            {
               datePicker.setDate( (Date) value );
            }
         }else if (component instanceof StreamflowJXColorSelectionButton)
         {
            StreamflowJXColorSelectionButton colorSelectionButton = (StreamflowJXColorSelectionButton) component;

            if (value instanceof String)
            {
               if (!((String) value).isEmpty())
               {
                  colorSelectionButton.setBackground( new Color( parseInt( (String) value ) ) );
               }
            }

         } else if (component instanceof JComboBox)
         {
            JComboBox box = (JComboBox) component;
            box.setSelectedItem( value );
         } else if (component instanceof JRadioButton)
         {
            JRadioButton button = (JRadioButton) component;
            button.setSelected( button.getActionCommand().equals( value.toString() ) );
         } else if (component instanceof RemovableLabel)
         {
            RemovableLabel label = (RemovableLabel) component;
            label.setRemoveLink((LinkValue) value);
         }
      }
   }

   public interface Converter<FROM,TO>
   {
      TO toComponent( FROM value );
   }
}
