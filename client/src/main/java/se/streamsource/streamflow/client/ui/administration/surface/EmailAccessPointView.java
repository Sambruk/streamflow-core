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
package se.streamsource.streamflow.client.ui.administration.surface;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.administration.surface.EmailAccessPointDTO;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.RemovableLabel;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Insets;

/**
 * TODO
 */
public class EmailAccessPointView
        extends JScrollPane
        implements TransactionListener, Refreshable
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private EmailAccessPointModel model;
   private JTextField subject;

   private JList emailTemplateList = new JList();
   private JTextArea emailTemplateText = new JTextArea();
   private JLabel project;
   private RemovableLabel casetype = new RemovableLabel(  );
   private StreamflowButton casetypeButton;
   private StreamflowButton projectButton;
   private StreamflowButton labelsButton;
   private CaseLabelsView labels;

   public void init(@Service ApplicationContext context, @Uses final EmailAccessPointModel model)
   {
      setActionMap(context.getActionMap(this));
      JPanel panel = new JPanel();
      this.setViewportView( panel );
      
      this.model = model;
      this.labels = module.objectBuilderFactory().newObjectBuilder(CaseLabelsView.class).use( model.createLabelsModel() ).newInstance();


      FormLayout layout = new FormLayout(
              "75dlu, 5dlu, fill:p:grow", "pref, pref, pref, pref, pref, fill:p:grow, pref");
      DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout, panel);

      formBuilder.append(projectButton = new StreamflowButton(getActionMap().get("project")));
      formBuilder.append(project = new JLabel());
      formBuilder.nextLine();
      formBuilder.append(casetypeButton = new StreamflowButton(getActionMap().get("casetype")));
      formBuilder.add( casetype, new CellConstraints( 3, 2, 1, 1, CellConstraints.LEFT, CellConstraints.CENTER, new Insets( 3, 0, 0, 0 ) ) );
      formBuilder.nextLine();
      formBuilder.append(labelsButton = new StreamflowButton(labels.getActionMap().get("addLabel")));
      formBuilder.add(labels, new CellConstraints( 3, 3, 1, 1, CellConstraints.LEFT, CellConstraints.CENTER, new Insets( 3, -3 , 0, 0 ) ) );
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
      actionBinder.bind( "save", emailTemplateText );
      actionBinder.bind( "changeSubject", subject );
      actionBinder.bind( "removecasetype", casetype );

      new RefreshWhenShowing(this, this);
   }

   @org.jdesktop.application.Action
   public Task project()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use( model.getPossibleProjects() ).newInstance();
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
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(
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

   @Action
   public Task removecasetype()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.removeCaseType();
         }
      };
   }

   public void refresh()
   {
      model.refresh();

      ValueBinder binder = module.objectBuilderFactory().newObject(ValueBinder.class);

      binder.bind("project", project);
      binder.bind("caseType", casetype);
      binder.bind("subject", subject);
      EmailAccessPointDTO value = model.getValue();
      binder.update(value);
      
      int selectedIndex = emailTemplateList.getSelectedIndex();
      DefaultListModel emailTemplateListModel = new DefaultListModel();
      for (String key : value.messages().get().keySet())
      {
         emailTemplateListModel.addElement(key);
      }
      emailTemplateList.setModel(emailTemplateListModel);
      emailTemplateList.setSelectedIndex( selectedIndex );
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
