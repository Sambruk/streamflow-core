/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
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
import se.streamsource.dci.api.Context;
import se.streamsource.dci.restlet.server.RootInteractionsFactory;
import se.streamsource.streamflow.web.context.RootContext;
import se.streamsource.streamflow.web.domain.structure.user.User;

import javax.security.auth.Subject;

/**
 * The StreamFlow root interactions will
 * return the class RootInteractions defining the
 * root of the application
 */
public class StreamFlowRootContextFactory
   implements RootInteractionsFactory
{
   @Structure
   TransientBuilderFactory tbf;

   @Structure
   UnitOfWorkFactory uowf;

   public Object getRoot( Context context )
   {
      context.set(uowf.currentUnitOfWork().get( User.class, context.get( Subject.class ).getPrincipals().iterator().next().getName()));
      
      return tbf.newTransientBuilder( RootContext.class ).use( context ).newInstance();
   }
}
