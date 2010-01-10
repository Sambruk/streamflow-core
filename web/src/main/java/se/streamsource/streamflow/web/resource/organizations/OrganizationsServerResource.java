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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.ResetPasswordCommand;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;
import se.streamsource.streamflow.web.domain.structure.organizations.Organizations;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsQueries;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Mapped to /organizations.
 */
public class OrganizationsServerResource
      extends CommandQueryServerResource
{
   @Structure
   QueryBuilderFactory qbf;

   @Structure
   ValueBuilderFactory vbf;


   @Override
   protected Representation get() throws ResourceException
   {
      Form form = getRequest().getResourceRef().getQueryAsForm();
      if (form.getFirst( "findbyid" ) != null)
      {
         // Find organizations
         String id = form.getFirstValue( "id" );
         UnitOfWork uow = uowf.newUnitOfWork();
         try
         {
            Reference orgRef = getRequest().getResourceRef().clone().addSegment( id ).addSegment( "" );
            orgRef.setQuery( "" );
            getResponse().redirectPermanent( orgRef );
            return new EmptyRepresentation();
         } catch (NoSuchEntityException e)
         {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
         } finally
         {
            uow.discard();
         }
      } else
      {
         return new InputRepresentation( getClass().getResourceAsStream( "resources/organizationsearch.html" ), MediaType.TEXT_HTML );
      }
   }


   public UserEntityListDTO users()
   {
      OrganizationsQueries orgs = uowf.currentUnitOfWork().get( OrganizationsQueries.class, OrganizationsEntity.ORGANIZATIONS_ID );

      checkPermission( orgs );

      return orgs.users();
   }

   public void createuser( NewUserCommand userCommand ) throws ResourceException
   {
      Organizations organizations = uowf.currentUnitOfWork().get( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID );

      checkPermission( organizations );

      try
      {
         organizations.createUser( userCommand.username().get(), userCommand.password().get() );
      } catch (ConstraintViolationException cve)
      {
         throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, ErrorResources.username_password_cviolation.toString() );
      } catch (IllegalArgumentException iae)
      {
         throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, ErrorResources.user_already_exists.toString() );
      }
   }

   public void changedisabled( UserEntityDTO user )
   {
      UserEntity userEntity = uowf.currentUnitOfWork().get( UserEntity.class, user.entity().get().identity() );

      checkPermission( userEntity );

      userEntity.changeEnabled( userEntity.disabled().get() );
   }

   public void importusers( Representation representation ) throws ResourceException
   {
      boolean badRequest = false;
      String errors = "<html>";
      Locale locale = resolveRequestLocale();

      ResourceBundle bundle = ResourceBundle.getBundle(
            OrganizationsServerResource.class.getName(), locale );

      UnitOfWork uow = uowf.currentUnitOfWork();

      Organizations organizations = uow.get( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID );

      checkPermission( organizations );

      try
      {
         List<String> users = new ArrayList<String>();

         if (representation.getMediaType().equals( MediaType.APPLICATION_EXCEL ))
         {
            HSSFWorkbook workbook = new HSSFWorkbook( representation.getStream() );

            //extract a user list
            Sheet sheet1 = workbook.getSheetAt( 0 );
            StringBuilder builder;
            for (Row row : sheet1)
            {
               builder = new StringBuilder();
               builder.append( row.getCell( 0 ).getStringCellValue() );
               builder.append( "," );
               builder.append( row.getCell( 1 ).getStringCellValue() );

               ((List<String>) users).add( builder.toString() );
            }

         } else if (representation.getMediaType().equals( MediaType.TEXT_CSV ))
         {
            StringReader reader = new StringReader( representation.getText() );
            BufferedReader bufReader = new BufferedReader( reader );
            String line = null;
            while ((line = bufReader.readLine()) != null)
            {
               users.add( line );
            }
         } else
         {
            throw new ResourceException( Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE );
         }

         for (String userNamePwd : users)
         {
            if (userNamePwd.startsWith( "#" ))
            {
               continue;
            }
            Pattern pattern = Pattern.compile( "\\t|," );
            String[] usrPwdPair = userNamePwd.split( pattern.pattern() );

            if (usrPwdPair.length < 2)
            {
               badRequest = true;
               errors += userNamePwd + " - " + bundle.getString( "missing_user_password" ) + "<br></br>";
               continue;
            }

            String name = usrPwdPair[0].trim();
            String pwd = usrPwdPair[1].trim();

            // Check for empty pwd!!! and log an error for that
            if ("".equals( pwd.trim() ))
            {
               badRequest = true;
               errors += name + " - " + bundle.getString( "missing_password" ) + "<br></br>";
            }

            try
            {   // Check if user already exists
               UserEntity existingUser = uow.get( UserEntity.class, name );
               if (existingUser.isCorrectPassword( pwd ))
               {
                  //nothing to do here
                  continue;
               } else
               {
                  existingUser.resetPassword( pwd );
                  continue;
               }

            } catch (NoSuchEntityException e)
            {
               //Ok user doesnt exist
            }

            try
            {
               organizations.createUser( name, pwd );

            } catch (ConstraintViolationException e)
            {
               // catch constraint violation and collect errors for the entire transaction
               badRequest = true;
               errors += name + " - " + bundle.getString( "user_name_not_valid" ) + "<br></br>";
            }
         }
      } catch (IOException ioe)
      {
         throw new ResourceException( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
      }

      // Check for errors and rollback
      if (badRequest)
      {
         errors += "</html>";
         throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, errors );
      }

   }

   public ListValue organizations() throws ResourceException
   {
      OrganizationsQueries organizations = uowf.currentUnitOfWork()
            .get( OrganizationsQueries.class, OrganizationsEntity.ORGANIZATIONS_ID );

      checkPermission( organizations );

      return organizations.organizations();
   }

   public void resetpassword( ResetPasswordCommand command )
   {
      UserEntity userEntity = uowf.currentUnitOfWork().get( UserEntity.class, command.entity().get().identity() );

      checkPermission( userEntity );

      userEntity.resetPassword( command.password().get() );
   }
}
