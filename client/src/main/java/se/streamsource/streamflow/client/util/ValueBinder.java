/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.jdesktop.swingx.*;
import org.qi4j.api.common.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.property.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.qi4j.spi.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.ui.workspace.cases.general.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;

/**
 * Bind components to value names to allow them to be updated from a given source
 */
public class ValueBinder
{
   @Structure
   Qi4jSPI spi;

   Map<Class<? extends Component>, Binder> binders = new HashMap<Class<? extends Component>, Binder>();
   Map<String, Binding> bindings = new HashMap<String, Binding>();

   public ValueBinder()
   {
      Binder defaultBinder = new DefaultBinder();
      registerBinder( defaultBinder,
            JXLabel.class,
            JLabel.class,
            JTextField.class,
            JTextArea.class,
            JScrollPane.class,
            JPasswordField.class,
            JCheckBox.class,
            JXDatePicker.class,
            JComboBox.class,
            RemovableLabel.class);
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

      bindings.put( name, new Binding(converter, boundComponent, binder) );

      return component;

   }

   public void update( ValueComposite source )
   {
      spi.getState( source ).visitProperties( new StateHolder.StateVisitor<RuntimeException>()
      {
         public void visitProperty( QualifiedName name, Object value ) throws RuntimeException
         {
            Binding binding = bindings.get( name.name() );
            if (binding != null)
            {
               binding.update( value );
            }
         }
      } );
   }

   public void update( String name, Object value )
   {
      Binding binding = bindings.get( name );
      if (binding != null)
      {
         binding.update( value );
      } else
         throw new IllegalArgumentException( "No binding named '" + name + "'" );
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
         } else if (component instanceof JComboBox)
         {
            JComboBox box = (JComboBox) component;
            box.setSelectedItem( value );
         } else if (component instanceof RemovableLabel)
         {
            RemovableLabel label = (RemovableLabel) component;
            label.setLinkValue( (LinkValue) value);
         }
      }
   }

   public interface Converter<FROM,TO>
   {
      TO toComponent( FROM value );
   }
}
