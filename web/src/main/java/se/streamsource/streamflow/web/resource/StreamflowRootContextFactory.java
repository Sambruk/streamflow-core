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

package se.streamsource.streamflow.web.resource;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.RootContextFactory;
import se.streamsource.streamflow.web.application.security.ProxyUserPrincipal;
import se.streamsource.streamflow.web.context.RootContext;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import javax.security.auth.Subject;

/**
 * The Streamflow root interactions will
 * return the class RootContext defining the
 * root of the application
 */
public class StreamflowRootContextFactory
   implements RootContextFactory
{
   @Structure
   TransientBuilderFactory tbf;

   @Structure
   UnitOfWorkFactory uowf;

   public Object getRoot( RoleMap roleMap )
   {

      String name = roleMap.get( Subject.class ).getPrincipals().iterator().next().getName();
      UserAuthentication authentication = uowf.currentUnitOfWork().get( UserAuthentication.class, name );
      roleMap.set(authentication);

      if ( authentication instanceof ProxyUser)
      {
         roleMap.get( Subject.class).getPrincipals().add( new ProxyUserPrincipal( name ));
      }

      return tbf.newTransientBuilder( RootContext.class ).use( roleMap ).newInstance();
   }
}
