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
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.library.jmx.JMXAssembler;
import se.streamsource.infrastructure.circuitbreaker.jmx.CircuitBreakerManagement;
import se.streamsource.streamflow.web.application.statistics.StatisticsStoreException;
import se.streamsource.streamflow.web.management.CompositeMBean;
import se.streamsource.streamflow.web.management.DatasourceConfigurationManagerService;
import se.streamsource.streamflow.web.management.ErrorLogService;
import se.streamsource.streamflow.web.management.EventManagerService;
import se.streamsource.streamflow.web.management.InstantMessagingAdminConfiguration;
import se.streamsource.streamflow.web.management.InstantMessagingAdminService;
import se.streamsource.streamflow.web.management.Manager;
import se.streamsource.streamflow.web.management.ManagerComposite;
import se.streamsource.streamflow.web.management.ManagerService;
import se.streamsource.streamflow.web.management.ReindexOnStartupService;
import se.streamsource.streamflow.web.management.UpdateBuilder;
import se.streamsource.streamflow.web.management.UpdateConfiguration;
import se.streamsource.streamflow.web.management.UpdateOperation;
import se.streamsource.streamflow.web.management.UpdateService;
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

      update(layer.module("Update"));
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

      module.services(InstantMessagingAdminService.class).identifiedBy("imadmin").instantiateOnStartup();
      configuration().entities(InstantMessagingAdminConfiguration.class).visibleIn(Visibility.application);
   }

   private void update(ModuleAssembly update)
   {
      update.services(UpdateService.class).identifiedBy("update").setMetaInfo(
            new UpdateBuilder("0.0")
                  .toVersion("1.4.1").atStartup(new UpdateOperation()
            {
               public void update(Application app, Module module) throws StatisticsStoreException
               {
                  Manager mgr = (Manager) module.serviceFinder().findService(Manager.class).get();
                  mgr.refreshStatistics();
               }
            }));
      configuration().entities(UpdateConfiguration.class);
   }
}
