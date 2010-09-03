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

package se.streamsource.streamflow.web.assembler;

import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;

/**
 * Assembly of the Streamflow Server
 */
public class StreamflowWebAssembler
      implements ApplicationAssembler
{
   private Object[] serviceObjects;

   public StreamflowWebAssembler( Object... serviceObjects )
   {
      this.serviceObjects = serviceObjects;
   }

   public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
   {
      ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
      assembly.setName( "StreamflowServer" );

      for (Object serviceObject : serviceObjects)
      {
         assembly.setMetaInfo( serviceObject );
      }

      // Version name rules: x.y.sprint.revision
      assembly.setVersion( "1.1.7.2360" );

      LayerAssembly configurationLayer = assembly.layerAssembly( "Configuration" );
      LayerAssembly domainInfrastructureLayer = assembly.layerAssembly( "Domain infrastructure" );
      LayerAssembly domainLayer = assembly.layerAssembly( "Domain" );
      LayerAssembly contextLayer = assembly.layerAssembly( "Context" );
      LayerAssembly appLayer = assembly.layerAssembly( "Application" );
      LayerAssembly webLayer = assembly.layerAssembly( "Web" );

      webLayer.uses( appLayer, contextLayer, domainLayer, domainInfrastructureLayer );
      appLayer.uses( contextLayer, domainLayer, domainInfrastructureLayer, configurationLayer );
      contextLayer.uses( domainLayer, domainInfrastructureLayer );
      domainLayer.uses( domainInfrastructureLayer );
      domainInfrastructureLayer.uses( configurationLayer );

      assembleWebLayer( webLayer );
      assembleApplicationLayer( appLayer );
      new ContextAssembler().assemble( contextLayer );
      new DomainAssembler().assemble( domainLayer );
      new InfrastructureAssembler().assemble( domainInfrastructureLayer );
      new ConfigurationAssembler().assemble( configurationLayer );

      return assembly;
   }

   protected void assembleApplicationLayer( LayerAssembly appLayer ) throws AssemblyException
   {
      new AppAssembler().assemble( appLayer );
   }

   protected void assembleWebLayer( LayerAssembly webLayer ) throws AssemblyException
   {
      new WebAssembler().assemble( webLayer );
   }
}
