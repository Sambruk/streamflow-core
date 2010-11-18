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

package se.streamsource.streamflow.web.assembler;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.rest.entity.EntitiesResource;
import org.qi4j.rest.entity.EntityResource;
import org.qi4j.rest.query.IndexResource;
import org.qi4j.rest.query.SPARQLResource;
import org.restlet.security.ChallengeAuthenticator;
import se.streamsource.dci.restlet.server.ResourceFinder;
import se.streamsource.streamflow.web.application.security.AuthenticationFilter;
import se.streamsource.streamflow.web.resource.APIRouter;
import se.streamsource.streamflow.web.resource.admin.ConsoleServerResource;
import se.streamsource.streamflow.web.resource.admin.SolrSearchServerResource;
import se.streamsource.streamflow.web.resource.events.ApplicationEventsServerResource;
import se.streamsource.streamflow.web.resource.events.DomainEventsServerResource;
import se.streamsource.streamflow.web.rest.StreamflowRestApplication;

/**
 * JAVADOC
 */
public class WebAssembler
{
   public void assemble( LayerAssembly layer )
         throws AssemblyException
   {
      rest( layer.moduleAssembly( "REST" ) );
   }

   private void rest( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( StreamflowRestApplication.class,
            ResourceFinder.class,
            EntityStateSerializer.class,
            EntityTypeSerializer.class );

      module.addObjects( SPARQLResource.class,
            IndexResource.class,
            EntitiesResource.class,
            EntityResource.class );

      module.importServices( ChallengeAuthenticator.class );

      // Resources
      module.addObjects(
            APIRouter.class,
            AuthenticationFilter.class,

            // Events
            DomainEventsServerResource.class,
            ApplicationEventsServerResource.class,

            // Admin
            ConsoleServerResource.class,
            SolrSearchServerResource.class
      );
   }
}
