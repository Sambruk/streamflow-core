/**
 *
 * Copyright 2009-2012 Streamsource AB
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.restlet.representation.Representation;

import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.form.FieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedPageDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowButton;
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
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      setViewportView(panel);

      setMinimumSize(new Dimension(150, 0));

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

      JLabel title = new JLabel(form.form().get() + " (" + form.submitter().get() +
            ", " + DateTimeFormat.forPattern(text(date_time_format)).print(new DateTime(form.submissionDate().get())) +
            "):");
      //title.setFont( title.getFont().deriveFont( Font. ))

      builder.append(title);
      builder.nextLine();

      if (!form.signatures().get().isEmpty())
      {
         builder.appendSeparator(i18n.text(WorkspaceResources.signatures));
         for (FormSignatureDTO signatureDTO : form.signatures().get())
         {
            builder.append(signatureDTO.name().get() + ": " + signatureDTO.signerName().get() + "(" + signatureDTO.signerId() + ")");
            builder.nextLine();
         }
      }

      for (SubmittedPageDTO page : form.pages().get())
      {
         builder.appendSeparator(page.name().get());

         for (FieldDTO field : page.fields().get())
         {
            JLabel label = new JLabel(field.field().get() + ":", SwingConstants.LEFT);
            label.setForeground(Color.gray);
            JComponent component = getComponent(field.value().get(), field.fieldType().get());

            builder.append(label);
            builder.nextLine();
            builder.append(component);
            builder.nextLine();
         }
      }
      revalidate();
      repaint();
   }

   protected FormAttachmentDownload getModel()
   {
      return model;
   }

   @Action
   public Task open(ActionEvent event)
   {
      return openAttachment(event);
   }

   public JComponent getComponent(String fieldValue, String fieldType)
   {
      JComponent component;
      if (fieldType.equals(DateFieldValue.class.getName()))
      {
         component = new JLabel(Strings.empty(fieldValue) ? " " : formatter.format(DateFunctions.fromString(fieldValue)));
      } else if (fieldType.equals(TextAreaFieldValue.class.getName()))
      {
         component = new JLabel("<html>" + fieldValue.replace("\n", "<br/>") + "</html>");
      } else if (fieldType.equals(AttachmentFieldValue.class.getName()))
      {
         final AttachmentFieldSubmission attachment = module.valueBuilderFactory().newValueFromJSON(AttachmentFieldSubmission.class, fieldValue);
         JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         panel.setBackground(Color.WHITE);
         panel.add(new JLabel(attachment.name().get()));
         StreamflowButton button = new StreamflowButton(context.getActionMap(this).get("open"));
         buttons.put(button, attachment);
         panel.add(button);
         component = panel;
      } else
      {
         component = new JLabel(fieldValue);
      }
//      component.setBorder( BorderFactory.createEtchedBorder() );
      return component;
   }

   public Task openAttachment(ActionEvent event)
   {
      AttachmentFieldSubmission selectedDocument = buttons.get(event.getSource());
      return new OpenAttachmentTask(selectedDocument);
   }

   private class OpenAttachmentTask extends Task<File, Void>
   {
      private final AttachmentFieldSubmission attachment;

      public OpenAttachmentTask(AttachmentFieldSubmission attachment)
      {
         super(Application.getInstance());
         this.attachment = attachment;

         setUserCanCancel(false);
      }

      @Override
      protected File doInBackground() throws Exception
      {
         setMessage(getResourceMap().getString("description"));

         String fileName = attachment.name().get();
         String[] fileNameParts = fileName.split("\\.");

         Representation representation = getModel().download(attachment.attachment().get().identity());

         File file = File.createTempFile(fileNameParts[0] + "_", "." + fileNameParts[1]);

         Inputs.byteBuffer(representation.getStream(), 8192).transferTo(Outputs.byteBuffer(file));

         return file;
      }

      @Override
      protected void succeeded(File file)
      {
         // Open file
         Desktop desktop = Desktop.getDesktop();
         try
         {
            desktop.edit(file);
         } catch (IOException e)
         {
            try
            {
               desktop.open(file);
            } catch (IOException e1)
            {
               dialogs.showMessageDialog(CaseSubmittedFormView.this, i18n.text(WorkspaceResources.could_not_open_attachment), "");
            }
         }
      }
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (Events.matches(Events.withNames("submittedForm"), transactions))
      {
         getModel().refresh();
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
}