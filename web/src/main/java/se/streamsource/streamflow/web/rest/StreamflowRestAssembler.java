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

package se.streamsource.streamflow.web.rest;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.rest.entity.EntitiesResource;
import org.qi4j.rest.entity.EntityResource;
import org.qi4j.rest.query.IndexResource;
import org.qi4j.rest.query.SPARQLResource;
import org.restlet.security.ChallengeAuthenticator;
import se.streamsource.dci.restlet.server.ResourceFinder;

/**
 * JAVADOC
 */
public class StreamflowRestAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( StreamflowRestApplication.class,
            ResourceFinder.class,
            EntityStateSerializer.class,
            EntityTypeSerializer.class );

      module.addObjects( SPARQLResource.class,
            IndexResource.class,
            EntitiesResource.class,
            EntityResource.class );

      module.importServices( ChallengeAuthenticator.class);
   }
}
