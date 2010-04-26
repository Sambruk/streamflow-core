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

package se.streamsource.streamflow.client.infrastructure.configuration;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

/**
 * Assembly of configuration for services
 */
public class ConfigurationAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      System.setProperty( "application", "StreamFlowClient" );

      module.addServices( ServiceConfiguration.class ).instantiateOnStartup();

      module.addServices( FileConfiguration.class ).instantiateOnStartup().visibleIn( Visibility.application );

      // Configurations
      module.addEntities( JdbmConfiguration.class ).visibleIn( Visibility.layer );

      // Configuration store
      module.addServices( MemoryEntityStoreService.class );
   }
}