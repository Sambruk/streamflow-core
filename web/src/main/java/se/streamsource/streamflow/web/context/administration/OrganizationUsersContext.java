/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.administration;

import static se.streamsource.dci.api.RoleMap.role;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.NewUserDTO;
import se.streamsource.streamflow.api.administration.UserEntityDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersQueries;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.Users;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImporterService;

/**
 * JAVADOC
 */
public class OrganizationUsersContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      UsersQueries users = RoleMap.role( UsersQueries.class );

      ValueBuilder<LinksValue> listBuilder = module.valueBuilderFactory().newValueBuilder(LinksValue.class);
      List<LinkValue> userlist = listBuilder.prototype().links().get();

      ValueBuilder<UserEntityDTO> builder = module.valueBuilderFactory().newValueBuilder(UserEntityDTO.class);


      Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
      for (UserEntity user : users.users())
      {
         builder.prototype().href().set( user.toString() + "/" );
         builder.prototype().id().set( user.toString() );
         builder.prototype().text().set( user.userName().get() );
         builder.prototype().disabled().set( user.disabled().get() );
         builder.prototype().joined().set( user.organizations().contains( orgs.organization().get() ) );
         builder.prototype().rel().set( "user" );

         userlist.add( builder.newInstance() );
      }

      return listBuilder.newInstance();
   }

   @ServiceAvailable( service = LdapImporterService.class, availability = false )
   public void importusers( Representation representation)
   {
      boolean badRequest = false;
      String errors = "<html>";
      Locale locale = RoleMap.role( Locale.class );

      ResourceBundle bundle = ResourceBundle.getBundle(
            OrganizationUsersContext.class.getName(), locale );

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

               users.add( builder.toString() );
            }

         } else if (representation.getMediaType().equals( MediaType.TEXT_CSV ))
         {
            StringReader reader = new StringReader( representation.getText() );
            BufferedReader bufReader = new BufferedReader( reader );
            String line;
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

   @ServiceAvailable( service = LdapImporterService.class, availability = false )
   public void createuser( NewUserDTO DTO)
   {
      Users users = RoleMap.role( Users.class );
      User user = users.createUser( DTO.username().get(), DTO.password().get() );

      Organization org = role( Organization.class );
      user.join( org );
   }
}
