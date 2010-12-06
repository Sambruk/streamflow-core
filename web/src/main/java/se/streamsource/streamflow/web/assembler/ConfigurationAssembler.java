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

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreInfo;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreService;
import org.qi4j.index.reindexer.ReindexerConfiguration;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.application.attachment.RemoveAttachmentsConfiguration;
import se.streamsource.streamflow.web.application.conversation.ConversationResponseConfiguration;
import se.streamsource.streamflow.web.application.conversation.NotificationConfiguration;
import se.streamsource.streamflow.web.configuration.ServiceConfiguration;
import se.streamsource.streamflow.web.infrastructure.database.DataSourceConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.PluginConfiguration;

import java.util.prefs.Preferences;

/**
 * JAVADOC
 */
public class ConfigurationAssembler
{
   public void assemble( LayerAssembly layer)
         throws AssemblyException
   {
      configuration(layer.moduleAssembly( "Configuration" ));
      configurationWithDefaults( layer.moduleAssembly( "DefaultConfiguration" ) );
      entityStoreConfiguration( layer.moduleAssembly( "EntityStoreConfiguration" ) );
   }

   private void configuration( ModuleAssembly module ) throws AssemblyException
   {
      System.setProperty( "application", "StreamFlowServer" );

      module.addServices( FileConfiguration.class).visibleIn( Visibility.application ).instantiateOnStartup();

      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (mode.equals( Application.Mode.production ))
         module.addServices( ServiceConfiguration.class ).visibleIn( Visibility.application ).instantiateOnStartup();

      // Configuration entities are registered in this module by using AbstractLayerAssembler.configuration()

      // Configurations
//      module.addEntities( EhCacheConfiguration.class ).visibleIn( Visibility.application );

      // Plugin configurations
      module.addEntities( PluginConfiguration.class ).visibleIn( Visibility.application );
   }

   private void configurationWithDefaults( ModuleAssembly module ) throws AssemblyException
   {
      module.addEntities( ReindexerConfiguration.class ).visibleIn( Visibility.application );
      module.addEntities( DataSourceConfiguration.class ).visibleIn( Visibility.application );
      module.addEntities( NotificationConfiguration.class ).visibleIn( Visibility.application );
      module.addEntities( ConversationResponseConfiguration.class ).visibleIn( Visibility.application );
      module.addEntities( RemoveAttachmentsConfiguration.class ).visibleIn( Visibility.application );

      module.forMixin( ReindexerConfiguration.class ).declareDefaults().loadValue().set( 50 );
      module.forMixin( DataSourceConfiguration.class ).declareDefaults().properties().set("");
      module.forMixin( NotificationConfiguration.class ).declareDefaults().enabled().set( true );
      module.forMixin( ConversationResponseConfiguration.class ).declareDefaults().enabled().set( true );
      module.forMixin( RemoveAttachmentsConfiguration.class ).declareDefaults().enabled().set( true );
   }

   private void entityStoreConfiguration( ModuleAssembly module ) throws AssemblyException
   {
      // Configuration store
      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (mode.equals( Application.Mode.development ))
      {
         // In-memory store
         module.addServices( MemoryEntityStoreService.class ).visibleIn( Visibility.layer );
      } else if (mode.equals( Application.Mode.test ))
      {
         // In-memory store
         module.addServices( MemoryEntityStoreService.class ).visibleIn( Visibility.layer );
      } else if (mode.equals( Application.Mode.production ))
      {
         // Preferences storage
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader( null );
         Preferences node;
         try
         {
            node =  Preferences.userRoot().node( "streamsource/streamflow/web" );
         } finally
         {
            Thread.currentThread().setContextClassLoader( cl );
         }

         module.addServices( PreferencesEntityStoreService.class ).setMetaInfo( new PreferencesEntityStoreInfo( node ) ).visibleIn( Visibility.layer );
      }
   }
}
