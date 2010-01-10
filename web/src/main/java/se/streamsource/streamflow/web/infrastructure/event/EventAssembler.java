/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.infrastructure.event;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.event.source.MemoryEventStoreService;
import se.streamsource.streamflow.infrastructure.event.TimeService;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;

/**
 * JAVADOC
 */
public class EventAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addValues( TransactionEvents.class, DomainEvent.class ).visibleIn( Visibility.application );
      module.addServices( EventSourceService.class ).identifiedBy( "eventsource" ).visibleIn( Visibility.application );
      module.addServices( DomainEventFactoryService.class ).visibleIn( Visibility.application );
      module.addServices( CommandEventListenerService.class ).visibleIn( Visibility.application );
      module.addObjects( TimeService.class );
      module.importServices( TimeService.class ).importedBy( NewObjectImporter.class );

      if (module.layerAssembly().applicationAssembly().mode() == Application.Mode.production)
         module.addServices( JdbmEventStoreService.class ).identifiedBy( "eventstore" ).visibleIn( Visibility.application );
      else
         module.addServices( MemoryEventStoreService.class ).identifiedBy( "eventstore" ).visibleIn( Visibility.application );
   }
}
