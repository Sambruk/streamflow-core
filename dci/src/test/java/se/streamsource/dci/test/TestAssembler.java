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

package se.streamsource.dci.test;

import org.qi4j.api.common.*;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.service.importer.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.test.interactions.*;
import se.streamsource.dci.test.interactions.file.*;
import se.streamsource.dci.test.interactions.jmx.*;
import se.streamsource.dci.value.*;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

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
      ModuleAssembly assembly1 = assembly.layer( "Web" ).module( "REST" );
      assemble( assembly1 );

      new ValueAssembler().assemble( assembly1 );
      new DCIAssembler().assemble( assembly1 );
      assembly1.importedServices(CommandResult.class).importedBy( NEW_OBJECT );

      assembly1.importedServices( InteractionConstraintsService.class ).
            importedBy( NewObjectImporter.class ).
            visibleIn( Visibility.application );
      assembly1.objects( InteractionConstraintsService.class );


      for (Object service : services)
      {
         assembly.setMetaInfo( service );
      }

      return assembly;
   }

   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.objects( TestCommandQueryRestlet2.class );

      // Use defaults
      module.objects( ResultWriterDelegator.class,
            NullCommandResult.class );

      module.objects( RootResource.class,
            FileResource.class,
            FileContext.class);

      module.objects( JmxServerContext.class, JmxServerResource.class,
            DomainContext.class, DomainResource.class,
            MBeanContext.class, MBeanResource.class,
            MBeanAttributeContext.class );

      module.objects( TestRestletApplication.class,
            ResourceFinder.class);
      
      module.values( TabularDataValue.class );
   }
}
