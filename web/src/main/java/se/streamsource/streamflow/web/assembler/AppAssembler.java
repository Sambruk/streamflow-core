/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.specification.Specifications;
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
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventPlayerService;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayerService;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;
import se.streamsource.streamflow.web.application.archival.ArchivalConfiguration;
import se.streamsource.streamflow.web.application.archival.ArchivalService;
import se.streamsource.streamflow.web.application.attachment.RemoveAttachmentsService;
import se.streamsource.streamflow.web.application.console.ConsoleResultValue;
import se.streamsource.streamflow.web.application.console.ConsoleScriptValue;
import se.streamsource.streamflow.web.application.console.ConsoleService;
import se.streamsource.streamflow.web.application.contact.StreamflowContactLookupService;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsConfiguration;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.dueon.DueOnNotificationConfiguration;
import se.streamsource.streamflow.web.application.dueon.DueOnNotificationService;
import se.streamsource.streamflow.web.application.dueon.DueOnNotification;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseConfiguration;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseService;
import se.streamsource.streamflow.web.application.mail.CreateCaseFromEmailConfiguration;
import se.streamsource.streamflow.web.application.mail.CreateCaseFromEmailService;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.ReceiveMailConfiguration;
import se.streamsource.streamflow.web.application.mail.ReceiveMailService;
import se.streamsource.streamflow.web.application.mail.SendMailConfiguration;
import se.streamsource.streamflow.web.application.mail.SendMailService;
import se.streamsource.streamflow.web.application.migration.StartupMigrationConfiguration;
import se.streamsource.streamflow.web.application.migration.StartupMigrationService;
import se.streamsource.streamflow.web.application.organization.BootstrapAssembler;
import se.streamsource.streamflow.web.application.pdf.CasePdfGenerator;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorService;
import se.streamsource.streamflow.web.application.security.AuthenticationFilterService;
import se.streamsource.streamflow.web.application.statistics.CaseStatisticsService;
import se.streamsource.streamflow.web.application.statistics.CaseStatisticsValue;
import se.streamsource.streamflow.web.application.statistics.FormFieldStatisticsValue;
import se.streamsource.streamflow.web.application.statistics.JdbcStatisticsStore;
import se.streamsource.streamflow.web.application.statistics.LoggingStatisticsStore;
import se.streamsource.streamflow.web.application.statistics.OrganizationalStructureValue;
import se.streamsource.streamflow.web.application.statistics.OrganizationalUnitValue;
import se.streamsource.streamflow.web.application.statistics.RelatedStatisticsValue;
import se.streamsource.streamflow.web.application.statistics.StatisticsConfiguration;
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;
import se.streamsource.streamflow.web.rest.service.conversation.EmailTemplatesUpdateService;

import java.util.Properties;
import java.util.ResourceBundle;

