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

package se.streamsource.streamflow.web.resource;

import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.data.Reference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.composite.TransientBuilderFactory;
import se.streamsource.dci.context.InteractionContext;
import se.streamsource.dci.restlet.server.RootContextFactory;
import se.streamsource.streamflow.web.context.RootContext;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.structure.user.User;

import javax.security.auth.Subject;
import java.util.Locale;
import java.util.List;
import java.security.Principal;

/**
 * The StreamFlow root context will
 * return the class RootContext defining the
 * root of the application
 */
public class StreamFlowRootContextFactory
   implements RootContextFactory
{
   @Structure
   TransientBuilderFactory tbf;

   @Structure
   UnitOfWorkFactory uowf;

   public Object getRoot( InteractionContext context )
   {
      context.playRoles(uowf.currentUnitOfWork().get( User.class, context.role( Subject.class ).getPrincipals().iterator().next().getName()));
      
      return tbf.newTransientBuilder( RootContext.class ).use( context ).newInstance();
   }
}
