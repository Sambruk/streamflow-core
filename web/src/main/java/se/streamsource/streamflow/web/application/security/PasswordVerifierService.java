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
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.security.*;
import org.restlet.security.UserPrincipal;
import se.streamsource.streamflow.web.domain.user.User;

import java.util.Date;

/**
 * Accept login if username==password
 */
public class PasswordVerifierService
        extends SecretVerifier
{
    @Structure
    UnitOfWorkFactory uowf;

    @Override
    public int verify(Request request, Response response)
    {
        int result =  super.verify(request, response);
        if (result == Verifier.RESULT_VALID)
        {
            LoginContext loginContext = new LoginContext("BASIC", new Date());
            request.getClientInfo().getSubject().getPublicCredentials().add(loginContext);
        }

        return result;
    }

    public boolean verify(String username, char[] password)
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();

        try
        {
            User user = unitOfWork.get(User.class, username);

            if (user.verifyPassword(new String(password)))
            {
                return true;
            } else
            {
                return false;
            }
        } catch (NoSuchEntityException e)
        {
            return false;
        } finally
        {
            unitOfWork.discard();
        }
    }
}