import static org.qi4j.api.common.Visibility.*;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

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
      
      system( layer.module( "System" ));

      archival(layer.module("Archival"));

      dueOnNotifiation(layer.module("DueOn Notification"));
      
      replay(layer.module("Replay"));

      console( layer.module( "Console" ) );
      migration( layer.module( "Migration" ) );

      security( layer.module( "Security" ) );

      new BootstrapAssembler().assemble( layer.module( "Bootstrap" ) );

      statistics( layer.module( "Statistics" ) );

      contactLookup( layer.module( "Contact lookup" ) );

      pdf( layer.module( "Pdf" ) );

      attachment( layer.module( "Attachment" ));

      if (layer.application().mode().equals( Application.Mode.production ))
      {
         mail( layer.module( "Mail" ) );
      }

      knowledgebase(layer.module("Knowledgebase"));

      // All configurations must be visible in the Application scope
      configuration().layer().entities(Specifications.<Object>TRUE()).visibleIn(Visibility.application);
   }

   private void system( ModuleAssembly system )
   {
      system.services( SystemDefaultsService.class )
            .identifiedBy( "systemdefaults" ).instantiateOnStartup().visibleIn( Visibility.application );
      configuration().entities( SystemDefaultsConfiguration.class );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().enabled().set( true );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().sortOrderAscending().set( false );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().caseLogAttachmentVisible().set( false );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().caseLogContactVisible().set( false );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().caseLogConversationVisible().set( false );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().caseLogCustomVisible().set( true );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().caseLogFormVisible().set( true );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().caseLogSystemVisible().set( false );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().caseLogSystemTraceVisible().set( false );

      ResourceBundle bundle = ResourceBundle.getBundle( AppAssembler.class.getName() );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().supportOrganizationName().set( bundle.getString( "supportOuName" ) );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().supportProjectName().set( bundle.getString( "supportProjectName" ) );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().supportCaseTypeForIncomingEmailName().set( bundle.getString( "supportCaseTypeForIncomingEmailName" ) );
   }

   private void archival(ModuleAssembly archival)
   {
      archival.services(ArchivalService.class).identifiedBy("archival").instantiateOnStartup().visibleIn(Visibility.application);
      configuration().entities(ArchivalConfiguration.class);
   }

   private void dueOnNotifiation(ModuleAssembly dueOnNotification)
   {
      dueOnNotification.services(DueOnNotificationService.class).identifiedBy("dueOnNotification").instantiateOnStartup().visibleIn(Visibility.application);
      configuration().entities(DueOnNotificationConfiguration.class);
   }
   
   private void replay( ModuleAssembly module ) throws AssemblyException
   {
      module.services( DomainEventPlayerService.class, ApplicationEventPlayerService.class ).visibleIn( Visibility.application );
   }

   private void attachment( ModuleAssembly module ) throws AssemblyException
   {
      module.services( RemoveAttachmentsService.class )
            .identifiedBy( "removeattachments" ).visibleIn(application).instantiateOnStartup();
   }

   private void pdf( ModuleAssembly module ) throws AssemblyException
   {
      module.objects( CasePdfGenerator.class ).visibleIn( application );
      module.services( PdfGeneratorService.class ).identifiedBy( "generatepdf" )
            .visibleIn( application ).instantiateOnStartup();
   }

   private void contactLookup( ModuleAssembly module ) throws AssemblyException
   {
      module.services(StreamflowContactLookupService.class).visibleIn( Visibility.application );

      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor( "solrquery", "" );
      namedQueries.addQuery( queryDescriptor );

      module.importedServices( NamedEntityFinder.class ).
            importedBy( ServiceSelectorImporter.class ).
            setMetaInfo(ServiceQualifier.withId("solr")).
            setMetaInfo(namedQueries);
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
            setMetaInfo(new CircuitBreaker(3, 1000 * 60 * 5));

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
                              "        ?entity <urn:qi4j:type:se.streamsource.streamflow.web.domain.structure.user.Contactable-Data#contact> ?v0.\n" +
                              "        ?v0 <urn:qi4j:type:se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO#emailAddresses> ?email\n" +
                              "        }"));
      module.importedServices(NamedEntityFinder.class).
              importedBy(ImportedServiceDeclaration.SERVICE_SELECTOR).
              setMetaInfo(namedQueries).
              setMetaInfo(ServiceQualifier.withId("RdfIndexingEngineService")).visibleIn( layer );

      module.services(CreateCaseFromEmailService.class).visibleIn(Visibility.application).instantiateOnStartup();
      configuration().entities(CreateCaseFromEmailConfiguration.class).visibleIn(Visibility.application);
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

      module.values(RelatedStatisticsValue.class, FormFieldStatisticsValue.class, OrganizationalStructureValue.class, OrganizationalUnitValue.class, CaseStatisticsValue.class).visibleIn(layer);
   }

   private void security( ModuleAssembly module ) throws AssemblyException
   {
      module.values(UserDetailsValue.class);
      module.services( AuthenticationFilterService.class )
            .identifiedBy( "authentication" )
            .instantiateOnStartup()
            .setMetaInfo(new CircuitBreaker(10, 1000 * 60 * 5))
            .visibleIn(application);
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

   private void knowledgebase(ModuleAssembly knowledgebase) throws AssemblyException
   {
      Properties props = new Properties();
      try
      {
         props.load(getClass().getResourceAsStream("/velocity.properties"));

         VelocityEngine velocity = new VelocityEngine(props);

         knowledgebase.importedServices(VelocityEngine.class)
                 .importedBy(INSTANCE).setMetaInfo(velocity);

      } catch (Exception e)
      {
         throw new AssemblyException("Could not load velocity properties", e);
      }

      knowledgebase.services(KnowledgebaseService.class).identifiedBy("knowledgebase").visibleIn(Visibility.application);
      configuration().entities(KnowledgebaseConfiguration.class);
   }
}
