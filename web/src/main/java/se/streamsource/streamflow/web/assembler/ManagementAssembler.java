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

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.library.jmx.JMXAssembler;
import se.streamsource.infrastructure.circuitbreaker.jmx.CircuitBreakerManagement;
import se.streamsource.streamflow.web.management.CompositeMBean;
import se.streamsource.streamflow.web.management.DatasourceConfigurationManagerService;
import se.streamsource.streamflow.web.management.ErrorLogService;
import se.streamsource.streamflow.web.management.EventManagerService;
import se.streamsource.streamflow.web.management.ManagerComposite;
import se.streamsource.streamflow.web.management.ManagerService;
import se.streamsource.streamflow.web.management.ReindexOnStartupService;
import se.streamsource.streamflow.web.management.jmxconnector.JmxConnectorConfiguration;
import se.streamsource.streamflow.web.management.jmxconnector.JmxConnectorService;

import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;

/**
 * Assembler for management layer
 */
public class ManagementAssembler
        extends AbstractLayerAssembler
{
   public void assemble(LayerAssembly layer)
           throws AssemblyException
   {
      super.assemble(layer);
      jmx(layer.module("JMX"));
   }

   private void jmx(ModuleAssembly module) throws AssemblyException
   {
      new JMXAssembler().assemble(module);

      module.objects(CompositeMBean.class);
      module.transients(ManagerComposite.class);

      module.services(
              ManagerService.class,
              DatasourceConfigurationManagerService.class,
              ReindexOnStartupService.class,
              EventManagerService.class,
              ErrorLogService.class,
              CircuitBreakerManagement.class).visibleIn(application).instantiateOnStartup();

      module.services(ReindexerService.class).identifiedBy("reindexer").visibleIn(layer);

      module.services(JmxConnectorService.class).identifiedBy("jmxconnector").instantiateOnStartup();
      configuration().entities(JmxConnectorConfiguration.class).visibleIn(Visibility.application);
   }
}
