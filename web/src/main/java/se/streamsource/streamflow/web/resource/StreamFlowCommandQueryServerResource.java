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

import org.restlet.data.Reference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.composite.TransientBuilderFactory;
import se.streamsource.streamflow.dci.infrastructure.web.context.InteractionContext;
import se.streamsource.streamflow.dci.resource.DCICommandQueryServerResource;
import se.streamsource.streamflow.web.context.RootContext;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;

import java.util.Locale;
import java.util.List;
import java.security.Principal;

/**
 * The StreamFlow root context will
 * return the class RootContext defining the
 * root of the application
 */
public class StreamFlowCommandQueryServerResource
      extends DCICommandQueryServerResource
{

   @Structure
   TransientBuilderFactory tbf;

   protected RootContext getRoot()
   {
      InteractionContext interactionContext = new InteractionContext();
      interactionContext.playRoles( getActor(), Actor.class );
      interactionContext.playRoles( resolveRequestLocale(), Locale.class );
      interactionContext.playRoles( getRequest().getResourceRef(), Reference.class );
      return tbf.newTransientBuilder( RootContext.class ).use( interactionContext ).newInstance();
   }

   private Actor getActor()
   {
      List<Principal> principals = getRequest().getClientInfo().getPrincipals();
      if (!principals.isEmpty())
      {
         String userName = principals.get( 0 ).getName();
         return uowf.currentUnitOfWork().get( Actor.class, userName );
      } else
         return null;
   }

}