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

package se.streamsource.streamflow.web.context.administration;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.unitofwork.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.resource.user.*;
import se.streamsource.streamflow.util.*;
import se.streamsource.streamflow.web.domain.entity.user.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

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
               if( !Strings.empty( line ))
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
