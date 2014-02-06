/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultEditorKit.PasteAction;
import javax.swing.text.JTextComponent;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.administration.form.KnownDatatypeDefinitionUrls;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionPluginDTO;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.util.RegexPatternFormatter;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.SuggestTextField;
import se.streamsource.streamflow.client.util.TextTransferHandler;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.util.Strings;

public class TextFieldPanel extends AbstractFieldPanel
{
   private JTextField textField;
   private TextFieldValue fieldValue;
   private StreamflowButton openMapButton;
   private StreamflowButton pasteMapCoordinatesButton;

   @Service
   DialogService dialogs;

   private FormSubmissionWizardPageModel model;

   public TextFieldPanel(@Service ApplicationContext appContext, @Uses FieldSubmissionDTO field,
         @Uses TextFieldValue fieldValue, @Uses FormSubmissionWizardPageModel model)
   {
      super( field );
      this.model = model;
      setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );
      this.fieldValue = fieldValue;
      
      
      if (KnownDatatypeDefinitionUrls.STREET_ADDRESS.equals( field.field().get().datatypeUrl().get() )
            && model.getFormDraftModel().isStreetLookupEnabled())
      {
         FormStreetAddressSuggestModel suggestModel = new FormStreetAddressSuggestModel();
         suggestModel.setFormDraftModel( model.getFormDraftModel() );
         SuggestTextField<StreetSearchDTO> suggestTextField = new SuggestTextField<StreetSearchDTO>( suggestModel );
         textField = suggestTextField.getTextField();
         add( suggestTextField );
      } else {
         textField = new JTextField();
         add( textField );
      }
      textField.setColumns( fieldValue.width().get() );
      
      setActionMap( appContext.getActionMap( this ) );
      ActionMap am = getActionMap();

      if (KnownDatatypeDefinitionUrls.GEO_LOCATION.equals( field.field().get().datatypeUrl().get() )
            && field instanceof FieldSubmissionPluginDTO && ((FieldSubmissionPluginDTO) field).plugin().get() != null)
      {

         TextTransferHandler th = new TextTransferHandler();
         textField.setTransferHandler( th );

         javax.swing.Action openMapAction = am.get( "openMap" );
         openMapButton = new StreamflowButton( openMapAction );
         add( openMapButton );

         javax.swing.Action pasteMapCoordinatesAction = am.get( "pasteMapCoordinates" );
         pasteMapCoordinatesButton = new StreamflowButton( pasteMapCoordinatesAction );

         add( pasteMapCoordinatesButton );
      }
   }

   @Action
   public void pasteMapCoordinates(final ActionEvent event)
   {
      textField.requestFocusInWindow();
      SwingUtilities.invokeLater( new Runnable()
      {

         public void run()
         {
            textField.setText( "" );
            PasteAction pasteAction = new DefaultEditorKit.PasteAction();
            pasteAction.actionPerformed( event );
            String formattedValue = textField.getText().trim().replace( "," , "." ).replace( ";" , "," );
            textField.setText( formattedValue );
         }
      } );
   }

   @Action
   public void openMap()
   {
      Runtime rt = Runtime.getRuntime();
      try
      {
         String kartagoclientexe = model.kartagoclientexe( ((FieldSubmissionPluginDTO) getField()).plugin().get() );
         if (!Strings.empty( textField.getText() ))
         {
            kartagoclientexe += " xy=" + textField.getText();
         }
         rt.exec( kartagoclientexe );
      } catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   @Override
   public String getValue()
   {
      return textField.getText();
   }

   @Override
   public void setValue(String newValue)
   {
      textField.setText( newValue );
   }

   @Override
   public boolean validateValue(Object newValue)
   {
      return true;
   }

   @Override
   public void setBinding(final StateBinder.Binding binding)
   {
      final TextFieldPanel panel = this;
      textField.setInputVerifier( new InputVerifier()
      {
         @Override
         public boolean verify(JComponent input)
         {
            if (!Strings.empty( fieldValue.regularExpression().get() )
                  && !Strings.empty( ((JTextComponent) input).getText() ))
            {
               try
               {
                  new RegexPatternFormatter( fieldValue.regularExpression().get() )
                        .stringToValue( ((JTextComponent) input).getText() );
               } catch (ParseException e)
               {
                  dialogs.showMessageDialog( panel, i18n.text( CaseResources.regular_expression_does_not_validate ), "" );
                  return false;
               }
            }
            binding.updateProperty( ((JTextComponent) input).getText() );
            return true;
         }
      } );
   }

   @Override
   protected String componentName()
   {
      StringBuilder componentName = new StringBuilder( "<html>" );
      componentName.append( title() );
      if (!Strings.empty( fieldValue.hint().get() ))
      {
         componentName.append( " <font color='#778899'>(" ).append( fieldValue.hint().get() ).append( ")</font>" );
      }

      if (mandatory())
      {
         componentName.append( " <font color='red'>*</font>" );
      }
      componentName.append( "</html>" );
      return componentName.toString();
   }

}