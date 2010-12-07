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

package se.streamsource.streamflow.web.context.administration;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersQueries;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.Users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * JAVADOC
 */
public class UsersContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      UsersQueries orgs = RoleMap.role( UsersQueries.class );

      return orgs.users();
   }

   public void createuser( NewUserCommand command )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      Users users = RoleMap.role( Users.class );
      User user = users.createUser( command.username().get(), command.password().get() );
   }

   public void importusers( Representation representation ) throws ResourceException
   {
      boolean badRequest = false;
      String errors = "<html>";
      Locale locale = RoleMap.role( Locale.class );

      ResourceBundle bundle = ResourceBundle.getBundle(
            UsersContext.class.getName(), locale );

      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      Users organizations = RoleMap.role( Users.class );

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
               if( Strings.notEmpty( line ))
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
}
