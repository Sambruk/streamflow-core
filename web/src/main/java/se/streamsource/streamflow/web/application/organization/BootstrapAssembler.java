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

package se.streamsource.streamflow.web.application.organization;

import org.qi4j.api.service.qualifier.*;
import org.qi4j.api.structure.*;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.query.*;
import org.qi4j.spi.service.importer.*;

/**
 * Bootstrap and testdata assembler.
 */
public class BootstrapAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.services( BootstrapDataService.class ).instantiateOnStartup();

      if (module.layer().application().mode() == Application.Mode.development)
      {
         module.importedServices( NamedEntityFinder.class ).importedBy( ServiceSelectorImporter.class ).setMetaInfo( ServiceQualifier.withId("solr" ));
         module.services( TestDataService.class ).instantiateOnStartup();
      }
   }
}
