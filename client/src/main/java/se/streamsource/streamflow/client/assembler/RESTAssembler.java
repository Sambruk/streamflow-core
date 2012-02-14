/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.assembler;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.specification.Specifications;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.restlet.Restlet;
import se.streamsource.dci.restlet.client.ClientAssembler;
import se.streamsource.dci.value.ValueAssembler;
import se.streamsource.streamflow.api.assembler.ClientAPIAssembler;

import static org.qi4j.api.common.Visibility.application;

/**
 * TODO
 */
public class RESTAssembler
{
   public void assembler(LayerAssembly layer) throws AssemblyException
   {
      rest(layer.module("REST"));
   }

   private void rest(ModuleAssembly module) throws AssemblyException
   {
      module.importedServices( Restlet.class ).visibleIn(application);

      new ValueAssembler().assemble(module);
      new ClientAPIAssembler().assemble(module);
      new ClientAssembler().assemble( module );

      module.values(Specifications.<Object>TRUE()).visibleIn(Visibility.application);
   }
}
