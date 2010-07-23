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

package se.streamsource.dci.test;

import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.restlet.Restlet;
import se.streamsource.dci.api.InteractionConstraintsService;
import se.streamsource.dci.restlet.server.CommandResult;
import se.streamsource.dci.restlet.server.DCIAssembler;
import se.streamsource.dci.restlet.server.DefaultResponseWriterFactory;
import se.streamsource.dci.restlet.server.NullCommandResult;
import se.streamsource.dci.restlet.server.ResourceFinder;
import se.streamsource.dci.restlet.server.ResponseWriterFactory;
import se.streamsource.dci.test.interactions.RootInteractions;
import se.streamsource.dci.test.interactions.file.FileInteractions;
import se.streamsource.dci.test.interactions.file.FilesInteractions;
import se.streamsource.dci.test.interactions.jmx.DomainInteractions;
import se.streamsource.dci.test.interactions.jmx.JmxServerInteractions;
import se.streamsource.dci.test.interactions.jmx.MBeanAttributeInteractions;
import se.streamsource.dci.test.interactions.jmx.MBeanInteractions;
import se.streamsource.dci.test.interactions.jmx.TabularDataValue;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

/**
 * JAVADOC
 */
public class TestAssembler
      implements ApplicationAssembler, Assembler
{
   private Object[] services;

   public TestAssembler( Object... services )
   {
      this.services = services;
   }

   public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
   {
      ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
      ModuleAssembly assembly1 = assembly.layerAssembly( "Web" ).moduleAssembly( "REST" );
      assemble( assembly1 );
      new DCIAssembler().assemble( assembly1 );
      assembly1.importServices(CommandResult.class).importedBy( NEW_OBJECT );

      assembly1.importServices( InteractionConstraintsService.class ).
            importedBy( NewObjectImporter.class ).
            visibleIn( Visibility.application );
      assembly1.addObjects( InteractionConstraintsService.class );


      for (Object service : services)
      {
         assembly.setMetaInfo( service );
      }

      return assembly;
   }

   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.importServices( TestRootInteractionsFactory.class ).importedBy( NewObjectImporter.class );
      module.addObjects( TestRootInteractionsFactory.class );

      // Use defaults
      module.addObjects( DefaultResponseWriterFactory.class,
            NullCommandResult.class );

      module.addTransients( RootInteractions.class,
            FilesInteractions.class,
            FileInteractions.class);

      module.addObjects( JmxServerInteractions.class, DomainInteractions.class, MBeanInteractions.class, MBeanAttributeInteractions.class );

      module.addObjects( TestRestletApplication.class,
            ResourceFinder.class);
      
      module.addValues( TabularDataValue.class );
   }
}
