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

package se.streamsource.streamflow.web.application.security;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.SecretVerifier;
import se.streamsource.streamflow.web.domain.user.UserAuthentication;

/**
 * Accept login if user with the given username has the given password
 * in the StreamFlow user database.
 */
public class PasswordVerifierService
      extends SecretVerifier
{
   private static Usecase usecase = UsecaseBuilder.newUsecase( "Verify password" );

   @Structure
   UnitOfWorkFactory uowf;

   @Override
   public int verify( Request request, Response response )
   {
      int result = super.verify( request, response );

      return result;
   }

   public boolean verify( String username, char[] password )
   {
      UnitOfWork unitOfWork = uowf.newUnitOfWork( usecase );

      try
      {
         UserAuthentication user = unitOfWork.get( UserAuthentication.class, username );

         if (user.login( new String( password ) ))
         {
            unitOfWork.discard();
            return true;
         } else
         {
            try
            {
               // Save failed login count
               unitOfWork.complete();
            } catch (UnitOfWorkCompletionException e)
            {
               e.printStackTrace();
            }

            return false;
         }
      } catch (NoSuchEntityException e)
      {
         unitOfWork.discard();
         return false;
      }
   }
}