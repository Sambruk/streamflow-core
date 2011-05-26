/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.*;
import org.qi4j.library.rdf.entity.*;
import org.qi4j.library.rest.*;
import org.restlet.security.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.streamflow.util.ClassScanner;
import se.streamsource.streamflow.web.application.security.*;
import se.streamsource.streamflow.web.resource.*;
import se.streamsource.streamflow.web.resource.admin.*;
import se.streamsource.streamflow.web.resource.events.*;
import se.streamsource.streamflow.web.rest.*;

/**
 * JAVADOC
 */
public class WebAssembler
{
   public void assemble(LayerAssembly layer)
           throws AssemblyException
   {
      rest(layer.module("REST"));
   }

   private void rest(ModuleAssembly module) throws AssemblyException
   {
      module.objects(StreamflowRestApplication.class,
              ResourceFinder.class,
              EntityStateSerializer.class,
              EntityTypeSerializer.class);

      module.objects(SPARQLResource.class,
              IndexResource.class,
              EntitiesResource.class,
              EntityResource.class);

      module.importedServices(ChallengeAuthenticator.class);

      // Resources
      module.objects(
              APIRouter.class,
              AuthenticationFilter.class
      );

      // Register all resources
      for (Class aClass : Iterables.filter(ClassScanner.matches(".*Resource"), ClassScanner.getClasses(RootResource.class)))
      {
         module.objects(aClass);
      }

   }
}
