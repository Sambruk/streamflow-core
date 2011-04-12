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

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.query.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import se.streamsource.streamflow.domain.contact.*;
import se.streamsource.streamflow.domain.user.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.application.mail.*;
import se.streamsource.streamflow.web.domain.entity.user.*;

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
         contacts.contact().set( vbf.newValue( ContactValue.class ) );
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
         ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder(ContactValue.class);
         contactBuilder.prototype().name().set(email.fromName().get());

         ValueBuilder<ContactEmailValue> emailBuilder = vbf.newValueBuilder(ContactEmailValue.class);
         emailBuilder.prototype().emailAddress().set(email.from().get());

         contactBuilder.prototype().emailAddresses().get().add(emailBuilder.newInstance());

         user.updateContact(contactBuilder.newInstance());

         user.changeDescription(email.fromName().get());

         return user;
      }

      public EmailUserEntity createdEmailUser(@Optional DomainEvent event, String email)
      {
         EntityBuilder<EmailUserEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( EmailUserEntity.class, "email:"+email );
         builder.instance().contact().set(vbf.newValue(ContactValue.class));

         return builder.newInstance();
      }
   }
}