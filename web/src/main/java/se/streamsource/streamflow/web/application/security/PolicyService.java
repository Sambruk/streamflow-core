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

import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import java.security.*;

import org.restlet.security.UserPrincipal;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.mixin.Mixins;

import javax.security.auth.Subject;

/**
 * JAVADOC
 */
@Mixins(PolicyService.PolicyServiceMixin.class)
public interface PolicyService
    extends AccessPolicy, ServiceComposite
{
    class PolicyServiceMixin
        implements AccessPolicy
    {
        @Structure
        UnitOfWorkFactory uowf;

        public AccessControlContext getAccessControlContext(Subject subject, Object securedObject)
        {
            // TODO Actually use the context for policy decisions
            LoginContext loginContext = subject.getPublicCredentials(LoginContext.class).iterator().next();

            UserPrincipal userPrincipal = subject.getPrincipals(UserPrincipal.class).iterator().next();
            UserEntity userEntity = uowf.currentUnitOfWork().get(UserEntity.class, userPrincipal.getName());

            PermissionCollection permissions = null;
            if (userEntity.isAdministrator())
            {
                permissions = new AllPermission().newPermissionCollection();
                permissions.add(new AllPermission());
            } else
            {
                // TODO - Calculate permissions for use
            }

            Principal[] principals = new Principal[]{};
            ProtectionDomain[] domains = new ProtectionDomain[] {new ProtectionDomain(null, permissions, securedObject.getClass().getClassLoader(), principals)};


            return new AccessControlContext(domains);
        }
    }
}
