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

package se.streamsource.streamflow.web.resource.organizations;

import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.isNotNull;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.organization.Organizations;
import se.streamsource.streamflow.web.domain.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Mapped to /organizations/{id}.
 */
public class OrganizationsServerResource
        extends CommandQueryServerResource
{
    @Structure
    QueryBuilderFactory qbf;


    @Override
    protected Representation get() throws ResourceException
    {
        Form form = getRequest().getResourceRef().getQueryAsForm();
        if (form.getFirst("findbyid") != null)
        {
            // Find organizations
            String id = form.getFirstValue("id");
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                OrganizationalUnits org = uow.get(OrganizationalUnits.class, id);
                Reference orgRef = getRequest().getResourceRef().clone().addSegment(id).addSegment("");
                orgRef.setQuery("");
                getResponse().redirectPermanent(orgRef);
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
            return new InputRepresentation(getClass().getResourceAsStream("resources/organizationsearch.html"), MediaType.TEXT_HTML);
        }
    }


    public UserEntityListDTO users()
    {
        QueryBuilder<UserEntity> queryBuilder = qbf.newQueryBuilder(UserEntity.class);

        Property<String> username = templateFor(User.UserState.class).userName();
        Query<UserEntity> usersQuery = queryBuilder.where(
                isNotNull(username)).
                newQuery(uowf.currentUnitOfWork());

        usersQuery.orderBy(orderBy(templateFor(User.UserState.class).userName()));

        ValueBuilder<UserEntityListDTO> listBuilder = vbf.newValueBuilder(UserEntityListDTO.class);
        List<UserEntityDTO> userlist = listBuilder.prototype().users().get();

        ValueBuilder<UserEntityDTO> builder = vbf.newValueBuilder(UserEntityDTO.class);

        for (UserEntity entity : usersQuery)
        {
            builder.prototype().entity().set(EntityReference.getEntityReference(entity));
            builder.prototype().username().set(entity.userName().get());
            builder.prototype().disabled().set(entity.disabled().get());

            userlist.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }

    public void createUser(NewUserCommand userCommand)
    {
        OrganizationsEntity organizations = uowf.currentUnitOfWork().get(OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID);

        organizations.createUser(userCommand.username().get(), userCommand.password().get());
    }

    public void changeDisabled(UserEntityDTO user)
    {
        UserEntity userEntity = uowf.currentUnitOfWork().get(UserEntity.class, user.entity().get().identity());

        userEntity.changeEnabled(userEntity.disabled().get());
    }

    public void importUsers(Representation representation) throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        try
        {   Iterable<String> users = new ArrayList<String>();
            if(representation.getMediaType().equals(MediaType.APPLICATION_EXCEL))
            {
              // TODO: Exel conversion to CSV - this is not working due to POI gets IOException wrong header

                HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(representation.getText().getBytes()));
                ExcelExtractor extractor = new ExcelExtractor(workbook);

                extractor.setFormulasNotResults(true);
                extractor.setIncludeSheetNames(false);
                String text = extractor.getText();

                

            } else if(representation.getMediaType().equals(MediaType.TEXT_ALL))
            {
                users = Arrays.asList(representation.getText().split(System.getProperty("line.separator")));

            } else
            {
                throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            }

            for(Iterator<String> iter = users.iterator();iter.hasNext();)
            {
                String userNamePwd = iter.next();
                String[] tmp = userNamePwd.split("\t");
                String name = tmp[0].trim();
                String pwd = tmp[1].trim();

                try
                {   // Check if user already exists
                    UserEntity existingUser = uow.get(UserEntity.class, name);
                    if(existingUser.isCorrectPassword(pwd))
                    {
                        //nothing to do here
                        continue;
                    } else
                    {
                        existingUser.resetPassword(pwd);
                        continue;
                    }

                } catch (NoSuchEntityException e)
                {
                    //Ok user doesnt exist
                }

                try
                {
                    uow.get(Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID).createUser(name, pwd);

                } catch (ConstraintViolationException e)
                {
                    //TODO build a representation of Violation errors for return to the client
                    e.printStackTrace();
                }
             }
        } catch(IOException ioe)
        {
            throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        }

    }
}
