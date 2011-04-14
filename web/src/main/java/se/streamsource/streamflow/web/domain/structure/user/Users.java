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

package se.streamsource.streamflow.web.domain.structure.user;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.api.Password;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.Username;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.domain.entity.user.EmailUserEntity;

/**
 * JAVADOC
 */
@Mixins(Users.Mixin.class)
public interface Users
{
   /**
    * Create user with the given password. Username has a constraint that allows the
    * username only to be a whole word, because it will be used as part of the REST url.
    *
    * @param username of the new user
    * @param password of the new user
    * @return the created user
    * @throws IllegalArgumentException if user with given name already exists
    */
   User createUser( @Username String username, @Password String password )
         throws IllegalArgumentException;

   EmailUserEntity createEmailUser (EmailValue email);

   interface Data
   {
      User createdUser( @Optional DomainEvent event, String username, String password );

      EmailUserEntity createdEmailUser(@Optional DomainEvent event, String email);
   }

   abstract class Mixin
         implements Users, Data
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Service
      IdentityGenerator idGen;

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      public User createUser( String username, String password )
            throws IllegalArgumentException
      {
         // Check if user already exist
         try
         {
            uowf.currentUnitOfWork().get( User.class, username );

            throw new IllegalArgumentException( "user_already_exists" );
         } catch (NoSuchEntityException e)
         {
            // Ok!
         }

         User user = createdUser( null, username, password );
         return user;
      }

      public User createdUser( DomainEvent event, String username, String password )
      {
         EntityBuilder<User> builder = uowf.currentUnitOfWork().newEntityBuilder( User.class, username );
         UserAuthentication.Data userEntity = builder.instanceFor( UserAuthentication.Data.class );
         userEntity.userName().set( username );
         userEntity.hashedPassword().set( userEntity.hashPassword( password ) );
         Contactable.Data contacts = builder.instanceFor( Contactable.Data.class );
         contacts.contact().set( vbf.newValue( ContactDTO.class ) );
         return builder.newInstance();
      }

      public EmailUserEntity createEmailUser(EmailValue email)
              throws IllegalArgumentException
      {
         // Check if user already exist
         EmailUserEntity user;
         try
         {
            user = uowf.currentUnitOfWork().get( EmailUserEntity.class, "email:"+email.from().get() );
         } catch (NoSuchEntityException e)
         {
            // Create new email user
            user = createdEmailUser(null, email.from().get());
         }

         // Update contact info
         ValueBuilder<ContactDTO> contactBuilder = vbf.newValueBuilder(ContactDTO.class);
         contactBuilder.prototype().name().set(email.fromName().get());

         ValueBuilder<ContactEmailDTO> emailBuilder = vbf.newValueBuilder(ContactEmailDTO.class);
         emailBuilder.prototype().emailAddress().set(email.from().get());

         contactBuilder.prototype().emailAddresses().get().add(emailBuilder.newInstance());

         user.updateContact(contactBuilder.newInstance());

         user.changeDescription(email.fromName().get());

         return user;
      }

      public EmailUserEntity createdEmailUser(@Optional DomainEvent event, String email)
      {
         EntityBuilder<EmailUserEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( EmailUserEntity.class, "email:"+email );
         builder.instance().contact().set(vbf.newValue(ContactDTO.class));

         return builder.newInstance();
      }
   }
}