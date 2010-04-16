/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.dci.test;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.restlet.Restlet;
import se.streamsource.dci.restlet.server.DCIAssembler;
import se.streamsource.dci.restlet.server.DefaultResponseWriterFactory;
import se.streamsource.dci.restlet.server.NullCommandResult;
import se.streamsource.dci.restlet.server.ResourceFinder;
import se.streamsource.dci.test.context.RootContext;
import se.streamsource.dci.test.context.file.FileContext;
import se.streamsource.dci.test.context.file.FilesContext;
import se.streamsource.dci.test.context.jmx.DomainContext;
import se.streamsource.dci.test.context.jmx.JmxServerContext;
import se.streamsource.dci.test.context.jmx.MBeanAttributeContext;
import se.streamsource.dci.test.context.jmx.MBeanContext;
import se.streamsource.dci.test.context.jmx.TabularDataValue;

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

      for (Object service : services)
      {
         assembly.setMetaInfo( service );
      }

      return assembly;
   }

   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.importServices( TestRootContextFactory.class ).importedBy( NewObjectImporter.class );
      module.addObjects( TestRootContextFactory.class );

      // Use defaults
      module.addObjects( DefaultResponseWriterFactory.class,
            NullCommandResult.class );

      module.addTransients( RootContext.class,
            FilesContext.class,
            FileContext.class);

      module.addObjects( JmxServerContext.class, DomainContext.class, MBeanContext.class, MBeanAttributeContext.class );

      module.addObjects( TestRestletApplication.class,
            ResourceFinder.class);
      
      module.addValues( TabularDataValue.class );
   }
}
