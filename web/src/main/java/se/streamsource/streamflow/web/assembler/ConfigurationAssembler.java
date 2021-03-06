/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.assembler;

import java.util.prefs.Preferences;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.MixinDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreInfo;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreService;
import org.qi4j.index.reindexer.ReindexerConfiguration;

import org.qi4j.library.rdf.repository.NativeConfiguration;
import se.streamsource.infrastructure.database.DataSourceConfiguration;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchConfiguration;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.application.attachment.RemoveAttachmentsConfiguration;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorConfiguration;
import se.streamsource.streamflow.web.configuration.ServiceConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.ContactLookupServiceConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.KartagoPluginConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.StreetAddressLookupConfiguration;
import se.streamsource.streamflow.web.rest.service.conversation.ConversationResponseConfiguration;
import se.streamsource.streamflow.web.rest.service.conversation.NotificationConfiguration;

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

      module.services( ServiceConfiguration.class ).visibleIn( Visibility.application ).instantiateOnStartup();
      module.entities(JdbmConfiguration.class, NativeConfiguration.class, ElasticSearchConfiguration.class ).visibleIn( Visibility.application );
       if (mode.equals( Application.Mode.production ))

      // Configuration entities are registered in this module by using AbstractLayerAssembler.configuration()

      // Configurations
//      module.entities( EhCacheConfiguration.class ).visibleIn( Visibility.application );

      // Plugin configurations
      module.entities(
              ContactLookupServiceConfiguration.class,
              KartagoPluginConfiguration.class).visibleIn(Visibility.application);
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

      module.forMixin( StreetAddressLookupConfiguration.class ).declareDefaults().loadFrequence().set( 604800000L );
      module.forMixin( StreetAddressLookupConfiguration.class ).declareDefaults().minkeywordlength().set( 3 );
      module.forMixin( StreetAddressLookupConfiguration.class ).declareDefaults().limit().set( 10 );
      module.forMixin( StreetAddressLookupConfiguration.class ).declareDefaults().url().set( "http://localhost:8086/streets/street" );

      module.forMixin( NotificationConfiguration.class ).declareDefaults().notificationOnlyMailSubject().set(
            "A message has been added to a conversation in this case.");
      module.forMixin( NotificationConfiguration.class ).declareDefaults().notificationOnlyMailBody().set(
            "A message has been added to a conversation in the case: %s.");

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
