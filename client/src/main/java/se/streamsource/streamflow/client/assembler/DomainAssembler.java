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

package se.streamsource.streamflow.client.assembler;

import org.qi4j.api.common.*;
import org.qi4j.bootstrap.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.domain.individual.*;
import se.streamsource.streamflow.domain.*;
import se.streamsource.streamflow.resource.*;

/**
 * JAVADOC
 */
public class DomainAssembler
{
   public void assemble( LayerAssembly layer ) throws AssemblyException
   {
      new CommonDomainAssembler().assemble( layer );

      individual( layer.module( "Individual" ) );
      restDomainModel( layer.module( "REST domain model" ) );
   }

   private void individual( ModuleAssembly module ) throws AssemblyException
   {
      module.services( IndividualRepositoryService.class ).visibleIn( Visibility.application );

      module.values( AccountSettingsValue.class ).visibleIn( Visibility.application );
      module.entities( IndividualEntity.class, AccountEntity.class ).visibleIn( Visibility.application );
   }

   private void restDomainModel( ModuleAssembly module ) throws AssemblyException
   {
      new CommonResourceAssembler().assemble( module );
      new ClientAssembler().assemble( module );
   }
}
