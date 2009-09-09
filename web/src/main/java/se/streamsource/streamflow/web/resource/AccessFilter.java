/**
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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
package se.streamsource.streamflow.web.resource;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import javax.security.auth.Subject;
import java.util.List;

public class AccessFilter extends Filter
{

    @Structure
    UnitOfWorkFactory uowf;

    @Override
    protected int beforeHandle(Request request, Response response)
    {
        List<String> segments = request.getResourceRef().getSegments();
        int idx;
        if (-1 != (idx = segments.indexOf("users")) && segments.size()>idx+1)
        {
            String userId = segments.get(idx+1);
            UnitOfWork uow = uowf.newUnitOfWork();
            UserEntity user = uow.get(UserEntity.class, userId);

            Subject subject = request.getClientInfo().getSubject();

            //LoginContext loginContext = subject.getPublicCredentials(LoginContext.class).iterator().next();

            //UserPrincipal userPrincipal = subject.getPrincipals(UserPrincipal.class).iterator().next();
            //UserEntity loggedInUser = uow.get(UserEntity.class, userId);
            //if (!user.identity().get().equals(loggedInUser.identity().get()))
            //{
            //    response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            //}

            uow.discard();
        }
        return super.beforeHandle(request, response);
    }
}
