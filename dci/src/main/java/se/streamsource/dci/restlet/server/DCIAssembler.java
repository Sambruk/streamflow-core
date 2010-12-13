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
import se.streamsource.dci.restlet.server.resultwriter.LinksResultWriter;
import se.streamsource.dci.restlet.server.resultwriter.ResourceResultWriter;
import se.streamsource.dci.restlet.server.resultwriter.ResourceTemplateResultWriter;
import se.streamsource.dci.restlet.server.resultwriter.TableResultWriter;
import se.streamsource.dci.restlet.server.resultwriter.ValueCompositeResultWriter;
import se.streamsource.dci.restlet.server.resultwriter.ValueDescriptorResultWriter;
import se.streamsource.dci.value.CellValue;
import se.streamsource.dci.value.ColumnValue;
import se.streamsource.dci.value.ContextValue;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.RowValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.TableQuery;
import se.streamsource.dci.value.TableValue;

import java.util.Properties;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * JAVADOC
 */
public class DCIAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      Properties props = new Properties();
      try
      {
         props.load( getClass().getResourceAsStream( "/velocity.properties" ) );

         VelocityEngine velocity = new VelocityEngine( props );

         module.importServices( VelocityEngine.class )
               .importedBy( INSTANCE ).setMetaInfo( velocity );

      } catch (Exception e)
      {
         throw new AssemblyException( "Could not load velocity properties", e );
      }
      module.addObjects( DefaultCommandQueryResource.class );

      module.importServices( MetadataService.class );

      module.importServices( ResultWriterDelegator.class ).importedBy( NEW_OBJECT );
      module.addObjects( ResultWriterDelegator.class );

      // Standard result writers
      module.addObjects( ResourceTemplateResultWriter.class,
            LinksResultWriter.class,
            TableResultWriter.class,
            ResourceResultWriter.class,
            ValueCompositeResultWriter.class,
            ValueDescriptorResultWriter.class );

      module.addValues( ResourceValue.class,
            ContextValue.class,
            EntityValue.class,
            LinksValue.class,
            LinkValue.class,
            StringValue.class,

            TableValue.class,
            ColumnValue.class,
            RowValue.class,
            CellValue.class,
            TableQuery.class );
   }
}
