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

package se.streamsource.streamflow.client.ui.account;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.caze.CaseResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;

/**
 * JAVADOC
 */
public class ProfileView
      extends JScrollPane
      implements Observer, Refreshable
{
   private ProfileModel model;

   private StateBinder contactBinder;
   private StateBinder phoneNumberBinder;
   private StateBinder emailBinder;

   public JPanel contactForm;
   public JRadioButton noneButton;
   public JRadioButton emailButton;

   public ProfileView(@Service ApplicationContext context, @Structure ObjectBuilderFactory obf, @Uses CommandQueryClient client)
   {
      ApplicationActionMap am = context.getActionMap(this);
      setActionMap(am);

      model = obf.newObjectBuilder( ProfileModel.class ).use( client ).newInstance();
      model.addObserver( this );

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

      contactForm = new JPanel();
      panel.add(contactForm, BorderLayout.CENTER);
      FormLayout contactLayout = new FormLayout("75dlu, 5dlu, 120dlu:grow",
            "pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref");

      contactBinder = obf.newObject( StateBinder.class );
      contactBinder.setResourceMap(context.getResourceMap(getClass()));
      ContactValue contactTemplate = contactBinder
            .bindingTemplate(ContactValue.class);

      phoneNumberBinder = obf.newObject( StateBinder.class );
      phoneNumberBinder.setResourceMap(context.getResourceMap(getClass()));
      ContactPhoneValue phoneTemplate = phoneNumberBinder
            .bindingTemplate(ContactPhoneValue.class);

      emailBinder = obf.newObject( StateBinder.class );
      emailBinder.setResourceMap(context.getResourceMap(getClass()));
      ContactEmailValue emailTemplate = emailBinder
            .bindingTemplate(ContactEmailValue.class);

      DefaultFormBuilder contactBuilder = new DefaultFormBuilder(contactLayout,
            contactForm);

      contactBuilder.appendSeparator(i18n
            .text(AccountResources.contact_info_for_user_separator));
      contactBuilder.nextLine();

      contactBuilder.add(new JLabel(i18n.text(WorkspaceResources.name_label)));
      contactBuilder.nextColumn(2);
      contactBuilder.add(contactBinder.bind(TEXTFIELD.newField(),
            contactTemplate.name()));
      contactBuilder.nextLine();

      contactBuilder.add(new JLabel(i18n.text(WorkspaceResources.email_label)));
      contactBuilder.nextColumn(2);
      contactBuilder.add(emailBinder.bind(TEXTFIELD.newField(), emailTemplate
            .emailAddress()));
      contactBuilder.nextLine();

      contactBuilder.add(new JLabel(i18n.text(WorkspaceResources.phone_label)));
      contactBuilder.nextColumn(2);
      contactBuilder.add(phoneNumberBinder.bind(TEXTFIELD.newField(),
            phoneTemplate.phoneNumber()));
      contactBuilder.nextLine(2);

      contactBuilder.add(new JLabel(i18n
            .text(WorkspaceResources.choose_message_delivery_type)));
      noneButton = (JRadioButton) RADIOBUTTON.newField();
      noneButton.setAction(am.get("messageDeliveryTypeNone"));
      noneButton.setSelected(true);
      contactBuilder.nextColumn(2);
      contactBuilder.add(noneButton);
      contactBuilder.nextLine();
      contactBuilder.nextColumn(2);
      emailButton = (JRadioButton) RADIOBUTTON.newField();
      emailButton.setAction(am.get("messageDeliveryTypeEmail"));
      contactBuilder.add(emailButton);

      // Group the radio buttons.
      ButtonGroup group = new ButtonGroup();
      group.add(noneButton);
      group.add(emailButton);

      contactBuilder.nextLine(2);

      contactBinder.addObserver(this);
      phoneNumberBinder.addObserver(this);
      emailBinder.addObserver(this);
      setViewportView(panel);

      new RefreshWhenVisible(this, model);
   }

   @Action
   public void messageDeliveryTypeNone() throws Exception
   {
      model.changeMessageDeliveryType("none");
   }

   @Action
   public void messageDeliveryTypeEmail() throws Exception
   {
      model.changeMessageDeliveryType("email");
   }

   @Action
   public void execute()
   {
      
   }

   public void refresh()
   {
      model.refresh();
   }

   public void update(Observable observable, Object arg)
   {
      if (observable == model)
      {
         contactBinder.updateWith(model.getContact());
         phoneNumberBinder.updateWith(model.getPhoneNumber());
         emailBinder.updateWith(model.getEmailAddress());

         String messageDeliveryType = model.getMessageDeliveryType();
         if ("email".equalsIgnoreCase(messageDeliveryType))
         {
            emailButton.setSelected(true);
         } else
         {
            noneButton.setSelected(true);
         }
      } else
      {
         Property property = (Property) arg;
         if (property.qualifiedName().name().equals("name"))
         {
            try
            {
               model.changeName((String) property.get());
            } catch (ResourceException e)
            {
               throw new OperationException( CaseResources.could_not_change_name, e);
            }
         } else if (property.qualifiedName().name().equals("phoneNumber"))
         {
            try
            {
               model.changePhoneNumber((String) property.get());
            } catch (ResourceException e)
            {
               throw new OperationException(
                     CaseResources.could_not_change_phone_number, e);
            }
         } else if (property.qualifiedName().name().equals("emailAddress"))
         {
            try
            {
               model.changeEmailAddress((String) property.get());
            } catch (ResourceException e)
            {
               throw new OperationException(
                     CaseResources.could_not_change_email_address, e);
            }
         }
      }
   }

}