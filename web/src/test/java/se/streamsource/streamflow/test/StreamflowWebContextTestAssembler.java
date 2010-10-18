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

package se.streamsource.streamflow.test;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.web.application.contact.StreamflowContactLookupService;
import se.streamsource.streamflow.web.application.organization.BootstrapAssembler;
import se.streamsource.streamflow.web.application.pdf.SubmittedFormPdfGenerator;
import se.streamsource.streamflow.web.assembler.StreamflowWebAssembler;

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
      appLayer.applicationAssembly().setMode( Application.Mode.test );
      appLayer.moduleAssembly( "Pdf" ).addServices( SubmittedFormPdfGenerator.class ).visibleIn( application );
      new BootstrapAssembler().assemble( appLayer.moduleAssembly( "Bootstrap" ) );


      ApplicationAssembly applicationAssembly = appLayer.applicationAssembly();
      LayerAssembly layer1 = applicationAssembly.layerAssembly( "Layer 1" );
      layer1.uses(
            applicationAssembly.layerAssembly( "Context" ),
            applicationAssembly.layerAssembly( "Domain infrastructure" ),
            applicationAssembly.layerAssembly( "Domain" ) );
      ModuleAssembly moduleAssembly = layer1.moduleAssembly( "Module 1" );
      moduleAssembly.addValues( EntityValue.class );
      applicationAssembly.layerAssembly( "Domain infrastructure" ).moduleAssembly( "Events" ).importServices( TransactionVisitor.class ).visibleIn( Visibility.application ).setMetaInfo( transactionVisitor );
      applicationAssembly.layerAssembly( "Context" ).moduleAssembly( "Contact Lookup" ).importServices( StreamflowContactLookupService.class ).visibleIn( Visibility.application );

   }

   @Override
   protected void assembleWebLayer( LayerAssembly webLayer ) throws AssemblyException
   {
   }
}