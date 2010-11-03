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

package se.streamsource.dci.restlet.server;

import org.apache.velocity.app.VelocityEngine;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.restlet.service.MetadataService;
import se.streamsource.dci.restlet.server.resultwriter.*;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

/**
 * JAVADOC
 */
public class DCIAssembler
   implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.importServices( VelocityEngine.class,
            ResponseWriterFactory.class).importedBy( NEW_OBJECT );
      module.addObjects( VelocityEngine.class);

      module.addObjects( DefaultCommandQueryResource.class );

      module.importServices( MetadataService.class );

      module.importServices( ResultWriterDelegator.class ).importedBy( NEW_OBJECT );
      module.addObjects( ResultWriterDelegator.class);

      // Standard result writers
      module.addObjects( LinksResultWriter.class,
            TableResultWriter.class,
            ResourceResultWriter.class,
            ValueCompositeResultWriter.class,
            ValueDescriptorResultWriter.class);

      module.addValues( ResourceValue.class,
            ContextValue.class,
            EntityValue.class,
            LinksValue.class,
            LinkValue.class,
            StringValue.class,

            TableValue.class,
            ColumnValue.class,
            RowValue.class,
            CellValue.class);
   }
}
