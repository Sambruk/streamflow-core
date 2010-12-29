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
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventPlayerService;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayerService;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;
import se.streamsource.streamflow.web.application.attachment.RemoveAttachmentsService;
import se.streamsource.streamflow.web.application.console.ConsoleResultValue;
import se.streamsource.streamflow.web.application.console.ConsoleScriptValue;
import se.streamsource.streamflow.web.application.console.ConsoleService;
import se.streamsource.streamflow.web.application.contact.StreamflowContactLookupService;
import se.streamsource.streamflow.web.application.mail.*;
import se.streamsource.streamflow.web.application.migration.StartupMigrationConfiguration;
import se.streamsource.streamflow.web.application.migration.StartupMigrationService;
import se.streamsource.streamflow.web.application.conversation.ConversationResponseService;
import se.streamsource.streamflow.web.application.conversation.NotificationService;
import se.streamsource.streamflow.web.application.organization.BootstrapAssembler;
import se.streamsource.streamflow.web.application.pdf.CasePdfGenerator;
import se.streamsource.streamflow.web.application.pdf.SubmittedFormPdfGenerator;
import se.streamsource.streamflow.web.application.security.AuthenticationFilterService;
import se.streamsource.streamflow.web.application.statistics.*;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;

import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;

/**
 * JAVADOC
 */
public class AppAssembler
   extends AbstractLayerAssembler
{
   public void assemble( LayerAssembly layer )
         throws AssemblyException
   {
      super.assemble( layer );

      replay(layer.moduleAssembly("Replay"));

      console( layer.moduleAssembly( "Console" ) );
      migration( layer.moduleAssembly( "Migration" ) );

      security( layer.moduleAssembly( "Security" ) );

      new BootstrapAssembler().assemble( layer.moduleAssembly( "Bootstrap" ) );

      statistics( layer.moduleAssembly( "Statistics" ) );

      contactLookup( layer.moduleAssembly( "Contact lookup" ) );

      pdf( layer.moduleAssembly( "Pdf" ) );

      attachment( layer.moduleAssembly( "Attachment" ));

      conversation( layer.moduleAssembly( "Conversation" ) );

      if (layer.applicationAssembly().mode().equals( Application.Mode.production ))
      {
         mail( layer.moduleAssembly( "Mail" ) );
      }
   }

   private void replay( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( DomainEventPlayerService.class, ApplicationEventPlayerService.class ).visibleIn( Visibility.application );
   }

   private void attachment( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.addServices( RemoveAttachmentsService.class )
            .identifiedBy( "removeattachments" ).visibleIn( application ).instantiateOnStartup();
   }

   private void pdf( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.addObjects( CasePdfGenerator.class ).visibleIn( application );
      moduleAssembly.addServices( SubmittedFormPdfGenerator.class ).visibleIn( application );
   }

   private void contactLookup( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.addServices( StreamflowContactLookupService.class ).visibleIn( Visibility.application );

      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor( "solrquery", "" );
      namedQueries.addQuery( queryDescriptor );

      moduleAssembly.importServices( NamedEntityFinder.class ).
            importedBy( ServiceSelectorImporter.class ).
            setMetaInfo( ServiceQualifier.withId( "solr" ) ).
            setMetaInfo( namedQueries );
   }

   private void mail( ModuleAssembly module ) throws AssemblyException
   {
      module.addValues( EmailValue.class ).visibleIn( Visibility.application );
      
      module.addServices( ReceiveMailService.class ).
            identifiedBy( "receivemail" ).
            instantiateOnStartup().
            visibleIn( Visibility.application ).
            setMetaInfo( new CircuitBreaker(3, 1000*60*5) );
      
      module.addServices( SendMailService.class ).
            identifiedBy( "sendmail" ).
            instantiateOnStartup().
            visibleIn( Visibility.application ).
            setMetaInfo( new CircuitBreaker(3, 1000*60*5) );

      configuration().addEntities( SendMailConfiguration.class ).visibleIn( Visibility.application );
      configuration().addEntities( ReceiveMailConfiguration.class ).visibleIn( Visibility.application );
   }

   private void conversation( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( NotificationService.class )
            .identifiedBy( "notification" )
            .instantiateOnStartup()
            .visibleIn( application );

      module.addServices( ConversationResponseService.class )
            .identifiedBy( "conversationresponse" )
            .instantiateOnStartup()
            .visibleIn( application );
   }

   private void statistics( ModuleAssembly module ) throws AssemblyException
   {
      if (module.layerAssembly().applicationAssembly().mode().equals( Application.Mode.production ))
      {
         module.addServices( CaseStatisticsService.class ).
               identifiedBy( "statistics" ).
               instantiateOnStartup().
               visibleIn( application );
         configuration().addEntities( StatisticsConfiguration.class ).visibleIn( Visibility.application );

         module.addServices( LoggingStatisticsStore.class ).
               identifiedBy( "logstatisticsstore" ).
               instantiateOnStartup().
               visibleIn( Visibility.module );
         module.addServices( JdbcStatisticsStore.class ).
               identifiedBy( "jdbcstatisticsstore" ).
               instantiateOnStartup().
               visibleIn( Visibility.module );
      }

      module.addValues( RelatedStatisticsValue.class, FormFieldStatisticsValue.class, CaseStatisticsValue.class ).visibleIn( layer );
   }

   private void security( ModuleAssembly module ) throws AssemblyException
   {
      module.addValues(UserDetailsValue.class);
      module.addServices( AuthenticationFilterService.class )
            .identifiedBy( "authentication" )
            .instantiateOnStartup()
            .setMetaInfo( new CircuitBreaker(10, 1000*60*5) )
            .visibleIn( application );
   }

   private void migration( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (mode.equals( Application.Mode.production ))
      {
         // Migrate state
         module.addServices( StartupMigrationService.class ).
               visibleIn( application ).
               identifiedBy( "startupmigration" ).
               instantiateOnStartup();
         configuration().addEntities( StartupMigrationConfiguration.class ).visibleIn( Visibility.application );
      }
   }

   private void console( ModuleAssembly module ) throws AssemblyException
   {
      module.addValues( ConsoleScriptValue.class, ConsoleResultValue.class ).visibleIn( application );

      module.addServices( ConsoleService.class ).visibleIn( application );
   }
}
