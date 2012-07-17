/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import org.qi4j.bootstrap.MixinDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreInfo;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreService;
import org.qi4j.index.reindexer.ReindexerConfiguration;
import se.streamsource.infrastructure.database.DataSourceConfiguration;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.application.attachment.RemoveAttachmentsConfiguration;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorConfiguration;
import se.streamsource.streamflow.web.configuration.ServiceConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.KartagoPluginConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.PluginConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.StreetAddressLookupConfiguration;
import se.streamsource.streamflow.web.rest.service.conversation.ConversationResponseConfiguration;
import se.streamsource.streamflow.web.rest.service.conversation.NotificationConfiguration;

import java.util.prefs.Preferences;

/**
 * JAVADOC
 */
public class ConfigurationAssembler
{
   public void assemble( LayerAssembly layer)
         throws AssemblyException
   {
      configuration(layer.module( "Configuration" ));
      configurationWithDefaults( layer.module( "DefaultConfiguration" ) );
      entityStoreConfiguration( layer.module( "EntityStoreConfiguration" ) );
   }

   private void configuration( ModuleAssembly module ) throws AssemblyException
   {
      module.services( FileConfiguration.class).visibleIn( Visibility.application ).instantiateOnStartup();

      Application.Mode mode = module.layer().application().mode();
      if (mode.equals( Application.Mode.production ))
         module.services( ServiceConfiguration.class ).visibleIn( Visibility.application ).instantiateOnStartup();

      // Configuration entities are registered in this module by using AbstractLayerAssembler.configuration()

      // Configurations
//      module.entities( EhCacheConfiguration.class ).visibleIn( Visibility.application );

      // Plugin configurations
      module.entities(
            PluginConfiguration.class,
            KartagoPluginConfiguration.class ).visibleIn( Visibility.application );
   }

   private void configurationWithDefaults( ModuleAssembly module ) throws AssemblyException
   {
      module.entities( ReindexerConfiguration.class,
            DataSourceConfiguration.class,
            NotificationConfiguration.class,
            ConversationResponseConfiguration.class,
            RemoveAttachmentsConfiguration.class,
            StreetAddressLookupConfiguration.class,
            PdfGeneratorConfiguration.class
      ).visibleIn( Visibility.application );

      module.forMixin( ReindexerConfiguration.class ).declareDefaults().loadValue().set( 50 );

      module.forMixin( DataSourceConfiguration.class ).declareDefaults().properties().set("");

      module.forMixin( NotificationConfiguration.class ).declareDefaults().enabled().set( true );

      module.forMixin( ConversationResponseConfiguration.class ).declareDefaults().enabled().set( true );

      module.forMixin( RemoveAttachmentsConfiguration.class ).declareDefaults().enabled().set( true );

      module.forMixin( StreetAddressLookupConfiguration.class ).declareDefaults().loadFrequence().set( 604800000L );
      module.forMixin( StreetAddressLookupConfiguration.class ).declareDefaults().enabled().set( false );
      module.forMixin( StreetAddressLookupConfiguration.class ).declareDefaults().minkeywordlength().set( 3 );
      module.forMixin( StreetAddressLookupConfiguration.class ).declareDefaults().limit().set( 10 );
      module.forMixin( StreetAddressLookupConfiguration.class ).declareDefaults().url().set( "http://localhost:8086/streets/street" );

      MixinDeclaration<PdfGeneratorConfiguration> pdfConfig = module.forMixin( PdfGeneratorConfiguration.class );
      pdfConfig.declareDefaults().headerMargin().set( 100F );
      pdfConfig.declareDefaults().footerMargin().set( 80F );
      pdfConfig.declareDefaults().leftMargin().set( 80F );
      pdfConfig.declareDefaults().rightMargin().set( 80F );

   }

   private void entityStoreConfiguration( ModuleAssembly module ) throws AssemblyException
   {
      // Configuration store
      Application.Mode mode = module.layer().application().mode();
      if (mode.equals( Application.Mode.development ))
      {
         // In-memory store
         module.services( MemoryEntityStoreService.class ).visibleIn( Visibility.layer );
      } else if (mode.equals( Application.Mode.test ))
      {
         // In-memory store
         module.services( MemoryEntityStoreService.class ).visibleIn( Visibility.layer );
      } else if (mode.equals( Application.Mode.production ))
      {
         // Preferences storage
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader( null );
         Preferences node;
         try
         {
            node =  Preferences.userRoot().node( "streamsource/streamflow/"+module.layer().application().name() );
         } finally
         {
            Thread.currentThread().setContextClassLoader( cl );
         }

         module.services( PreferencesEntityStoreService.class ).setMetaInfo( new PreferencesEntityStoreInfo( node ) ).visibleIn( Visibility.layer );
      }
   }
}
