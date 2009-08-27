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

package se.streamsource.streamflow.web.resource.users.workspace.user.task.contacts;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/user/{view}/{task}/contacts/{index}
 */
public class TaskContactServerResource
        extends CommandQueryServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    Qi4jSPI spi;

    public TaskContactServerResource()
    {
        setNegotiated(true);
        getVariants().put(Method.ALL, MediaType.APPLICATION_JSON);
    }

    @Override
    protected Representation delete(Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Delete task contact"));

        TaskEntity task = uow.get(TaskEntity.class, getRequest().getAttributes().get("task").toString());
        String taskContactIndex = getRequest().getAttributes().get("index").toString();

        task.deleteContact(Integer.parseInt(taskContactIndex));

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            uow.discard();
        }
        return null;
    }

    /*
    @Override
    protected Representation put(Representation representation, Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Update task contacts"));
        try
        {
            ContactValue contact = vbf.newValueFromJSON(ContactValue.class, representation.getText());

            TaskEntity task = uow.get(TaskEntity.class, getRequest().getAttributes().get("task").toString());
            String taskContactIndex = getRequest().getAttributes().get("index").toString();

            ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder(ContactValue.class);
            contactBuilder.prototype().company().set(contact.company().get());
            contactBuilder.prototype().name().set(contact.name().get());
            contactBuilder.prototype().isCompany().set(contact.isCompany().get());
            contactBuilder.prototype().note().set(contact.note().get());
            contactBuilder.prototype().picture().set(contact.picture().get());

            contactBuilder.prototype().addresses().set(contact.addresses().get());
            contactBuilder.prototype().emailAddresses().set(contact.emailAddresses().get());
            contactBuilder.prototype().phoneNumbers().set(contact.phoneNumbers().get());
            ContactValue contactValue = contactBuilder.newInstance();

            task.updateContact(Integer.parseInt(taskContactIndex), contactValue);

            uow.complete();
        } catch (Exception e)
        {
            e.printStackTrace();
            uow.discard();
        }

        return null;
    }*/

    public void changeName(StringDTO name)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        TaskEntity task = uowf.currentUnitOfWork().get(TaskEntity.class, taskId);
        String taskContactIndex = getRequest().getAttributes().get("index").toString();

        int idx = Integer.parseInt(taskContactIndex);
        ContactValue contact = task.contacts().get().get(idx);

        ValueBuilder<ContactValue> builder = vbf.newValueBuilder(ContactValue.class).withPrototype(contact);
        builder.prototype().name().set(name.string().get());

        task.updateContact(idx, builder.newInstance());
    }

    public void changeNote(StringDTO note)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        TaskEntity task = uowf.currentUnitOfWork().get(TaskEntity.class, taskId);
        String taskContactIndex = getRequest().getAttributes().get("index").toString();

        int idx = Integer.parseInt(taskContactIndex);
        ContactValue contact = task.contacts().get().get(idx);

        ValueBuilder<ContactValue> builder = vbf.newValueBuilder(ContactValue.class).withPrototype(contact);
        builder.prototype().note().set(note.string().get());

        task.updateContact(idx, builder.newInstance());
    }

    public void changeCompany(StringDTO company)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        TaskEntity task = uowf.currentUnitOfWork().get(TaskEntity.class, taskId);

        String taskContactIndex = getRequest().getAttributes().get("index").toString();

        int idx = Integer.parseInt(taskContactIndex);
        ContactValue contact = task.contacts().get().get(idx);

        ValueBuilder<ContactValue> builder = vbf.newValueBuilder(ContactValue.class).withPrototype(contact);
        builder.prototype().company().set(company.string().get());

        task.updateContact(idx, builder.newInstance());
    }


    @Override
    protected String getConditionalIdentityAttribute()
    {
        return "task";
    }
}