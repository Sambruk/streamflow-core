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

package se.streamsource.streamflow.test;

import org.qi4j.api.common.*;
import org.qi4j.api.structure.*;
import org.qi4j.bootstrap.*;
import se.streamsource.dci.value.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.web.application.contact.*;
import se.streamsource.streamflow.web.application.organization.*;
import se.streamsource.streamflow.web.application.pdf.*;
import se.streamsource.streamflow.web.assembler.*;

import static org.qi4j.api.common.Visibility.*;

/**
 * JAVADOC
 */
public class StreamflowWebContextTestAssembler
      extends StreamflowWebAssembler
{
   private TransactionVisitor transactionVisitor;


   public StreamflowWebContextTestAssembler( TransactionVisitor transactionVisitor )
   {
      this.transactionVisitor = transactionVisitor;
   }

   @Override
   protected void assembleApplicationLayer( LayerAssembly appLayer ) throws AssemblyException
   {
      appLayer.application().setMode( Application.Mode.test );
      appLayer.module( "Pdf" ).services( SubmittedFormPdfGenerator.class ).visibleIn( application );
      new BootstrapAssembler().assemble( appLayer.module( "Bootstrap" ) );

      ApplicationAssembly applicationAssembly = appLayer.application();
      LayerAssembly layer1 = applicationAssembly.layer( "Layer 1" );
      layer1.uses(
            applicationAssembly.layer( "Context" ),
            applicationAssembly.layer( "Domain infrastructure" ),
            applicationAssembly.layer( "Domain" ) );
      ModuleAssembly module = layer1.module( "Module 1" );
      module.values( EntityValue.class );
      applicationAssembly.layer( "Domain infrastructure" ).module( "Events" ).importedServices( TransactionVisitor.class ).visibleIn( Visibility.application ).setMetaInfo( transactionVisitor );
      applicationAssembly.layer( "Context" ).module( "Contact Lookup" ).importedServices( StreamflowContactLookupService.class ).visibleIn( Visibility.application );

   }

   @Override
   protected void assembleWebLayer( LayerAssembly webLayer ) throws AssemblyException
   {
   }

   @Override
   protected void assembleManagementLayer( LayerAssembly managementLayer ) throws AssemblyException
   {
   }
}