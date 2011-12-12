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

import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreInfo;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreService;
import org.qi4j.library.jmx.JMXAssembler;
import se.streamsource.infrastructure.management.DatasourceConfigurationManagerService;
import se.streamsource.streamflow.server.plugin.address.StreetList;
import se.streamsource.streamflow.server.plugin.address.StreetValue;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;
import se.streamsource.streamflow.server.plugin.authentication.UserIdentityValue;
import se.streamsource.streamflow.server.plugin.contact.ContactAddressValue;
import se.streamsource.streamflow.server.plugin.contact.ContactEmailValue;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactPhoneValue;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;

import java.util.prefs.Preferences;

/**
 * Assembler for the plugin application
 */
public class PluginApplicationAssembler
      implements ApplicationAssembler
{
   Assembler pluginAssembler;
   private final String preferenceNode;

    @Structure
    Module module;

   public PluginApplicationAssembler( Assembler pluginAssembler, String preferenceNode )
   {
      this.pluginAssembler = pluginAssembler;
      this.preferenceNode = preferenceNode;
   }

   public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
   {
      ApplicationAssembly app = applicationFactory.newApplicationAssembly();
      app.setName( "Streamflow-Plugins" );

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

      adminAssembly.services(DatasourceConfigurationManagerService.class).instantiateOnStartup();;
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
            UserDetailsValue.class,
            StreetList.class,
            StreetValue.class).visibleIn( Visibility.application );

      pluginAssembler.assemble( moduleAssembly );
   }

   private void assembleWebLayer( LayerAssembly webLayer ) throws AssemblyException
   {

      ModuleAssembly rest = webLayer.module( "REST" );

      // Plugin wrappers
      rest.objects( ContactLookupRestlet.class,
            AuthenticationRestlet.class, StreetAddressLookupRestlet.class );

   }
}
