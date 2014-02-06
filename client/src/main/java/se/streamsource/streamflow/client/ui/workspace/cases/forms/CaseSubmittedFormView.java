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
package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.date_time_format;
import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;

import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.ListBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.LocationDTO;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.form.FieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedPageDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.api.workspace.cases.general.SecondSigneeInfoValue;
import se.streamsource.streamflow.client.ui.DateFormats;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.OpenAttachmentTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.StreamflowSelectableLabel;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.util.Strings;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JAVADOC
 */
public class CaseSubmittedFormView
      extends JScrollPane
      implements TransactionListener, Refreshable
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   @Service
   ApplicationContext context;

   private JPanel panel;

   private CaseSubmittedFormModel model;

   private Map<StreamflowButton, AttachmentFieldSubmission> buttons = new HashMap<StreamflowButton, AttachmentFieldSubmission>();

   private SimpleDateFormat formatter = new SimpleDateFormat(i18n.text(WorkspaceResources.date_format));

   public CaseSubmittedFormView(@Service ApplicationContext context, @Uses CaseSubmittedFormModel model)
   {
      panel = new JPanel();
      panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
      setViewportView(panel);
      getViewport().setScrollMode( JViewport.SIMPLE_SCROLL_MODE );

      setMinimumSize( new Dimension( 150, 0 ) );

      this.model = model;

      setActionMap(context.getActionMap(this));

      new RefreshWhenShowing(this, this);
   }

   public void refresh()
   {
      model.refresh();
      panel().removeAll();
      final DefaultFormBuilder builder = builder(panel());
      SubmittedFormDTO form = model.getForm();

      StreamflowSelectableLabel title = new StreamflowSelectableLabel(form.form().get() + " (" + form.submitter().get() +
            ", " + DateTimeFormat.forPattern(text(date_time_format)).print(new DateTime(form.submissionDate().get())) +
            "):");

      builder.append(title);
      builder.nextLine();

      if (!form.signatures().get().isEmpty())
      {
         builder.appendSeparator(i18n.text(WorkspaceResources.signatures));
         for (FormSignatureDTO signatureDTO : form.signatures().get())
         {
            builder.append(new StreamflowSelectableLabel( signatureDTO.name().get() + ": " + signatureDTO.signerName().get() + "(" + signatureDTO.signerId() + ")" ) );
            builder.nextLine();
         }
      }

      if( form.secondSignee().get() != null )
      {
         SecondSigneeInfoValue secondSignee = form.secondSignee().get();
         builder.appendSeparator(i18n.text(WorkspaceResources.second_signee));

         if( !Strings.empty( secondSignee.name().get() ) )
         {
            builder.append(new StreamflowSelectableLabel( text( WorkspaceResources.name_label ) + ": " + secondSignee.name().get() ) );
            builder.nextLine();
         }

         if( !Strings.empty( secondSignee.phonenumber().get() ) )
         {
            builder.append( new StreamflowSelectableLabel( text( WorkspaceResources.phone_label ) + ": " + secondSignee.phonenumber().get() ) );
            builder.nextLine();
         }

         if( !Strings.empty( secondSignee.socialsecuritynumber().get() ) )
         {
            builder.append( new StreamflowSelectableLabel( text( WorkspaceResources.contact_id_label ) + ": " + secondSignee.socialsecuritynumber().get() ) );
            builder.nextLine();
         }

         if( !Strings.empty( secondSignee.email().get() ) )
         {
            builder.append( new StreamflowSelectableLabel( text( WorkspaceResources.email_label ) + ": " + secondSignee.email().get() ) );
            builder.nextLine();
         }

         if( !Strings.empty( secondSignee.secondsigneetaskref().get() ) )
         {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            StreamflowButton button = new StreamflowButton(context.getActionMap(this).get("resenddoublesignemail"));
            DateTime date = secondSignee.lastReminderSent().get();
            JLabel lastReminderSent = new JLabel( text( WorkspaceResources.last_reminder_sent ) + ": " + (date != null ? DateFormats.getProgressiveDateTimeValue( date, Locale.getDefault() ) : "" ) );
            panel.add( button );
            panel.add( lastReminderSent );
            builder.append( panel );
            builder.nextLine();
         }

         if( !Strings.empty( secondSignee.secondDraftUrl().get() ))
         {
            JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ));
            panel.add( new JLabel( text( WorkspaceResources.second_draft_url ) + ": " ) );
            panel.add( new StreamflowSelectableLabel( secondSignee.secondDraftUrl().get() ) );
            builder.append( panel );
            builder.nextLine();
         }

      }

      for (SubmittedPageDTO page : form.pages().get())
      {
         builder.appendSeparator(page.name().get());

         for (FieldDTO field : page.fields().get())
         {
            JLabel label = new JLabel(field.field().get() + ( field.field().get().trim().endsWith( ":" ) ? "" : ":" ), SwingConstants.LEFT);
            label.setForeground(Color.gray);

            JComponent component = getComponent(field.value().get(), field.fieldType().get());

            builder.append(label);
            builder.nextLine();
            builder.append(component);
            builder.nextLine();
         }
      }
   }

   @Action(block = Task.BlockingScope.APPLICATION)
   public Task open(ActionEvent event)
   {
      return openAttachment(event);
   }

   @Action
   public Task resenddoublesignemail()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
           model.resenddoublesignemail();
         }
      };
   }

   public JComponent getComponent(String fieldValue, String fieldType)
   {
      StreamflowSelectableLabel selectableLabel = new StreamflowSelectableLabel();
      selectableLabel.setContentType( "text/html" );
      JLabel fontSource = new JLabel( );


      final Desktop desktop = Desktop.getDesktop(); 
      
      selectableLabel.addHyperlinkListener(new HyperlinkListener()
      {
         public void hyperlinkUpdate(HyperlinkEvent hle) {
             if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                 try {
                     try {
                         desktop.browse(new URI(hle.getURL().toString()));
                     } catch (URISyntaxException ex) {
                         System.out.println( ex.getMessage());
                     }
                 } catch (IOException ex) {
                    System.out.println( ex.getMessage());
                 }

             }
         }
     });
      
      String text = "<html> <font size='3' face='"+ fontSource.getFont().getFontName() +"'>";

      if (fieldType.equals(DateFieldValue.class.getName()))
      {
         text += Strings.empty(fieldValue) ? " " : formatter.format(DateFunctions.fromString(fieldValue));
      } else if (fieldType.equals(TextAreaFieldValue.class.getName()))
      {
         text += fieldValue.replace("\n", "<br/>");
      } else if (fieldType.equals(AttachmentFieldValue.class.getName()))
      {
         if( Strings.empty( fieldValue ))
         {
            text += fieldValue;
         } else
         {
            final AttachmentFieldSubmission attachment = module.valueBuilderFactory().newValueFromJSON(AttachmentFieldSubmission.class, fieldValue);
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            StreamflowSelectableLabel onPanel = new StreamflowSelectableLabel(  );
            onPanel.setContentType( "text/html" );
            onPanel.setText( text + attachment.name().get() + "</font></html>" );
            panel.add( onPanel );
            StreamflowButton button = new StreamflowButton(context.getActionMap(this).get("open"));
            buttons.put(button, attachment);
            panel.add(button);
            return panel;
         }
      } else if (fieldType.equals( CheckboxesFieldValue.class.getName()) || fieldType.equals( ListBoxFieldValue.class.getName() ))
      {
         // replace all [ with " and ] with "
         fieldValue = fieldValue.replaceAll( "\\[", "\"" );
         fieldValue = fieldValue.replaceAll( "\\]", "\"" );
         text += fieldValue;
      }  else if (fieldType.equals(GeoLocationFieldValue.class.getName()))
      {
         LocationDTO locationDTO = module.valueBuilderFactory().newValueFromJSON( LocationDTO.class, fieldValue );
         text += locationDTO.street().get() + ", " + locationDTO.zipcode().get() + ", " + locationDTO.city().get() + "<br>";
         String locationString = locationDTO.location().get();
         if (locationString != null) {
            locationString = locationString.replace( ' ', '+' );
            if (locationString.contains( "(" )) {
               String[] positions = locationString.split( "\\),\\(");
               locationString = positions[0].substring( 1, positions[0].length() -1 );
            }
         }
         text += "<a href=\"http://maps.google.com/maps?z=13&t=m&q=" + locationString + "\" alt=\"Google Maps\">Klicka här för att visa karta</a>";
      } else
      {
         text += fieldValue;
      }
      text += "</font></html>";
      selectableLabel.setText( text );
      return selectableLabel;
   }

   public Task openAttachment(ActionEvent event)
   {
      AttachmentFieldSubmission selectedDocument = buttons.get(event.getSource());
      return new OpenAttachmentTask(selectedDocument.name().get(), selectedDocument.attachment().get().identity(), this, model, dialogs );
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (Events.matches(Events.withNames("submittedForm", "updatedLastReminderSent"), transactions))
      {
         refresh();
      }
   }

   protected JPanel panel()
   {
      return panel;
   }

   protected DefaultFormBuilder builder(JPanel aPanel)
   {
      FormLayout formLayout = new FormLayout("150dlu:grow", "");
      return new DefaultFormBuilder(formLayout, aPanel);
   }

   public void read()
   {
      new CommandTask(){

         @Override
         protected void command() throws Exception
         {
            model.read();
         }
      }.execute();
   }
}