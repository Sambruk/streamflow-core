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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.rest.ExtensionMediaTypeFilter;
import org.qi4j.rest.entity.EntitiesResource;
import org.qi4j.rest.entity.EntityResource;
import org.qi4j.rest.query.IndexResource;
import org.qi4j.rest.query.SPARQLResource;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;
import org.restlet.security.ChallengeAuthenticator;
import se.streamsource.dci.restlet.server.CommandQueryRestlet;
import se.streamsource.dci.restlet.server.ResourceFinder;
import se.streamsource.streamflow.web.resource.admin.ConsoleServerResource;
import se.streamsource.streamflow.web.resource.events.EventsServerResource;

/**
 * Router for v1 of the StreamFlow REST API.
 */
public class APIv1Router
      extends Router
{
   private ObjectBuilderFactory factory;

   public APIv1Router( @Uses Context context, @Structure ObjectBuilderFactory factory ) throws Exception
   {
      super( context );
      this.factory = factory;

      Restlet cqr = factory.newObjectBuilder( CommandQueryRestlet.class ).use(context).newInstance();

      Authenticator auth = new ChallengeAuthenticator( getContext(), ChallengeScheme.HTTP_BASIC, "StreamFlow" );
      auth.setNext( cqr );

      attach(new ExtensionMediaTypeFilter( getContext(), auth));

/*
      attach(new ExtensionMediaTypeFilter( getContext(), factory.newObjectBuilder( EventsCommandResult.class).use(getContext(),
            factory.newObjectBuilder( ViewFilter.class).use(getContext(), createServerResourceFinder( StreamFlowRootContextFactory.class )).newInstance()).newInstance()));
*/
      // Events
      attach( "/events", createServerResourceFinder( EventsServerResource.class ) );

      // Qi4j
      Router qi4jRouter = new Router( getContext() );
      qi4jRouter.attach( "/entity", createServerResourceFinder( EntitiesResource.class ) );
      qi4jRouter.attach( "/entity/{identity}", createServerResourceFinder( EntityResource.class ) );
      qi4jRouter.attach( "/query", createServerResourceFinder( SPARQLResource.class ) );
      qi4jRouter.attach( "/query/index", createServerResourceFinder( IndexResource.class ) );
      attach( "/qi4j", new ExtensionMediaTypeFilter( getContext(), qi4jRouter ) );


      attach( "/admin/console", createServerResourceFinder( ConsoleServerResource.class ) );

      // Version info
      Directory directory = new Directory( getContext(), "clap://class/static/" );
      directory.setListingAllowed( true );
      attach( "/static", directory );
   }

   private Restlet createServerResourceFinder( Class<? extends ServerResource> resource )
   {
      return createServerResourceFinder( resource, true );
   }

   private Restlet createServerResourceFinder( Class<? extends ServerResource> resource, boolean secure )
   {
      ResourceFinder finder = factory.newObject( ResourceFinder.class );
      finder.setTargetClass( resource );

      if (secure)
      {
         Authenticator auth = new ChallengeAuthenticator( getContext(), ChallengeScheme.HTTP_BASIC, "StreamFlow" );
         auth.setNext( finder );
         return auth;
      } else
         return finder;
   }
}
