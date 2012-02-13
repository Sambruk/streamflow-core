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
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.domain.individual.AccountEntity;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.IndividualEntity;
import se.streamsource.streamflow.client.domain.individual.IndividualRepositoryService;
import se.streamsource.streamflow.client.ui.DummyDataService;

/**
 * JAVADOC
 */
public class DomainAssembler
{
   public void assemble(LayerAssembly layer) throws AssemblyException
   {
      individual(layer.module("Individual"));
   }

   private void individual(ModuleAssembly module) throws AssemblyException
   {
      module.services(IndividualRepositoryService.class).visibleIn(Visibility.application);

      module.values(AccountSettingsValue.class).visibleIn(Visibility.application);
      module.entities(IndividualEntity.class, AccountEntity.class).visibleIn(Visibility.application);

      if (module.layer().application().mode().equals(Application.Mode.development))
         module.services(DummyDataService.class).instantiateOnStartup();
   }
}
