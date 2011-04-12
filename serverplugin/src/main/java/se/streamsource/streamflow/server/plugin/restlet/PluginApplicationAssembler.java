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

package se.streamsource.streamflow.server.plugin.restlet;

import org.qi4j.api.common.*;
import org.qi4j.bootstrap.*;
import org.qi4j.entitystore.prefs.*;
import org.qi4j.library.jmx.*;
import se.streamsource.streamflow.server.plugin.authentication.*;
import se.streamsource.streamflow.server.plugin.contact.*;

import java.util.prefs.*;

/**
 * Assembler for the plugin application
 */
public class PluginApplicationAssembler
      implements ApplicationAssembler
{
   Assembler pluginAssembler;
   private final String preferenceNode;

   public PluginApplicationAssembler( Assembler pluginAssembler, String preferenceNode )
   {
      this.pluginAssembler = pluginAssembler;
      this.preferenceNode = preferenceNode;
   }

   public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
   {
      ApplicationAssembly app = applicationFactory.newApplicationAssembly();

      LayerAssembly webLayer = app.layer( "Web" );
      assembleWebLayer(webLayer);

      LayerAssembly pluginLayer = app.layer( "Plugins" );
      assemblePluginLayer(pluginLayer);

      LayerAssembly managementLayer = app.layer("Management");
      assembleManagementLayer(managementLayer);

      LayerAssembly configurationLayer = app.layer( "Configuration" );
      assembleConfigurationLayer(configurationLayer);

      managementLayer.uses(pluginLayer);
      webLayer.uses( pluginLayer );
      pluginLayer.uses( configurationLayer );

      return app;
   }

   private void assembleConfigurationLayer( LayerAssembly configurationLayer ) throws AssemblyException
   {
      ModuleAssembly configurationModuleAssembly = configurationLayer.module( "Configuration" );

      // Preferences storage
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader( null );
      Preferences node;
      try
      {
         node = Preferences.userRoot().node( preferenceNode );
      } finally
      {
         Thread.currentThread().setContextClassLoader( cl );
      }

      configurationModuleAssembly.services( PreferencesEntityStoreService.class ).setMetaInfo( new PreferencesEntityStoreInfo( node ) ).visibleIn( Visibility.application );
   }

   private void assembleManagementLayer( LayerAssembly managementLayer ) throws AssemblyException
   {
      ModuleAssembly adminAssembly = managementLayer.module("JMX");
      new JMXAssembler().assemble( adminAssembly );
   }

   private void assemblePluginLayer( LayerAssembly pluginLayer ) throws AssemblyException
   {
      // Plugins goes here
      ModuleAssembly moduleAssembly = pluginLayer.module( "Plugin" );

      moduleAssembly.values( ContactList.class,
            ContactValue.class,
            ContactAddressValue.class,
            ContactEmailValue.class,
            ContactPhoneValue.class,
            UserIdentityValue.class,
            UserDetailsValue.class ).visibleIn( Visibility.application );

      pluginAssembler.assemble( moduleAssembly );
   }

   private void assembleWebLayer( LayerAssembly webLayer ) throws AssemblyException
   {

      ModuleAssembly rest = webLayer.module( "REST" );

      // Plugin wrappers
      rest.objects( ContactLookupRestlet.class,
            AuthenticationRestlet.class );

   }
}
