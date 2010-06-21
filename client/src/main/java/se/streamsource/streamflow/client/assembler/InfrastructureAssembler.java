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

package se.streamsource.streamflow.client.assembler;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.entitystore.jdbm.JdbmEntityStoreService;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import se.streamsource.dci.restlet.client.ResponseHandler;
import se.streamsource.streamflow.client.infrastructure.configuration.ServiceConfiguration;
import se.streamsource.streamflow.client.infrastructure.events.ClientEventSourceService;
import se.streamsource.streamflow.client.infrastructure.events.ClientResponseHandler;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;

/**
 * JAVADOC
 */
public class InfrastructureAssembler
{
   public void assemble( LayerAssembly layer ) throws AssemblyException
   {
      configuration(layer.moduleAssembly( "Configuration" ));
      clientEntityStyore(layer.moduleAssembly("Client EntityStore"));
      clientEvents(layer.moduleAssembly("Client Events"));
   }

   public void configuration( ModuleAssembly module ) throws AssemblyException
   {
      System.setProperty( "application", "StreamFlowClient" );

      module.addServices( ServiceConfiguration.class ).instantiateOnStartup();

      module.addServices( FileConfiguration.class ).instantiateOnStartup().visibleIn( Visibility.application );

      // Configurations
      module.addEntities( JdbmConfiguration.class ).visibleIn( Visibility.layer );

      // Configuration store
      module.addServices( MemoryEntityStoreService.class );
   }

   private void clientEntityStyore( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (mode.equals( Application.Mode.development ))
      {
         // In-memory store
         module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
      } else if (mode.equals( Application.Mode.test ))
      {
         // In-memory store
         module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
      } else if (mode.equals( Application.Mode.production ))
      {
         // JDBM storage
         module.addServices( JdbmEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
      }
   }

   private void clientEvents( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.addValues( TransactionEvents.class, DomainEvent.class ).visibleIn( Visibility.application );

      moduleAssembly.addServices( ClientEventSourceService.class ).visibleIn( Visibility.application );

      moduleAssembly.importServices( ResponseHandler.class ).importedBy( NewObjectImporter.class ).visibleIn( Visibility.application );
      moduleAssembly.addObjects( ClientResponseHandler.class );
   }
}
