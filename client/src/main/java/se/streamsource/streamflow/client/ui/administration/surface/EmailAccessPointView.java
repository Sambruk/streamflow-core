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
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.domain.organization.EmailAccessPointValue;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

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
        implements TransactionListener, Refreshable, DocumentListener
{
   private EmailAccessPointModel model;
   private JTextField subject;

   private JList emailTemplateList = new JList();
   private JTextArea emailTemplateText = new JTextArea();
   private ObjectBuilderFactory obf;

   public EmailAccessPointView(@Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      this.obf = obf;
      setActionMap(context.getActionMap(this));

      model = obf.newObjectBuilder(EmailAccessPointModel.class).use(client).newInstance();

      FormLayout layout = new FormLayout(
              "75dlu, 5dlu, 220dlu", "pref, pref, pref, fill:p:grow, pref");
      DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout, this);

      EmailAccessPointValue emailAccessPoint = client.query("index", EmailAccessPointValue.class);

      formBuilder.append(i18n.text(AdministrationResources.email), new JLabel(emailAccessPoint.email().get()));
      formBuilder.nextLine();
      formBuilder.append(i18n.text(AdministrationResources.subject), subject = new JTextField());
      formBuilder.nextLine();
      formBuilder.append(new JLabel(i18n.text(AdministrationResources.emailTemplates), JLabel.CENTER), 2);

      formBuilder.append(new JScrollPane(emailTemplateList));
      formBuilder.append(new JScrollPane(emailTemplateText));
      formBuilder.nextLine();

      formBuilder.append(new JButton(getActionMap().get("save")));

      emailTemplateList.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            if (!e.getValueIsAdjusting())
            {
               if (emailTemplateList.getSelectedIndex() != -1)
               {
                  emailTemplateText.getDocument().removeDocumentListener(EmailAccessPointView.this);
                  emailTemplateText.setText(model.getValue().messages().get().get(emailTemplateList.getSelectedValue()));
                  emailTemplateText.getDocument().addDocumentListener(EmailAccessPointView.this);
               }
            }
         }
      });

      emailTemplateText.getDocument().addDocumentListener(this);

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

   public void refresh()
   {
      model.refresh();

      ValueBinder binder = obf.newObject(ValueBinder.class);
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
   public Task save()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.getValue().subject().set(subject.getText());

            model.update();
         }
      };
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }
}
