/*
 * Copyright 2009-2010 Streamsource AB
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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import com.jgoodies.validation.util.ValidationUtils;
import com.jgoodies.validation.view.ValidationResultViewFactory;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.DateFunctions;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.util.BindingFormBuilder;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.domain.form.AttachmentFieldValue;
import se.streamsource.streamflow.domain.form.CheckboxesFieldValue;
import se.streamsource.streamflow.domain.form.ComboBoxFieldValue;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.ListBoxFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.domain.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class FormSubmissionWizardPageView
      extends WizardPage
      implements Observer, TransactionListener
{
   private java.util.Map<String, AbstractFieldPanel> componentFieldMap;
   private java.util.Map<StateBinder, EntityReference> fieldBinders;
   private ValidationResultModel validationResultModel;
   private FormSubmissionWizardPageModel model;
   private ObjectBuilderFactory obf;
   private static final Map<Class<? extends FieldValue>, Class<? extends AbstractFieldPanel>> fields = new HashMap<Class<? extends FieldValue>, Class<? extends AbstractFieldPanel>>();

   static
   {
      // Remember to add editors here when creating new types
      fields.put( CheckboxesFieldValue.class, CheckboxesPanel.class );
      fields.put( ComboBoxFieldValue.class, ComboBoxPanel.class );
      fields.put( DateFieldValue.class, DatePanel.class );
      fields.put( ListBoxFieldValue.class, ListBoxPanel.class );
      fields.put( NumberFieldValue.class, NumberPanel.class );
      fields.put( OptionButtonsFieldValue.class, OptionButtonsPanel.class );
      fields.put( OpenSelectionFieldValue.class, OpenSelectionPanel.class );
      fields.put( TextAreaFieldValue.class, TextAreaFieldPanel.class );
      fields.put( TextFieldValue.class, TextFieldPanel.class );
      fields.put( AttachmentFieldValue.class, AttachmentFieldPanel.class );
   }


   public FormSubmissionWizardPageView( @Structure ObjectBuilderFactory obf,
                                        @Uses PageSubmissionValue page,
                                        @Uses CommandQueryClient client )
   {
      super( page.title().get() );
      this.model = obf.newObjectBuilder( FormSubmissionWizardPageModel.class ).use( client ).newInstance();
      this.obf = obf;
      componentFieldMap = new HashMap<String, AbstractFieldPanel>();
      validationResultModel = new DefaultValidationResultModel();
      setLayout( new BorderLayout() );
      final JPanel panel = new JPanel( new FormLayout() );

      fieldBinders = new HashMap<StateBinder, EntityReference>( page.fields().get().size() );
      FormLayout formLayout = new FormLayout( "200dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );
      BindingFormBuilder bb = new BindingFormBuilder( formBuilder, null );

      for (FieldSubmissionValue value : page.fields().get())
      {
         AbstractFieldPanel component;
         FieldValue fieldValue = value.field().get().fieldValue().get();
         if (!(fieldValue instanceof CommentFieldValue))
         {
            component = getComponent( value );
            componentFieldMap.put( value.field().get().field().get().identity(), component );
            StateBinder stateBinder = component.bindComponent( bb, value );
            stateBinder.addObserver( this );
            fieldBinders.put( stateBinder, value.field().get().field().get() );

         } else
         {
            // comment field does not have any input component
            String comment = value.field().get().note().get();
            comment = comment.replaceAll( "\n", "<br/>" );

            JEditorPane commentPane = new JEditorPane( "text/html", "<html>" + comment + "</html>" );
            Font font = UIManager.getFont( "Label.font" );
            String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                  "font-size: " + font.getSize() + "pt; }";
            ((HTMLDocument) commentPane.getDocument()).getStyleSheet().addRule( bodyRule );

            commentPane.setOpaque( false );
            commentPane.setBorder( null );
            commentPane.setEditable( false );
            commentPane.addHyperlinkListener( new HyperlinkListener()
            {
               public void hyperlinkUpdate( HyperlinkEvent e )
               {
                  if (e.getEventType().equals( HyperlinkEvent.EventType.ACTIVATED ))
                  {
                     // Open in browser
                     try
                     {
                        Desktop.getDesktop().browse( e.getURL().toURI() );
                     } catch (IOException e1)
                     {
                        e1.printStackTrace();
                     } catch (URISyntaxException e1)
                     {
                        e1.printStackTrace();
                     }
                  }
               }
            } );

            bb.append( commentPane );
         }
      }

      JComponent validationResultsComponent = ValidationResultViewFactory.createReportList( validationResultModel );
      formBuilder.appendRow( "top:30dlu:g" );

      CellConstraints cc = new CellConstraints();
      formBuilder.add( validationResultsComponent, cc.xywh( 1, formBuilder.getRow() + 1, 1, 1, "fill, bottom" ) );

      final JScrollPane scroll = new JScrollPane( panel );
      add( scroll, BorderLayout.CENTER );

      for (Component component : panel.getComponents())
      {
         component.addFocusListener( new FocusAdapter()
         {

            @Override
            public void focusGained( FocusEvent e )
            {
               panel.scrollRectToVisible( e.getComponent().getBounds() );
            }
         } );
      }
   }

   @Override
   public WizardPanelNavResult allowNext( String s, Map map, Wizard wizard )
   {
      ValidationResult validation = validatePage();
      validationResultModel.setResult( validation );

      if (!validation.hasErrors())
      {
         // last page check needed ???
         return WizardPanelNavResult.PROCEED;
      }
      return WizardPanelNavResult.REMAIN_ON_PAGE;
   }

   @Override
   public WizardPanelNavResult allowBack( String stepName, Map settings, Wizard wizard )
   {
      // first page check needed ???
      return super.allowBack( stepName, settings, wizard );
   }

   @Override
   public WizardPanelNavResult allowFinish( String s, Map map, Wizard wizard )
   {
      ValidationResult validationResult = validatePage();
      validationResultModel.setResult( validationResult );
      if (validationResult.hasErrors())
      {
         return WizardPanelNavResult.REMAIN_ON_PAGE;
      }
      return WizardPanelNavResult.PROCEED;
   }

   private ValidationResult validatePage()
   {
      ValidationResult validationResult = new ValidationResult();

      for (AbstractFieldPanel component : componentFieldMap.values())
      {

         String value = component.getValue();

         if (component.mandatory())
         {
            if (ValidationUtils.isEmpty( value ))
            {
               validationResult.addError( i18n.text( WorkspaceResources.mandatory_field_missing ) + ": " + component.title() );
            }
         }
      }
      return validationResult;
   }

   public void update( Observable observable, Object arg )
   {
      Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "value" ))
      {
         try
         {
            if (property.get() instanceof Date)
            {
               model.updateField( fieldBinders.get( observable ), DateFunctions.toUtcString( (Date) property.get() ) );
            } else if (property.get() instanceof File)
            {
               try
               {
                  FileInputStream fin = new FileInputStream( (File) property.get() );
                  model.createAttachment( (File)property.get(), fin );
               } catch (Exception e)
               {
                  throw new OperationException( CaseResources.could_not_upload_file, e );
               }
            } else
            {
               model.updateField( fieldBinders.get( observable ), property.get().toString() );
            }
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_update_field, e );
         }
      }
   }

   public void updatePage( PageSubmissionValue page )
   {
      for (FieldSubmissionValue field : page.fields().get())
      {
         if (!(field.field().get().fieldValue().get() instanceof CommentFieldValue))
         {
            AbstractFieldPanel component = componentFieldMap.get( field.field().get().field().get().identity() );
            String value = component.getValue();
            if (field.value().get() != null && !field.value().get().equals( value ) || field.field().get().fieldValue().get() instanceof OpenSelectionFieldValue)
            {
               component.setValue( field.value().get() );
            }
         }
      }
   }

   private AbstractFieldPanel getComponent( FieldSubmissionValue field )
   {
      FieldValue fieldValue = field.field().get().fieldValue().get();
      Class<? extends FieldValue> fieldValueType = (Class<FieldValue>) fieldValue.getClass().getInterfaces()[0];
      return obf.newObjectBuilder( fields.get( fieldValueType ) ).use( field, fieldValue ).newInstance();
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( Events.withNames("addedAttachment" ), transactions ))
         ;
   }
}