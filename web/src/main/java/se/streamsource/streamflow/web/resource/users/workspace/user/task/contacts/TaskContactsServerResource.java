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
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.contact.Contacts;
import se.streamsource.streamflow.resource.task.TaskContactDTO;
import se.streamsource.streamflow.resource.task.TaskContactsDTO;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.BaseServerResource;

import java.util.List;

/**
 * Mapped to:
 * /users/{user}/workspace/user/{view}/{task}/contacts
 */
public class TaskContactsServerResource
        extends BaseServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    Qi4jSPI spi;

    public TaskContactsServerResource()
    {
        setNegotiated(true);
        getVariants().put(Method.ALL, MediaType.APPLICATION_JSON);
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Get task contacts"));

        ValueBuilder<TaskContactsDTO> builder = vbf.newValueBuilder(TaskContactsDTO.class);
        ValueBuilder<TaskContactDTO> contactBuilder = vbf.newValueBuilder(TaskContactDTO.class);
        List<TaskContactDTO> list = builder.prototype().contacts().get();

        String clientETag = null;
        if (getRequest().getAttributes().get("If-None-Match") != null)
        {
            clientETag = getRequest().getAttributes().get("If-None-Match").toString();
        }

        TaskEntity task = uow.get(TaskEntity.class, getRequest().getAttributes().get("task").toString());

        String eTag = spi.getEntityState(task).version();
        if (eTag.equals(clientETag))
        {
            uow.discard();

            getResponse().setStatus(Status.REDIRECTION_NOT_MODIFIED);

            return null;
        } else
        {
            getResponse().getAttributes().put("ETag", eTag);
            for (ContactValue contact : task.contacts().get())
            {
                contactBuilder.prototype().company().set(contact.company().get());
                contactBuilder.prototype().name().set(contact.name().get());
                contactBuilder.prototype().isCompany().set(contact.isCompany().get());
                contactBuilder.prototype().note().set(contact.note().get());
                contactBuilder.prototype().picture().set(contact.picture().get());

                contactBuilder.prototype().addresses().set(contact.addresses().get());
                contactBuilder.prototype().emailAddresses().set(contact.emailAddresses().get());
                contactBuilder.prototype().phoneNumbers().set(contact.phoneNumbers().get());
                list.add(contactBuilder.newInstance());
            }
            uow.discard();

            return new StringRepresentation(builder.newInstance().toJSON(), MediaType.APPLICATION_JSON);
        }
    }


    @Override
    protected Representation post(Representation representation, Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Add new task contact"));

        Contacts contacts = uow.get(Contacts.class, getRequest().getAttributes().get("task").toString());
        contacts.addContact();

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            uow.discard();
        }
        return null;
    }
}