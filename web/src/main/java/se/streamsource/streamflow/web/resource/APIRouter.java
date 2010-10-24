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

import org.qi4j.api.injection.scope.Service;
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
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import se.streamsource.dci.restlet.server.CommandQueryRestlet2;
import se.streamsource.dci.restlet.server.ResourceFinder;
import se.streamsource.streamflow.web.application.security.AuthenticationFilterFactory;
import se.streamsource.streamflow.web.resource.admin.ConsoleServerResource;
import se.streamsource.streamflow.web.resource.admin.SolrSearchServerResource;
import se.streamsource.streamflow.web.resource.events.DomainEventsServerResource;

/**
 * Router for the Streamflow REST API.
 */
public class APIRouter
      extends Router
{
   private ObjectBuilderFactory factory;
   private AuthenticationFilterFactory filterFactory;

   public APIRouter( @Uses Context context, @Structure ObjectBuilderFactory factory, @Service AuthenticationFilterFactory filterFactory ) throws Exception
   {
      super( context );
      this.factory = factory;
      this.filterFactory = filterFactory;

      Restlet cqr = factory.newObjectBuilder( CommandQueryRestlet2.class ).use( context ).newInstance();

      Filter authenticationFilter = this.filterFactory.createFilter( context, cqr );

      Filter performanceLoggingFilter = new PerformanceLoggingFilter( context, authenticationFilter );

      attachDefault( new ExtensionMediaTypeFilter( getContext(), performanceLoggingFilter ) );

      // Events
      attach( "/events/domain", new ExtensionMediaTypeFilter( getContext(), createServerResourceFinder( DomainEventsServerResource.class ) ), Template.MODE_STARTS_WITH );

      // Admin resources
      Router adminRouter = new Router( getContext() );
      adminRouter.attach( "/entity", createServerResourceFinder( EntitiesResource.class ) );
      adminRouter.attach( "/entity/{identity}", createServerResourceFinder( EntityResource.class ) );
      adminRouter.attach( "/query", new PerformanceLoggingFilter( context, createServerResourceFinder( SPARQLResource.class ) ), Template.MODE_STARTS_WITH );
      adminRouter.attach( "/index", createServerResourceFinder( IndexResource.class ) );
      adminRouter.attach( "/console", createServerResourceFinder( ConsoleServerResource.class ) );
      adminRouter.attach( "/search", createServerResourceFinder( SolrSearchServerResource.class ) );
      attach( "/admin/tools", new ExtensionMediaTypeFilter( getContext(), adminRouter ) );

      Directory dir = new Directory( getContext(), "clap://thread/static/admin/" );
      dir.setIndexName( "index.html" );
      attach( "/admin/", dir );


      // Version info
      Directory directory = new Directory( getContext(), "clap://thread/static/" );
      directory.setListingAllowed( true );
      attach( "/static", this.filterFactory.createFilter( getContext(), directory ) );
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
         return filterFactory.createFilter( getContext(), finder );
      } else
         return finder;
   }
}
