/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.server.plugin.restlet;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.server.plugin.contact.ContactAddressValue;
import se.streamsource.streamflow.server.plugin.contact.ContactEmailValue;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactPhoneValue;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;

/**
 * Assembler for the plugin application
 */
public class PluginApplicationAssembler
   implements ApplicationAssembler
{
   Assembler pluginAssembler;

   public PluginApplicationAssembler( Assembler pluginAssembler )
   {
      this.pluginAssembler = pluginAssembler;
   }

   public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
   {
      ApplicationAssembly app = applicationFactory.newApplicationAssembly();

      LayerAssembly webLayer = app.layerAssembly( "Web" );

      ModuleAssembly rest = webLayer.moduleAssembly( "REST" );

      // Plugin wrappers
      rest.addObjects( ContactLookupRestlet.class);

      // Plugins goes here
      LayerAssembly pluginLayer = app.layerAssembly( "Plugins" );

      ModuleAssembly moduleAssembly = pluginLayer.moduleAssembly( "Plugin" );

      moduleAssembly.addValues( ContactList.class,
            ContactValue.class,
            ContactAddressValue.class,
            ContactEmailValue.class,
            ContactPhoneValue.class).visibleIn( Visibility.application );

      pluginAssembler.assemble( moduleAssembly );

      webLayer.uses( pluginLayer );

      return app;
   }
}
