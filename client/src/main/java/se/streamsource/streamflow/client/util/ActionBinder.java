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

import org.jdesktop.application.ResourceMap;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.RemovableLabel;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Bind components to actions, which are invoked when the components state is updated
 */
public class ActionBinder
{
   ResourceBundle errorMessages;
   List<Binder> binders = new ArrayList<Binder>();

   private final ActionMap actionMap;

   public ActionBinder( @Uses ActionMap actionMap )
   {
      this.actionMap = actionMap;

      Binder defaultBinder = new DefaultBinder( );
      registerBinder( defaultBinder);

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

   public void registerBinder( Binder binder )
   {
      binders.add( binder );
   }

   public <T extends Component> T bind( String name, T component )
   {
      Component boundComponent = component;
      if (boundComponent instanceof JScrollPane)
      {
         boundComponent = ((JScrollPane)boundComponent).getViewport().getView();
      }

      Action action = actionMap.get( name );
      if (action == null)
         throw new IllegalArgumentException( "No action named '" + name + "' in action map" );

      for (Binder binder : binders)
      {
         if (binder.bind( boundComponent, action ))
         {
            return component;
         }
      }

      throw new IllegalArgumentException( "No binder registered for component type:" + component.getClass().getSimpleName() );
   }

   public interface Binder
   {
      boolean bind( Component component, Action action );
   }

   private class DefaultBinder
         implements Binder
   {
      public DefaultBinder()
      {
      }

      public boolean bind( Component component, final Action action )
      {
         if (component instanceof JPasswordField)
         {
            final JPasswordField passwordField = (JPasswordField) component;
            passwordField.setInputVerifier( new ActionInputVerifier( action ) );
         } else if (component instanceof JTextField)
         {
            final JTextField textField = (JTextField) component;

            textField.setInputVerifier( new ActionInputVerifier( action ) );
         } else if (component instanceof JTextComponent)
         {
            final JTextComponent textComponent = (JTextComponent) component;

            textComponent.setInputVerifier( new ActionInputVerifier( action ) );
         } else if (component instanceof AbstractButton)
         {
            final AbstractButton button = (AbstractButton) component;
            if (component instanceof StreamflowJXColorSelectionButton)
            {

               button.addPropertyChangeListener( "background", new PropertyChangeListener()
               {
                  public void propertyChange( PropertyChangeEvent e )
                  {
                     action.actionPerformed( new ActionEvent(button, ActionEvent.ACTION_PERFORMED, action.getValue( Action.NAME ).toString()) );
                  }
               } );

            } else
            {
               button.addActionListener( action );
            }
         } else if (component instanceof JXDatePicker)
         {
            final JXDatePicker datePicker = (JXDatePicker) component;
            datePicker.addPropertyChangeListener( new PropertyChangeListener()
            {
               public void propertyChange( PropertyChangeEvent e )
               {
                  if ("date".equals( e.getPropertyName() ))
                  {
                     action.actionPerformed( new ActionEvent(datePicker, ActionEvent.ACTION_PERFORMED, action.getValue( Action.NAME ).toString()) );
                  }
               }
            } );

         }else if (component instanceof JComboBox)
         {
            final JComboBox comboBox = (JComboBox) component;

            comboBox.addActionListener( action );
         } else if (component instanceof RemovableLabel)
         {
            final RemovableLabel removableLabel = (RemovableLabel) component;
            removableLabel.getButton().addActionListener( action );
         } else
            return false;

         return true;
      }
   }

   class ActionInputVerifier
         extends InputVerifier
   {
      private final Action action;

      ActionInputVerifier( Action action )
      {
         this.action = action;
      }

      Exception exception;

      public boolean verify( JComponent input )
      {
         try
         {
            action.actionPerformed( new ActionEvent(input, ActionEvent.ACTION_PERFORMED, action.getValue( Action.NAME ).toString()) );
            return true;
         } catch (Exception e)
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
                  message.append( "<p>" ).append( s ).append( "</p>" );
               }
               message.append( "</html>" );
            } else
            {
               message.append( exception.getMessage() );
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
}
