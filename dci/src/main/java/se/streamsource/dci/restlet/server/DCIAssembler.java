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

package se.streamsource.dci.restlet.server;

import org.apache.velocity.app.*;
import org.qi4j.bootstrap.*;
import org.restlet.service.*;
import se.streamsource.dci.restlet.server.resultwriter.*;

import java.util.*;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * JAVADOC
 */
public class DCIAssembler
        implements Assembler
{
   public void assemble(ModuleAssembly module) throws AssemblyException
   {
      Properties props = new Properties();
      try
      {
         props.load(getClass().getResourceAsStream("/velocity.properties"));

         VelocityEngine velocity = new VelocityEngine(props);

         module.importedServices(VelocityEngine.class)
                 .importedBy(INSTANCE).setMetaInfo(velocity);

      } catch (Exception e)
      {
         throw new AssemblyException("Could not load velocity properties", e);
      }
      module.objects(DefaultCommandQueryResource.class);

      module.importedServices(MetadataService.class);

      module.importedServices(ResultWriterDelegator.class).identifiedBy("resultwriterdelegator").importedBy(NEW_OBJECT);
      module.objects(ResultWriterDelegator.class);

      // Standard result writers
      module.objects(ResourceTemplateResultWriter.class,
              LinksResultWriter.class,
              TableResultWriter.class,
              ResourceResultWriter.class,
              ValueCompositeResultWriter.class,
              FormResultWriter.class);
   }
}
