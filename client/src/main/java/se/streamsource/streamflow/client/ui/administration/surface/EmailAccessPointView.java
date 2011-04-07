/**
 *
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

package se.streamsource.streamflow.client.ui.administration.surface;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsView;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.domain.organization.EmailAccessPointValue;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * TODO
 */
public class EmailAccessPointView
        extends JPanel
        implements TransactionListener, Refreshable
{
   @Service
   DialogService dialogs;

   @Uses
   protected ObjectBuilder<SelectLinkDialog> projectDialog;

   @Uses
   protected ObjectBuilder<SelectLinkDialog> caseTypeDialog;

   private EmailAccessPointModel model;
   private JTextField subject;

   private JList emailTemplateList = new JList();
   private JTextArea emailTemplateText = new JTextArea();
   private ObjectBuilderFactory obf;
   private JLabel project;
   private JLabel casetype;
   private JButton casetypeButton;
   private JButton projectButton;
   private JButton labelsButton;
   private CaseLabelsView labels;

   public EmailAccessPointView(@Service ApplicationContext context, @Uses final EmailAccessPointModel model, @Structure ObjectBuilderFactory obf)
   {
      this.obf = obf;
      setActionMap(context.getActionMap(this));

      this.model = model;
      this.labels = obf.newObjectBuilder( CaseLabelsView.class ).use( model.createLabelsModel() ).newInstance();


      FormLayout layout = new FormLayout(
              "75dlu, 5dlu, fill:p:grow", "pref, pref, pref, pref, pref, fill:p:grow, pref");
      DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout, this);

      formBuilder.append(projectButton = new JButton(getActionMap().get("project")));
      formBuilder.append(project = new JLabel());
      formBuilder.nextLine();
      formBuilder.append(casetypeButton = new JButton(getActionMap().get("casetype")));
      formBuilder.append(casetype = new JLabel());
      formBuilder.nextLine();
      formBuilder.append(labelsButton = new JButton(labels.getActionMap().get("addLabel")));
      formBuilder.append(labels);
      formBuilder.nextLine();
      formBuilder.addSeparator(i18n.text(AdministrationResources.emailTemplates));
      formBuilder.nextLine();
      formBuilder.append(i18n.text(AdministrationResources.subject), subject = new JTextField());
      formBuilder.nextLine();
      formBuilder.append(new JScrollPane(emailTemplateList));
      formBuilder.append(new JScrollPane(emailTemplateText));
      formBuilder.nextLine();

      emailTemplateList.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            if (!e.getValueIsAdjusting())
            {
               if (emailTemplateList.getSelectedIndex() != -1)
               {
                  emailTemplateText.setText(model.getValue().messages().get().get(emailTemplateList.getSelectedValue()));
               }
            }
         }
      });

      ActionBinder actionBinder = new ActionBinder(getActionMap());
      actionBinder.bind("save", emailTemplateText);
      actionBinder.bind("changeSubject", subject);

      new RefreshWhenShowing(this, this);
   }

   public void insertUpdate(DocumentEvent e)
   {
      model.getValue().messages().get().put(emailTemplateList.getSelectedValue().toString(), emailTemplateText.getText());
   }

   public void removeUpdate(DocumentEvent e)
   {
      model.getValue().messages().get().put(emailTemplateList.getSelectedValue().toString(), emailTemplateText.getText());
   }

   public void changedUpdate(DocumentEvent e)
   {
      model.getValue().messages().get().put(emailTemplateList.getSelectedValue().toString(), emailTemplateText.getText());
   }

   @org.jdesktop.application.Action
   public Task project()
   {
      final SelectLinkDialog dialog = projectDialog.use( model.getPossibleProjects() ).newInstance();
      dialogs.showOkCancelHelpDialog( projectButton, dialog, i18n.text( WorkspaceResources.choose_project ) );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeProject(dialog.getSelectedLink());
            }
         }
      };
   }

   @org.jdesktop.application.Action
   public Task casetype()
   {
      final SelectLinkDialog dialog = caseTypeDialog.use(
            i18n.text( WorkspaceResources.choose_casetype ),
            model.getPossibleCaseTypes() ).newInstance();
      dialogs.showOkCancelHelpDialog( casetypeButton, dialog );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeCaseType(dialog.getSelectedLink());
            }
         }
      };

   }

   public void refresh()
   {
      model.refresh();

      ValueBinder binder = obf.newObject(ValueBinder.class);

      binder.bind("project", project);
      binder.bind("caseType", casetype);
      binder.bind("subject", subject);
      EmailAccessPointValue value = model.getValue();
      binder.update(value);

      DefaultListModel emailTemplateListModel = new DefaultListModel();
      for (String key : value.messages().get().keySet())
      {
         emailTemplateListModel.addElement(key);
      }
      emailTemplateList.setModel(emailTemplateListModel);
   }

   @org.jdesktop.application.Action
   public Task changeSubject()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeSubject(subject.getText());
         }
      };
   }

   @org.jdesktop.application.Action
   public Task save()
   {
      final String template = emailTemplateText.getText();
      final String key = (String) emailTemplateList.getSelectedValue();

      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.updateTemplate(key, template);
            model.getValue().messages().get().put(key, template);
         }
      };
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      refresh();
   }
}
