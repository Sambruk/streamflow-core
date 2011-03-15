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

package se.streamsource.streamflow.client.assembler;

import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;

/**
 * JAVADOC
 */
public class StreamflowClientAssembler
      implements ApplicationAssembler
{
   Object[] serviceObjects;

   public StreamflowClientAssembler( Object... serviceObjects )
   {
      this.serviceObjects = serviceObjects;
   }

   public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
   {
      ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
      assembly.setName( "StreamflowClient" );
      assembly.setVersion( "0.1" );

      // Create layers
      LayerAssembly clientDomainInfrastructureLayer = assembly.layer( "Client domain infrastructure" );
      LayerAssembly clientDomainLayer = assembly.layer( "Client domain" );
      LayerAssembly uiLayer = assembly.layer( "UI" );

      // Define layer usage
      uiLayer.uses( clientDomainLayer, clientDomainInfrastructureLayer );
      clientDomainLayer.uses( clientDomainInfrastructureLayer );

//      assembleUILayer( uiLayer );

      new UIAssembler().assemble( uiLayer );

      new DomainAssembler().assemble( clientDomainLayer );

      new InfrastructureAssembler().assemble( clientDomainInfrastructureLayer );

      for (Object serviceObject : serviceObjects)
      {
         assembly.setMetaInfo( serviceObject );
      }

      return assembly;
   }
}