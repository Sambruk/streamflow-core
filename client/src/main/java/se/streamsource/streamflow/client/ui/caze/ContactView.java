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

package se.streamsource.streamflow.client.ui.caze;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.contact.ContactAddressValue;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.resource.caze.ContactsDTO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTAREA;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTFIELD;

/**
 * JAVADOC
 */
public class ContactView
      extends JPanel
      implements Observer
{
   @Service
   DialogService dialogs;

   @Uses
   protected ObjectBuilder<ContactLookupResultDialog> contactLookupResultDialog;

   @Structure
   ValueBuilderFactory vbf;

   private StateBinder contactBinder;
   private StateBinder phoneNumberBinder;
   private StateBinder emailBinder;
   private StateBinder addressBinder;

   ContactModel model;

   private CardLayout layout = new CardLayout();
   public JPanel form;
   private JTextField defaultFocusField;
   private JTextField addressField = (JTextField) TEXTFIELD.newField();
   private JTextField phoneField = (JTextField) TEXTFIELD.newField();
   private JTextField emailField = (JTextField) TEXTFIELD.newField();
   private JTextField contactIdField = (JTextField) TEXTFIELD.newField();
   private JTextField companyField = (JTextField) TEXTFIELD.newField();

   private ApplicationContext context;

   public ContactView( @Service ApplicationContext appContext, @Structure ObjectBuilderFactory obf )
   {
      setLayout( layout );

      context = appContext;
      setActionMap( context.getActionMap( this ) );
      FormLayout formLayout = new FormLayout(
            "70dlu, 5dlu, 150dlu:grow",
            "pref, pref, pref, pref, pref, pref, 5dlu, top:70dlu:grow, pref, pref" );
      this.setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      form = new JPanel();
      JScrollPane scrollPane = new JScrollPane( form );
      scrollPane.getVerticalScrollBar().setUnitIncrement( 30 );
      scrollPane.setBorder( BorderFactory.createEmptyBorder() );
      DefaultFormBuilder builder = new DefaultFormBuilder( formLayout, form );

      contactBinder = obf.newObject( StateBinder.class );
      contactBinder.setResourceMap( context.getResourceMap( getClass() ) );
      ContactValue template = contactBinder.bindingTemplate( ContactValue.class );

      phoneNumberBinder = obf.newObject( StateBinder.class );
      phoneNumberBinder.setResourceMap( context.getResourceMap( getClass() ) );
      ContactPhoneValue phoneTemplate = phoneNumberBinder.bindingTemplate( ContactPhoneValue.class );

      addressBinder = obf.newObject( StateBinder.class );
      addressBinder.setResourceMap( context.getResourceMap( getClass() ) );
      ContactAddressValue addressTemplate = addressBinder.bindingTemplate( ContactAddressValue.class );

      emailBinder = obf.newObject( StateBinder.class );
      emailBinder.setResourceMap( context.getResourceMap( getClass() ) );
      ContactEmailValue emailTemplate = emailBinder.bindingTemplate( ContactEmailValue.class );

      builder.add( new JLabel( i18n.text( WorkspaceResources.name_label ) ) );
      builder.nextColumn( 2 );
      builder.add( contactBinder.bind( defaultFocusField = (JTextField) TEXTFIELD.newField(), template.name() ) );
      builder.nextLine();
      builder.add( new JLabel( i18n.text( WorkspaceResources.phone_label ) ) );
      builder.nextColumn( 2 );
      builder.add( phoneNumberBinder.bind( phoneField, phoneTemplate.phoneNumber() ) );
      builder.nextLine();
      builder.add( new JLabel( i18n.text( WorkspaceResources.address_label ) ) );
      builder.nextColumn( 2 );
      builder.add( addressBinder.bind( addressField, addressTemplate.address() ) );
      builder.nextLine();
      builder.add( new JLabel( i18n.text( WorkspaceResources.email_label ) ) );
      builder.nextColumn( 2 );
      builder.add( emailBinder.bind( emailField, emailTemplate.emailAddress() ) );
      builder.nextLine();
      builder.add( new JLabel( i18n.text( WorkspaceResources.contact_id_label ) ) );
      builder.nextColumn( 2 );
      builder.add( contactBinder.bind( contactIdField, template.contactId() ) );
      builder.nextLine();
      builder.add( new JLabel( i18n.text( WorkspaceResources.company_label ) ) );
      builder.nextColumn( 2 );
      builder.add( contactBinder.bind( companyField, template.company() ) );
      builder.nextLine( 2 );
      builder.add( new JLabel( i18n.text( WorkspaceResources.note_label ) ) );
      builder.nextColumn( 2 );
      builder.add( contactBinder.bind( TEXTAREA.newField(), template.note() ) );

      builder.nextLine( 2 );
      builder.nextColumn( 2 );
      JPanel lookupPanel = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
      lookupPanel.add( new JButton( getActionMap().get( "lookupContact" ) ) );
      builder.add( lookupPanel );

      contactBinder.addObserver( this );
      phoneNumberBinder.addObserver( this );
      addressBinder.addObserver( this );
      emailBinder.addObserver( this );

      add( new JLabel("FOO"), "EMPTY" );
      add( scrollPane, "CONTACT" );
   }


   public void setModel( ContactModel model )
   {
      this.model = model;
      if (model != null)
      {
         contactBinder.updateWith( model.getContact() );
         phoneNumberBinder.updateWith( model.getPhoneNumber() );
         addressBinder.updateWith( model.getAddress() );
         emailBinder.updateWith( model.getEmailAddress() );

         layout.show( this, "CONTACT" );

      } else
      {
         layout.show( this, "EMPTY" );
      }
   }

   public void update( Observable observable, Object arg )
   {
      final Property property = (Property) arg;
      new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            String propertyName = property.qualifiedName().name();
            if (propertyName.equals( "name" ))
            {
               model.changeName( (String) property.get() );
            } else if (propertyName.equals( "note" ))
            {
               model.changeNote( (String) property.get() );
            } else if (propertyName.equals( "company" ))
            {
               model.changeCompany( (String) property.get() );
            } else if (propertyName.equals( "phoneNumber" ))
            {
               model.changePhoneNumber( (String) property.get() );
            } else if (propertyName.equals( "address" ))
            {
               model.changeAddress( (String) property.get() );
            } else if (propertyName.equals( "emailAddress" ))
            {
               model.changeEmailAddress( (String) property.get() );
            } else if (propertyName.equals( "contactId" ))
            {
               model.changeContactId( (String) property.get() );
            }
         }
      }.execute();
   }

   @Action
   public void lookupContact()
   {
      try
      {
         ContactValue query = createContactQuery();

         ContactValue emptyCriteria = vbf.newValueBuilder( ContactValue.class ).newInstance();
         if (emptyCriteria.equals( query ))
         {
            String msg = i18n.text( CaseResources.could_not_find_search_criteria );
            dialogs.showMessageDialog( this, msg, "Info" );

         } else
         {

            ContactsDTO contacts = model.searchContacts( query );

            if (contacts.contacts().get().isEmpty())
            {
               String msg = i18n.text( CaseResources.could_not_find_contacts );
               dialogs.showMessageDialog( this, msg, "Info" );
            } else
            {

               ContactLookupResultDialog dialog = contactLookupResultDialog.use(
                     contacts.contacts().get() ).newInstance();
               dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), dialog, i18n.text( WorkspaceResources.contacts_tab ) );

               ContactValue contactValue = dialog.getSelectedContact();

               if (contactValue != null)
               {
                  if (defaultFocusField.getText().equals( "" ) && !contactValue.name().get().equals( "" ))
                  {
                     model.changeName( contactValue.name().get() );
                     defaultFocusField.setText( contactValue.name().get() );
                  }

                  for (ContactPhoneValue contactPhoneValue : contactValue.phoneNumbers().get())
                  {
                     if (!contactPhoneValue.phoneNumber().get().equals( "" ) && model.getPhoneNumber().phoneNumber().get().equals( "" ))
                     {
                        model.changePhoneNumber( contactPhoneValue.phoneNumber().get() );
                        phoneField.setText( contactPhoneValue.phoneNumber().get() );
                     }
                  }

                  List<ContactAddressValue> addressValues = contactValue.addresses().get();
                  for (ContactAddressValue addressValue : addressValues)
                  {
                     if (!addressValue.address().get().equals( "" ) && model.getAddress().address().get().equals( "" ))
                     {
                        model.changeAddress( addressValue.address().get() );
                        addressField.setText( addressValue.address().get() );
                     }
                  }

                  List<ContactEmailValue> emailValues = contactValue.emailAddresses().get();
                  for (ContactEmailValue emailValue : emailValues)
                  {
                     if (!emailValue.emailAddress().get().equals( "" ) && model.getEmailAddress().emailAddress().get().equals( "" ))
                     {
                        model.changeEmailAddress( emailValue.emailAddress().get() );
                        emailField.setText( emailValue.emailAddress().get() );
                     }
                  }

                  if (contactIdField.getText().equals( "" ) && !contactValue.contactId().get().equals( "" ))
                  {
                     model.changeContactId( contactValue.contactId().get() );
                     contactIdField.setText( contactValue.contactId().get() );
                  }

                  if (companyField.getText().equals( "" ) && !contactValue.company().get().equals( "" ))
                  {
                     model.changeCompany( contactValue.company().get() );
                     companyField.setText( contactValue.company().get() );
                  }
               }
            }
         }

      } catch (ResourceException e)
      {
         e.printStackTrace();
      }
   }

   private ContactValue createContactQuery()
   {
      ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder( ContactValue.class );

      if (!defaultFocusField.getText().isEmpty())
      {
         contactBuilder.prototype().name().set( defaultFocusField.getText() );
      }

      if (!phoneField.getText().isEmpty())
      {
         ValueBuilder<ContactPhoneValue> builder = vbf.newValueBuilder( ContactPhoneValue.class );
         builder.prototype().phoneNumber().set( phoneField.getText() );
         contactBuilder.prototype().phoneNumbers().get().add( builder.newInstance() );
      }

      if (!addressField.getText().isEmpty())
      {
         ValueBuilder<ContactAddressValue> builder = vbf.newValueBuilder( ContactAddressValue.class );
         builder.prototype().address().set( addressField.getText() );
         contactBuilder.prototype().addresses().get().add( builder.newInstance() );
      }

      if (!emailField.getText().isEmpty())
      {
         ValueBuilder<ContactEmailValue> builder = vbf.newValueBuilder( ContactEmailValue.class );
         builder.prototype().emailAddress().set( emailField.getText() );
         contactBuilder.prototype().emailAddresses().get().add( builder.newInstance() );
      }

      if (!contactIdField.getText().isEmpty())
      {
         contactBuilder.prototype().contactId().set( contactIdField.getText() );
      }

      if (!companyField.getText().isEmpty())
      {
         contactBuilder.prototype().contactId().set( contactIdField.getText() );
      }
      return contactBuilder.newInstance();
   }
}
