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

package se.streamsource.streamflow.web.context.users;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.ChangePasswordDTO;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.account.AccountContext;
import se.streamsource.streamflow.web.context.account.ProfileContext;
import se.streamsource.streamflow.web.context.administration.UserContext;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.WrongPasswordException;

/**
 * JAVADOC
 */
public class UserContextTest
   extends ContextTest
{
   @BeforeClass
   public static void before() throws UnitOfWorkCompletionException
   {
      UsersContextTest.createUser( "test" );
      clearEvents();
   }

   @AfterClass
   public static void after() throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      uow.remove( uow.get( User.class, "test" ));
      uow.complete();
   }

   @Test
   public void testDisabled() throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.newCurrentRoleMap();
      playRole(User.class, "test");
      context(UserContext.class).changedisabled();
      uow.complete();
      eventsOccurred( "changedEnabled" );
   }

   @Test
   public void testChangePassword() throws UnitOfWorkCompletionException
   {
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole(User.class, "test");
         try
         {
            context( AccountContext.class).changepassword( value( ChangePasswordDTO.class, "{'oldPassword':'test','newPassword':'test2'}") );
         } catch (WrongPasswordException e)
         {
            Assert.fail( "Should have been able to change password" );
         }
         uow.complete();

         eventsOccurred( "changedPassword" );
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole(User.class, "test");
         try
         {
            context(AccountContext.class).changepassword( value( ChangePasswordDTO.class, "{'oldPassword':'test','newPassword':'test3'}") );
            Assert.fail( "Should not have been able to change password" );
         } catch (WrongPasswordException e)
         {
            // Ok
         }
         uow.complete();
         clearEvents();
      }

   }

   @Test
   public void testDeliveryType() throws UnitOfWorkCompletionException
   {
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole(User.class, "test");

         context(ProfileContext.class).changemessagedeliverytype(MessageRecipient.MessageDeliveryTypes.none);
         uow.complete();
         eventsOccurred( "changedMessageDeliveryType" );
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole(User.class, "test");

         context( ProfileContext.class).changemessagedeliverytype( MessageRecipient.MessageDeliveryTypes.email );
         uow.complete();
         eventsOccurred( );
      }

   }
}