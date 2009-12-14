/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.test;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.web.StreamFlowWebAssembler;
import se.streamsource.streamflow.web.application.organization.BootstrapAssembler;

/**
 * JAVADOC
 */
public class StreamFlowWebDomainTestAssembler
      extends StreamFlowWebAssembler
{
   private Class[] testClass;
   private Object[] serviceObjects;


   public StreamFlowWebDomainTestAssembler( Class[] testClass, Object... serviceObjects )
   {
      this.serviceObjects = serviceObjects;
      this.testClass = testClass;
   }

   @Override
   protected void assembleApplicationLayer( LayerAssembly appLayer ) throws AssemblyException
   {
      appLayer.applicationAssembly().setMode( Application.Mode.test );
//        super.assembleApplicationLayer(appLayer);
      new BootstrapAssembler().assemble( appLayer.moduleAssembly( "Bootstrap" ) );
   }

   @Override
   protected void assembleWebLayer( LayerAssembly webLayer ) throws AssemblyException
   {
   }

   @Override
   protected void assembleDomainLayer( LayerAssembly domainLayer ) throws AssemblyException
   {
      super.assembleDomainLayer( domainLayer );
      ModuleAssembly moduleAssembly = domainLayer.moduleAssembly( "Test" );
      moduleAssembly.addObjects( testClass );
      moduleAssembly.importServices( EventListener.class ).visibleIn( Visibility.application );

      for (Object serviceObject : serviceObjects)
      {
         domainLayer.applicationAssembly().setMetaInfo( serviceObject );
      }
   }
}
