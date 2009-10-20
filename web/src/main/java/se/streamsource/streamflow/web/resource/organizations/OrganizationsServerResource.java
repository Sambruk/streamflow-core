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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.*;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;
import se.streamsource.streamflow.web.domain.organization.Organizations;
import se.streamsource.streamflow.web.domain.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

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
        boolean badRequest = false;
        String errors = "<html>";
        Locale locale = resolveRequestLocale();

        ResourceBundle bundle = ResourceBundle.getBundle(
					OrganizationsServerResource.class.getName(), locale);

        UnitOfWork uow = uowf.currentUnitOfWork();

        try
        {
            Iterable<String> users = new ArrayList<String>();

            if(representation.getMediaType().equals(MediaType.APPLICATION_EXCEL))
            {
                HSSFWorkbook workbook = new HSSFWorkbook(representation.getStream());

                //extract a user list
                Sheet sheet1 = workbook.getSheetAt(0);
                StringBuilder builder;
                for (Row row : sheet1)
                {   builder = new StringBuilder();
                    builder.append(row.getCell(0).getStringCellValue());
                    builder.append(",");
                    builder.append(row.getCell(1).getStringCellValue());

                    ((List<String>)users).add(builder.toString());
                }

            } else if(representation.getMediaType().equals(MediaType.TEXT_CSV))
            {
                users = Arrays.asList(representation.getText().split(System.getProperty("line.separator")));

            } else
            {
                throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            }

            for(String userNamePwd : users)
            {
                if(userNamePwd.startsWith("#"))
                {
                    continue;
                }
                Pattern pattern = Pattern.compile("\\t|,");
                String[] usrPwdPair = userNamePwd.split(pattern.pattern());

                if(usrPwdPair.length < 2)
                {
                    badRequest = true;
                    errors += userNamePwd + " - " + bundle.getString("missing_user_password") + "<br></br>";
                    continue;
                }

                String name = usrPwdPair[0].trim();
                String pwd = usrPwdPair[1].trim();

                // Check for empty pwd!!! and logg an error for that
                if(pwd == null || "".equals(pwd.trim()))
                {
                    badRequest = true;
                    errors += name + " - " + bundle.getString("missing_password") + "<br></br>";
                }

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
                    // catch constraint violation and collect errors for the entire transaction
                    badRequest = true;
                    errors += name + " - " + bundle.getString("user_name_not_valid") + "<br></br>";
                }
             }
        } catch(IOException ioe)
        {
            throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        }

        // Check for errors and rollback
        if(badRequest)
        {
            errors += "</html>";
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, errors);
        }

    }

    private Locale resolveRequestLocale()
    {
        Language language = getRequest().getClientInfo().getAcceptedLanguages()
                .get(0).getMetadata();
        String[] localeStr = language.getName().split("_");

        Locale locale;
        switch(localeStr.length)
        {
            case 1:
                locale = new Locale(localeStr[0]);
                break;
            case 2:
                locale = new Locale(localeStr[0], localeStr[1]);
                break;
            case 3:
                locale = new Locale(localeStr[0], localeStr[1], localeStr[2]);
                break;
            default:
                locale = Locale.getDefault();
        }
        return locale;
    }
}
