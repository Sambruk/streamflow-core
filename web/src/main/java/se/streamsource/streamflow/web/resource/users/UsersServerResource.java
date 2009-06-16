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

package se.streamsource.streamflow.web.resource.users;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.*;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.resource.user.RegisterUserCommand;
import se.streamsource.streamflow.web.domain.organization.Organization;
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to /users
 */
public class UsersServerResource
        extends CommandQueryServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        Form form = getRequest().getResourceRef().getQueryAsForm();
        if (form.getFirst("findbyusername") != null)
        {
            // Find users
            String id = form.getFirstValue("username");
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                UserEntity user = uow.get(UserEntity.class, id);
                Reference userRef = getRequest().getResourceRef().clone().addSegment(id).addSegment("");
                userRef.setQuery("");
                getResponse().redirectPermanent(userRef);
                return new EmptyRepresentation();
            } catch (NoSuchEntityException e)
            {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
            } finally
            {
                uow.discard();
            }
        } else
        {
            return new InputRepresentation(getClass().getResourceAsStream("resources/usersearch.html"), MediaType.TEXT_HTML);
        }
    }


    @Override
    protected Representation post(Representation representation, Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Register user"));

        try
        {
            RegisterUserCommand registerUser = vbf.newValueFromJSON(RegisterUserCommand.class, representation.getText());
            // Create users
            EntityBuilder<UserEntity> userBuilder = uow.newEntityBuilder(UserEntity.class, registerUser.username().get());
            UserEntity userState = userBuilder.prototype();
            userState.userName().set(registerUser.username().get());

            ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder(ContactValue.class);
            contactBuilder.prototype().name().set(registerUser.contact().get().name().get());

            ContactValue contact = contactBuilder.newInstance();
            userState.contact().set(contact);

            // Lookup the bootstrap organization
            Organization org = uow.get(OrganizationEntity.class, "Organization");
            // Join the organization
            userState.join(org);
            UserEntity user = userBuilder.newInstance();

            setLocationRef("users/"+user.identity().get());
            uow.complete();
        } catch (Exception e)
        {
            uow.discard();
        }
        return null;
    }
}
