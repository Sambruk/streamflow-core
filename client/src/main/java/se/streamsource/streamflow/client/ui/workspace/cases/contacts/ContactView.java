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

package se.streamsource.streamflow.client.ui.workspace.cases.contacts;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.TEXTAREA;
import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.TEXTFIELD;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactAddressDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactsDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.SuggestTextField;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JAVADOC
 */
public class ContactView
      extends JPanel
      implements Observer
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private StateBinder contactBinder;
   private StateBinder phoneNumberBinder;
   private StateBinder emailBinder;
   private StateBinder addressBinder;

   ContactModel model;

   private CardLayout layout = new CardLayout();
   public JPanel form;
   private JTextField defaultFocusField;
   private JTextField addressField;
   private StreetAddressSuggestTextField suggestAddress;
   private JTextField zipField = (JTextField) TEXTFIELD.newField();
   private JTextField cityField = (JTextField) TEXTFIELD.newField();
   private JTextField regionField = (JTextField) TEXTFIELD.newField();
   private JTextField countryField = (JTextField) TEXTFIELD.newField();
   private JTextField phoneField = (JTextField) TEXTFIELD.newField();
   private JTextField emailField = (JTextField) TEXTFIELD.newField();
   private JTextField contactIdField = (JTextField) TEXTFIELD.newField();
   private JTextField companyField = (JTextField) TEXTFIELD.newField();

   private ApplicationContext context;
   private JPanel lookupPanel;
   private ValueBinder viewBinder;
   private ValueBinder phoneViewBinder;
   private ValueBinder addressViewBinder;
   private ValueBinder emailViewBinder;

   private StreetAddressSuggestModel suggestModel;

   public ContactView(@Service ApplicationContext appContext, @Structure Module module)
   {
      setLayout(layout);

      context = appContext;
      setActionMap(context.getActionMap(this));

      add(new JLabel(), "EMPTY");

      viewBinder = module.objectBuilderFactory().newObject(ValueBinder.class);
      phoneViewBinder = module.objectBuilderFactory().newObject(ValueBinder.class);
      addressViewBinder = module.objectBuilderFactory().newObject(ValueBinder.class);
      emailViewBinder = module.objectBuilderFactory().newObject(ValueBinder.class);

      suggestModel = new StreetAddressSuggestModel();
      suggestAddress = new StreetAddressSuggestTextField( suggestModel, cityField, addressViewBinder );
      addressField = suggestAddress.getTextField();
      
      // Edit panel
      {
         FormLayout formLayout = new FormLayout(
               "right:70dlu, 5dlu, 150dlu:grow",
               "pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, 5dlu, top:70dlu:grow, pref, pref");
         this.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

         form = new JPanel();
         JScrollPane scrollPane = new JScrollPane(form);
         scrollPane.getVerticalScrollBar().setUnitIncrement(30);
         scrollPane.setBorder(BorderFactory.createEmptyBorder());
         DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, form);

         contactBinder = module.objectBuilderFactory().newObject(StateBinder.class);
         contactBinder.setResourceMap(context.getResourceMap(getClass()));
         ContactDTO template = contactBinder.bindingTemplate(ContactDTO.class);

         phoneNumberBinder = module.objectBuilderFactory().newObject(StateBinder.class);
         phoneNumberBinder.setResourceMap(context.getResourceMap(getClass()));
         ContactPhoneDTO phoneTemplate = phoneNumberBinder.bindingTemplate(ContactPhoneDTO.class);

         addressBinder = module.objectBuilderFactory().newObject(StateBinder.class);
         addressBinder.setResourceMap(context.getResourceMap(getClass()));
         ContactAddressDTO addressTemplate = addressBinder.bindingTemplate(ContactAddressDTO.class);

         emailBinder = module.objectBuilderFactory().newObject(StateBinder.class);
         emailBinder.setResourceMap(context.getResourceMap(getClass()));
         ContactEmailDTO emailTemplate = emailBinder.bindingTemplate(ContactEmailDTO.class);

         builder.add(new JButton(getActionMap().get("view")));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.name_label));
         builder.nextColumn(2);
         builder.add(contactBinder.bind(defaultFocusField = (JTextField) TEXTFIELD.newField(), template.name()));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.phone_label));
         builder.nextColumn(2);
         builder.add(phoneNumberBinder.bind(phoneField, phoneTemplate.phoneNumber()));
         builder.nextLine();

         builder.add(createLabel(WorkspaceResources.address_label));
         builder.nextColumn(2);
         builder.add( suggestAddress);
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.zip_label));
         builder.nextColumn(2);
         builder.add(addressBinder.bind(zipField, addressTemplate.zipCode()));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.city_label));
         builder.nextColumn(2);
         builder.add(addressBinder.bind(cityField, addressTemplate.city()));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.region_label));
         builder.nextColumn(2);
         builder.add(addressBinder.bind(regionField, addressTemplate.region()));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.country_label));
         builder.nextColumn(2);
         builder.add(addressBinder.bind(countryField, addressTemplate.country()));
         builder.nextLine();

         builder.add(createLabel(WorkspaceResources.email_label));
         builder.nextColumn(2);
         builder.add(emailBinder.bind(emailField, emailTemplate.emailAddress()));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.contact_id_label));
         builder.nextColumn(2);
         builder.add(contactBinder.bind(contactIdField, template.contactId()));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.company_label));
         builder.nextColumn(2);
         builder.add(contactBinder.bind(companyField, template.company()));
         builder.nextLine(2);
         builder.add(createLabel(WorkspaceResources.note_label));
         builder.nextColumn(2);
         builder.add(contactBinder.bind(TEXTAREA.newField(), template.note()));

         builder.nextLine(2);
         builder.nextColumn(2);
         lookupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         lookupPanel.add(new JButton(getActionMap().get("lookupContact")));
         builder.add(lookupPanel);

         contactBinder.addObserver(this);
         phoneNumberBinder.addObserver(this);
         addressBinder.addObserver(this);
         emailBinder.addObserver(this);
         add(scrollPane, "EDIT");
      }

      // View panel
      {
         FormLayout formLayout = new FormLayout(
               "right:70dlu, 5dlu, 150dlu:grow",
               "pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, 5dlu, top:70dlu:grow, pref, pref");
         this.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

         form = new JPanel();
         JScrollPane scrollPane = new JScrollPane(form);
         scrollPane.getVerticalScrollBar().setUnitIncrement(30);
         scrollPane.setBorder(BorderFactory.createEmptyBorder());
         DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, form);

         builder.add(new JButton(getActionMap().get("edit")));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.name_label));
         builder.nextColumn(2);
         builder.add(viewBinder.bind("name", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.phone_label));
         builder.nextColumn(2);
         builder.add(phoneViewBinder.bind("phoneNumber", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine();

         builder.add(createLabel(WorkspaceResources.address_label));
         builder.nextColumn(2);
         builder.add(addressViewBinder.bind("address", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.zip_label));
         builder.nextColumn(2);
         builder.add(addressViewBinder.bind("zipCode", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.city_label));
         builder.nextColumn(2);
         builder.add(addressViewBinder.bind("city", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.region_label));
         builder.nextColumn(2);
         builder.add(addressViewBinder.bind("region", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.country_label));
         builder.nextColumn(2);
         builder.add(addressViewBinder.bind("country", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine();

         builder.add(createLabel(WorkspaceResources.email_label));
         builder.nextColumn(2);
         builder.add(emailViewBinder.bind("emailAddress", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.contact_id_label));
         builder.nextColumn(2);
         builder.add(viewBinder.bind("contactId", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine();
         builder.add(createLabel(WorkspaceResources.company_label));
         builder.nextColumn(2);
         builder.add(viewBinder.bind("company", visibleIfNotEmpty(new JLabel("!"))));
         builder.nextLine(2);
         builder.add(createLabel(WorkspaceResources.note_label));
         builder.nextColumn(2);
         builder.add(viewBinder.bind("note", visibleIfNotEmpty(new JLabel("!"))));

         add(scrollPane, "VIEW");
      }
   }

   private JLabel createLabel(Enum key)
   {
      JLabel label = new JLabel(i18n.text(key));
      label.setForeground(Color.gray);
      return label;
   }

   public void setModel(ContactModel model)
   {
      this.model = model;
      if (model != null)
      {
         suggestModel.setContactModel( model );
         contactBinder.updateWith(model.getContact());
         phoneNumberBinder.updateWith(model.getPhoneNumber());
         addressBinder.updateWith(model.getAddress());
         suggestAddress.getTextField().setText( model.getAddress().address() != null ? model.getAddress().address().get() : "" );
         emailBinder.updateWith(model.getEmailAddress());

         viewBinder.update(model.getContact());
         phoneViewBinder.update(model.getPhoneNumber());
         addressViewBinder.update(model.getAddress());
         emailViewBinder.update(model.getEmailAddress());

         javax.swing.Action action = getActionMap().get("lookupContact");
         action.setEnabled(model.isContactLookupEnabled());
         lookupPanel.setVisible(action.isEnabled());

         if (model.getContact().toJSON().equals("{\"addresses\":[{\"address\":\"\",\"city\":\"\",\"contactType\":\"HOME\",\"country\":\"\",\"region\":\"\",\"zipCode\":\"\"}],\"company\":\"\",\"contactId\":\"\",\"emailAddresses\":[{\"contactType\":\"HOME\",\"emailAddress\":\"\"}],\"isCompany\":false,\"name\":\"\",\"note\":\"\",\"phoneNumbers\":[{\"contactType\":\"HOME\",\"phoneNumber\":\"\"}],\"picture\":\"\"}"))
            layout.show(this, "EDIT");
         else
            layout.show(this, "VIEW");

      } else
      {
         layout.show(this, "EMPTY");
      }
   }


   public void update(Observable observable, Object arg)
   {
      final Property property = (Property) arg;
      new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            String propertyName = property.qualifiedName().name();
            if (propertyName.equals("name"))
            {
               model.changeName((String) property.get());
            } else if (propertyName.equals("note"))
            {
               model.changeNote((String) property.get());
            } else if (propertyName.equals("company"))
            {
               model.changeCompany((String) property.get());
            } else if (propertyName.equals("phoneNumber"))
            {
               model.changePhoneNumber((String) property.get());
            } else if (propertyName.equals("address"))
            {
               model.changeAddress((String) property.get());
            } else if (propertyName.equals("zipCode"))
            {
               model.changeZipCode((String) property.get());
            } else if (propertyName.equals("city"))
            {
               model.changeCity((String) property.get());
            } else if (propertyName.equals("region"))
            {
               model.changeRegion((String) property.get());
            } else if (propertyName.equals("country"))
            {
               model.changeCountry((String) property.get());
            } else if (propertyName.equals("emailAddress"))
            {
               model.changeEmailAddress((String) property.get());
            } else if (propertyName.equals("contactId"))
            {
               model.changeContactId((String) property.get());
            }
         }
      }.execute();
   }

   @Action
   public void edit()
   {
      layout.show(this, "EDIT");
   }

   @Action
   public void view()
   {
      setModel(model);
      layout.show(this, "VIEW");
   }

   @Action
   public void lookupContact()
   {
      try
      {
         ContactDTO query = createContactQuery();

         ContactDTO emptyCriteria = module.valueBuilderFactory().newValueBuilder(ContactDTO.class).newInstance();
         if (emptyCriteria.equals(query))
         {
            String msg = i18n.text(CaseResources.could_not_find_search_criteria);
            dialogs.showMessageDialog(this, msg, "Info");

         } else
         {

            ContactsDTO contacts = model.searchContacts(query);

            if (contacts.contacts().get().isEmpty())
            {
               String msg = i18n.text(CaseResources.could_not_find_contacts);
               dialogs.showMessageDialog(this, msg, "Info");
            } else
            {

               ContactLookupResultDialog dialog = module.objectBuilderFactory().newObjectBuilder(ContactLookupResultDialog.class).use(
                     contacts.contacts().get()).newInstance();
               dialogs.showOkCancelHelpDialog(WindowUtils.findWindow(this), dialog, i18n.text(WorkspaceResources.contacts_tab));

               ContactDTO contactValue = dialog.getSelectedContact();

               if (contactValue != null)
               {
                  if (defaultFocusField.getText().equals("") && !contactValue.name().get().equals(""))
                  {
                     model.changeName(contactValue.name().get());
                     defaultFocusField.setText(contactValue.name().get());
                  }

                  for (ContactPhoneDTO contactPhoneDTO : contactValue.phoneNumbers().get())
                  {
                     if (!contactPhoneDTO.phoneNumber().get().equals("") && model.getPhoneNumber().phoneNumber().get().equals(""))
                     {
                        model.changePhoneNumber(contactPhoneDTO.phoneNumber().get());
                        phoneField.setText(contactPhoneDTO.phoneNumber().get());
                     }
                  }

                  List<ContactAddressDTO> addressDTOs = contactValue.addresses().get();
                  for (ContactAddressDTO addressDTO : addressDTOs)
                  {
                     if (!addressDTO.address().get().equals("") && model.getAddress().address().get().equals(""))
                     {
                        model.changeAddress(addressDTO.address().get());
                        addressField.setText(addressDTO.address().get());
                     }
                  }

                  List<ContactEmailDTO> emailDTOs = contactValue.emailAddresses().get();
                  for (ContactEmailDTO emailDTO : emailDTOs)
                  {
                     if (!emailDTO.emailAddress().get().equals("") && model.getEmailAddress().emailAddress().get().equals(""))
                     {
                        model.changeEmailAddress(emailDTO.emailAddress().get());
                        emailField.setText(emailDTO.emailAddress().get());
                     }
                  }

                  if (contactIdField.getText().equals("") && !contactValue.contactId().get().equals(""))
                  {
                     model.changeContactId(contactValue.contactId().get());
                     contactIdField.setText(contactValue.contactId().get());
                  }

                  if (companyField.getText().equals("") && !contactValue.company().get().equals(""))
                  {
                     model.changeCompany(contactValue.company().get());
                     companyField.setText(contactValue.company().get());
                  }
               }
            }
         }

      } catch (ResourceException e)
      {
         e.printStackTrace();
      }
   }

   private ContactDTO createContactQuery()
   {
      ValueBuilder<ContactDTO> contactBuilder = module.valueBuilderFactory().newValueBuilder(ContactDTO.class);

      if (!defaultFocusField.getText().isEmpty())
      {
         contactBuilder.prototype().name().set(defaultFocusField.getText());
      }

      if (!phoneField.getText().isEmpty())
      {
         ValueBuilder<ContactPhoneDTO> builder = module.valueBuilderFactory().newValueBuilder(ContactPhoneDTO.class);
         builder.prototype().phoneNumber().set(phoneField.getText());
         contactBuilder.prototype().phoneNumbers().get().add(builder.newInstance());
      }

      if (!addressField.getText().isEmpty())
      {
         ValueBuilder<ContactAddressDTO> builder = module.valueBuilderFactory().newValueBuilder(ContactAddressDTO.class);
         builder.prototype().address().set(addressField.getText());
         contactBuilder.prototype().addresses().get().add(builder.newInstance());
      }

      if (!emailField.getText().isEmpty())
      {
         ValueBuilder<ContactEmailDTO> builder = module.valueBuilderFactory().newValueBuilder(ContactEmailDTO.class);
         builder.prototype().emailAddress().set(emailField.getText());
         contactBuilder.prototype().emailAddresses().get().add(builder.newInstance());
      }

      if (!contactIdField.getText().isEmpty())
      {
         contactBuilder.prototype().contactId().set(contactIdField.getText());
      }

      if (!companyField.getText().isEmpty())
      {
         contactBuilder.prototype().contactId().set(contactIdField.getText());
      }
      return contactBuilder.newInstance();
   }

   private JLabel visibleIfNotEmpty(final JLabel label)
   {
      label.addPropertyChangeListener("text", new PropertyChangeListener()
      {
         public void propertyChange(PropertyChangeEvent evt)
         {
            label.setVisible(!evt.getNewValue().equals(""));

            Container container = label.getParent();
            for (int i = 0; i < container.getComponents().length; i++)
            {
               Component component = container.getComponents()[i];
               if (component == label)
               {
                  JLabel labelForLabel = (JLabel) container.getComponent(i - 1);
                  labelForLabel.setVisible(label.isVisible());
               }
            }
         }
      });
      return label;
   }

   public void setFocusOnName()
   {
      defaultFocusField.requestFocusInWindow();
   }
}
