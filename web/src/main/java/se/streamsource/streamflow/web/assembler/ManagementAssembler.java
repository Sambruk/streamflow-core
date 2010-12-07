/*
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

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.library.jmx.JMXAssembler;
import se.streamsource.streamflow.web.application.management.*;
import se.streamsource.streamflow.web.application.management.jmxconnector.JmxConnectorConfiguration;
import se.streamsource.streamflow.web.application.management.jmxconnector.JmxConnectorService;
import se.streamsource.infrastructure.circuitbreaker.jmx.CircuitBreakerManagement;

import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;

/**
 * Assembler for management layer
 */
public class ManagementAssembler
   extends AbstractLayerAssembler
{
   public void assemble( LayerAssembly layer )
         throws AssemblyException
   {
      super.assemble( layer );
      jmx( layer.moduleAssembly( "JMX" ) );
   }

   private void jmx( ModuleAssembly module ) throws AssemblyException
   {
      new JMXAssembler().assemble( module );

      module.addObjects( CompositeMBean.class );
      module.addTransients( ManagerComposite.class );

      module.addServices(
            ManagerService.class,
            DatasourceConfigurationManagerService.class,
            ReindexOnStartupService.class,
            EventManagerService.class,
            ErrorLogService.class,
            CircuitBreakerManagement.class).visibleIn( application ).instantiateOnStartup();

      module.addServices( ReindexerService.class ).identifiedBy( "reindexer" ).visibleIn( layer );

      module.addServices( JmxConnectorService.class ).identifiedBy( "jmxconnector" ).instantiateOnStartup();
      configuration().addEntities( JmxConnectorConfiguration.class ).visibleIn( Visibility.application );
   }
}
