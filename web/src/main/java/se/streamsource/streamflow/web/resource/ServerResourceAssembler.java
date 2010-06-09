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

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.dci.restlet.server.DCIAssembler;
import se.streamsource.dci.restlet.server.DefaultResponseWriterFactory;
import se.streamsource.streamflow.web.resource.admin.ConsoleServerResource;
import se.streamsource.streamflow.web.resource.events.EventsServerResource;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

/**
 * Assembler for API resources
 */
public class ServerResourceAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( DefaultResponseWriterFactory.class, EventsCommandResult.class );
      new DCIAssembler().assemble( module );

      module.importServices( StreamflowRootContextFactory.class ).importedBy( NEW_OBJECT );

      // Resources
      module.addObjects(
            APIv1Router.class,
            StreamflowRootContextFactory.class,

            // Events
            EventsServerResource.class,

            // Admin
            ConsoleServerResource.class
      );
   }
}
