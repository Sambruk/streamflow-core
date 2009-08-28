/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.workspace;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTAREA;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTFIELD;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.domain.contact.ContactAddressValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class TaskContactView
        extends JPanel
    implements Observer
{
    private StateBinder contactBinder;
    private StateBinder phoneNumberBinder;
    private StateBinder emailBinder;
    private StateBinder addressBinder;

    TaskContactModel model;

    public ValueBuilder<ContactValue> valueBuilder;
    private CardLayout layout = new CardLayout();

    public TaskContactView(@Service ApplicationContext appContext)
    {
        setLayout(layout);

        setActionMap(appContext.getActionMap(this));
        FormLayout formLayout = new FormLayout(
                "200dlu",
                "");
        JPanel form = new JPanel();
        JScrollPane scrollPane = new JScrollPane(form);
        DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, form);
        builder.setDefaultDialogBorder();

        

        contactBinder = new StateBinder();
        contactBinder.setResourceMap(appContext.getResourceMap(getClass()));
        ContactValue template = contactBinder.bindingTemplate(ContactValue.class);

        phoneNumberBinder = new StateBinder();
        phoneNumberBinder.setResourceMap(appContext.getResourceMap(getClass()));
        ContactPhoneValue phoneTemplate =  phoneNumberBinder.bindingTemplate(ContactPhoneValue.class);

        addressBinder = new StateBinder();
        addressBinder.setResourceMap(appContext.getResourceMap(getClass()));
        ContactAddressValue addressTemplate = addressBinder.bindingTemplate(ContactAddressValue.class);

        emailBinder = new StateBinder();
        emailBinder.setResourceMap(appContext.getResourceMap(getClass()));
        ContactEmailValue emailTemplate = emailBinder.bindingTemplate(ContactEmailValue.class);

        BindingFormBuilder bb = new BindingFormBuilder(builder, contactBinder);
        bb
        .appendLine(WorkspaceResources.name_label, TEXTFIELD, template.name())
        .appendLine(WorkspaceResources.phone_label, BindingFormBuilder.Fields.FORMATTEDTEXTFIELD, phoneTemplate.phoneNumber(), phoneNumberBinder)
        .appendLine(WorkspaceResources.address_label, TEXTFIELD, addressTemplate.address(), addressBinder)
        .appendLine(WorkspaceResources.email_label, TEXTFIELD, emailTemplate.emailAddress(), emailBinder)
        .appendLine(WorkspaceResources.company_label, TEXTFIELD, template.company())
        .appendLine(WorkspaceResources.note_label, TEXTAREA, template.note());


        contactBinder.addObserver(this);
        phoneNumberBinder.addObserver(this);
        addressBinder.addObserver(this);
        emailBinder.addObserver(this);

        add(new JPanel(), "EMPTY");
        add(scrollPane, "CONTACT");
    }


    public void setModel(TaskContactModel model)
    {
        this.model = model;
        if (model != null)
        {
            contactBinder.updateWith(model.getContact());
            phoneNumberBinder.updateWith(model.getPhoneNumber());
            addressBinder.updateWith(model.getAddress());
            emailBinder.updateWith(model.getEmailAddress());

            layout.show(this, "CONTACT");
        } else
        {
            layout.show(this, "EMPTY");
        }



    }

    public void update(Observable observable, Object arg)
    {
        Property property = (Property) arg;
        if (property.qualifiedName().name().equals("name"))
        {
            try
            {
                model.changeName((String) property.get());
            } catch (ResourceException e)
            {
                throw new OperationException(WorkspaceResources.could_not_change_name, e);
            }
        } else if (property.qualifiedName().name().equals("note"))
        {
            try
            {
                model.changeNote((String) property.get());
            } catch (ResourceException e)
            {
                throw new OperationException(WorkspaceResources.could_not_change_note, e);
            }
        } else if (property.qualifiedName().name().equals("company"))
        {
            try
            {
                model.changeCompany((String) property.get());
            } catch (ResourceException e)
            {
                throw new OperationException(WorkspaceResources.could_not_change_company, e);
            }
        } else if (property.qualifiedName().name().equals("phoneNumber"))
        {
            try
            {
                model.changePhoneNumber((String) property.get());   
            } catch (ResourceException e)
            {
               throw new OperationException(WorkspaceResources.could_not_change_phone_number, e);
            }
        } else if (property.qualifiedName().name().equals("address"))
        {
            try
            {
                model.changeAddress((String) property.get());
            } catch (ResourceException e)
            {
               throw new OperationException(WorkspaceResources.could_not_change_address, e);
            }
        } else if (property.qualifiedName().name().equals("emailAddress"))
        {
            try
            {
                model.changeEmailAddress((String) property.get());
            } catch (ResourceException e)
            {
               throw new OperationException(WorkspaceResources.could_not_change_email_address, e);
            }
        }
    }
}
