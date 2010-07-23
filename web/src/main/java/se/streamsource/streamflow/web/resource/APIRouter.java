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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.rest.ExtensionMediaTypeFilter;
import org.qi4j.rest.entity.EntitiesResource;
import org.qi4j.rest.entity.EntityResource;
import org.qi4j.rest.query.IndexResource;
import org.qi4j.rest.query.SPARQLResource;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.Authenticator;
import org.restlet.security.ChallengeAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.restlet.server.CommandQueryRestlet;
import se.streamsource.dci.restlet.server.ResourceFinder;
import se.streamsource.streamflow.web.resource.admin.ConsoleServerResource;
import se.streamsource.streamflow.web.resource.events.DomainEventsServerResource;

/**
 * Router for the Streamflow REST API.
 */
public class APIRouter
      extends Router
{
   private ObjectBuilderFactory factory;

   public APIRouter( @Uses Context context, @Structure ObjectBuilderFactory factory ) throws Exception
   {
      super( context );
      this.factory = factory;

      Restlet cqr = factory.newObjectBuilder( CommandQueryRestlet.class ).use(context).newInstance();

      Filter performanceLoggingFilter = new PerformanceLoggingFilter( context, cqr );

      Authenticator auth = new ChallengeAuthenticator( getContext(), ChallengeScheme.HTTP_BASIC, "Streamflow" );
      auth.setNext( performanceLoggingFilter );

      attachDefault( new ExtensionMediaTypeFilter( getContext(), auth) );

      // Events
      attach( "/events/domain", new ExtensionMediaTypeFilter( getContext(), createServerResourceFinder( DomainEventsServerResource.class )), Template.MODE_STARTS_WITH );

      // Admin resources
      Router adminRouter = new Router( getContext() );
      adminRouter.attach( "/entity", createServerResourceFinder( EntitiesResource.class ) );
      adminRouter.attach( "/entity/{identity}", createServerResourceFinder( EntityResource.class ) );
      adminRouter.attach( "/query", createServerResourceFinder( SPARQLResource.class ), Template.MODE_STARTS_WITH );
      adminRouter.attach( "/index", createServerResourceFinder( IndexResource.class ) );
      adminRouter.attach( "/console", createServerResourceFinder( ConsoleServerResource.class ) );
      attach( "/admin", new ExtensionMediaTypeFilter( getContext(), adminRouter ) );



      // Version info
      Directory directory = new Directory( getContext(), "clap://thread/static/" );
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
         Authenticator auth = new ChallengeAuthenticator( getContext(), ChallengeScheme.HTTP_BASIC, "Streamflow" );
         auth.setNext( finder );
         return auth;
      } else
         return finder;
   }

   private static class PerformanceLoggingFilter extends Filter
   {
      Logger queryPerformanceMonitor;
      Logger commandPerformanceMonitor;

      public PerformanceLoggingFilter( Context context, Restlet restlet )
      {
         super( context, restlet );
         queryPerformanceMonitor = LoggerFactory.getLogger( "monitor.rest.query" );
         commandPerformanceMonitor = LoggerFactory.getLogger( "monitor.rest.command" );
      }

      @Override
      protected int doHandle( Request request, Response response )
      {
         long start = System.nanoTime();
         try
         {
            return super.doHandle( request, response );
         } finally
         {
            long end = System.nanoTime();
            long requestTime = (end - start) / 1000000L;

            if (request.getMethod().equals( Method.GET))
            {
               queryPerformanceMonitor.info( "{}\t{}\t{}", new Object[]{requestTime, request.getResourceRef().getLastSegment(), request.getResourceRef() } );
            } else
            {
               commandPerformanceMonitor.info( "{}\t{}\t{}", new Object[]{requestTime, request.getResourceRef().getLastSegment(), request.getResourceRef() } );
            }
         }
      }
   }
}
