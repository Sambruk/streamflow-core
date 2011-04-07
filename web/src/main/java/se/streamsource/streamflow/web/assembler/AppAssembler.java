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
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.query.NamedSparqlDescriptor;
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
import se.streamsource.streamflow.web.application.conversation.EmailTemplatesUpdateService;
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

      replay(layer.module("Replay"));

      console( layer.module( "Console" ) );
      migration( layer.module( "Migration" ) );

      security( layer.module( "Security" ) );

      new BootstrapAssembler().assemble( layer.module( "Bootstrap" ) );

      statistics( layer.module( "Statistics" ) );

      contactLookup( layer.module( "Contact lookup" ) );

      pdf( layer.module( "Pdf" ) );

      attachment( layer.module( "Attachment" ));

      conversation( layer.module( "Conversation" ) );

      if (layer.application().mode().equals( Application.Mode.production ))
      {
         mail( layer.module( "Mail" ) );
      }
   }

   private void replay( ModuleAssembly module ) throws AssemblyException
   {
      module.services( DomainEventPlayerService.class, ApplicationEventPlayerService.class ).visibleIn( Visibility.application );
   }

   private void attachment( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.services( RemoveAttachmentsService.class )
            .identifiedBy( "removeattachments" ).visibleIn( application ).instantiateOnStartup();
   }

   private void pdf( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.objects( CasePdfGenerator.class ).visibleIn( application );
      moduleAssembly.services( SubmittedFormPdfGenerator.class ).visibleIn( application );
   }

   private void contactLookup( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.services( StreamflowContactLookupService.class ).visibleIn( Visibility.application );

      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor( "solrquery", "" );
      namedQueries.addQuery( queryDescriptor );

      moduleAssembly.importedServices( NamedEntityFinder.class ).
            importedBy( ServiceSelectorImporter.class ).
            setMetaInfo( ServiceQualifier.withId( "solr" ) ).
            setMetaInfo( namedQueries );
   }

   private void mail( ModuleAssembly module ) throws AssemblyException
   {
      module.services(EmailTemplatesUpdateService.class).instantiateOnStartup();

      module.values( EmailValue.class ).visibleIn(Visibility.application);
      
      module.services( ReceiveMailService.class ).
            identifiedBy( "receivemail" ).
            instantiateOnStartup().
            visibleIn( Visibility.application ).
            setMetaInfo( new CircuitBreaker(3, 1000*60*5) );
      
      module.services( SendMailService.class ).
            identifiedBy( "sendmail" ).
            instantiateOnStartup().
            visibleIn( Visibility.application ).
            setMetaInfo( new CircuitBreaker(3, 1000*60*5) );

      configuration().entities( SendMailConfiguration.class ).visibleIn( Visibility.application );
      configuration().entities( ReceiveMailConfiguration.class ).visibleIn( Visibility.application );

      NamedQueries namedQueries = new NamedQueries();
      namedQueries.addQuery(      new NamedSparqlDescriptor("finduserwithemail",
                      "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#>\n" +
                              "        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                              "        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                              "        SELECT DISTINCT ?identity\n" +
                              "        WHERE {\n" +
                              "        ?entity rdf:type <urn:qi4j:type:se.streamsource.streamflow.web.domain.entity.user.UserEntity>.\n" +
                              "        ?entity ns0:identity ?identity.\n" +
                              "        ?entity <urn:qi4j:type:se.streamsource.streamflow.domain.contact.Contactable-Data#contact> ?v0.\n" +
                              "        ?v0 <urn:qi4j:type:se.streamsource.streamflow.domain.contact.ContactValue#emailAddresses> ?email\n" +
                              "        }"));
      module.importedServices(NamedEntityFinder.class).
              importedBy(ImportedServiceDeclaration.SERVICE_SELECTOR).
              setMetaInfo(namedQueries).
              setMetaInfo(ServiceQualifier.withId("RdfIndexingEngineService"));

      module.services(CreateCaseFromEmailService.class).visibleIn(Visibility.application).instantiateOnStartup();
      configuration().entities(CreateCaseFromEmailConfiguration.class).visibleIn(Visibility.application);
   }

   private void conversation( ModuleAssembly module ) throws AssemblyException
   {
      module.services( NotificationService.class )
            .identifiedBy( "notification" )
            .instantiateOnStartup()
            .visibleIn( application );

      module.services( ConversationResponseService.class )
            .identifiedBy( "conversationresponse" )
            .instantiateOnStartup()
            .visibleIn( application );
   }

   private void statistics( ModuleAssembly module ) throws AssemblyException
   {
      if (module.layer().application().mode().equals( Application.Mode.production ))
      {
         module.services( CaseStatisticsService.class ).
               identifiedBy( "statistics" ).
               instantiateOnStartup().
               visibleIn( application );
         configuration().entities( StatisticsConfiguration.class ).visibleIn( Visibility.application );

         module.services( LoggingStatisticsStore.class ).
               identifiedBy( "logstatisticsstore" ).
               instantiateOnStartup().
               visibleIn( Visibility.module );
         module.services( JdbcStatisticsStore.class ).
               identifiedBy( "jdbcstatisticsstore" ).
               instantiateOnStartup().
               visibleIn( Visibility.module );
      }

      module.values( RelatedStatisticsValue.class, FormFieldStatisticsValue.class, CaseStatisticsValue.class ).visibleIn( layer );
   }

   private void security( ModuleAssembly module ) throws AssemblyException
   {
      module.values(UserDetailsValue.class);
      module.services( AuthenticationFilterService.class )
            .identifiedBy( "authentication" )
            .instantiateOnStartup()
            .setMetaInfo( new CircuitBreaker(10, 1000*60*5) )
            .visibleIn( application );
   }

   private void migration( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layer().application().mode();
      if (mode.equals( Application.Mode.production ))
      {
         // Migrate state
         module.services( StartupMigrationService.class ).
               visibleIn( application ).
               identifiedBy( "startupmigration" ).
               instantiateOnStartup();
         configuration().entities( StartupMigrationConfiguration.class ).visibleIn( Visibility.application );
      }
   }

   private void console( ModuleAssembly module ) throws AssemblyException
   {
      module.values( ConsoleScriptValue.class, ConsoleResultValue.class ).visibleIn( application );

      module.services( ConsoleService.class ).visibleIn( application );
   }
}
