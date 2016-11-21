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

import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.INSTANCE;

import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;

import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.infrastructure.index.elasticsearch.NamedESDescriptor;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventPlayerService;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayerService;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;
import se.streamsource.streamflow.server.plugin.ldapimport.GroupDetailsValue;
import se.streamsource.streamflow.server.plugin.ldapimport.GroupListValue;
import se.streamsource.streamflow.server.plugin.ldapimport.GroupMemberDetailValue;
import se.streamsource.streamflow.server.plugin.ldapimport.UserListValue;
import se.streamsource.streamflow.web.application.archival.ArchivalConfiguration;
import se.streamsource.streamflow.web.application.archival.ArchivalService;
import se.streamsource.streamflow.web.application.archival.ArchivalStartJob;
import se.streamsource.streamflow.web.application.archival.ArchivalStopJob;
import se.streamsource.streamflow.web.application.attachment.RemoveAttachmentsService;
import se.streamsource.streamflow.web.application.console.ConsoleResultValue;
import se.streamsource.streamflow.web.application.console.ConsoleScriptValue;
import se.streamsource.streamflow.web.application.console.ConsoleService;
import se.streamsource.streamflow.web.application.contact.StreamflowContactLookupService;
import se.streamsource.streamflow.web.application.defaults.AvailabilityConfiguration;
import se.streamsource.streamflow.web.application.defaults.AvailabilityService;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsConfiguration;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.dueon.DueOnNotificationConfiguration;
import se.streamsource.streamflow.web.application.dueon.DueOnNotificationJob;
import se.streamsource.streamflow.web.application.dueon.DueOnNotificationService;
import se.streamsource.streamflow.web.application.external.IntegrationConfiguration;
import se.streamsource.streamflow.web.application.external.IntegrationService;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseConfiguration;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseService;
import se.streamsource.streamflow.web.application.mail.CreateCaseFromEmailConfiguration;
import se.streamsource.streamflow.web.application.mail.CreateCaseFromEmailService;
import se.streamsource.streamflow.web.application.mail.HtmlMailGenerator;
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
import se.streamsource.streamflow.web.application.security.AuthenticationFilterServiceConfiguration;
import se.streamsource.streamflow.web.application.statistics.CaseStatisticsService;
import se.streamsource.streamflow.web.application.statistics.CaseStatisticsValue;
import se.streamsource.streamflow.web.application.statistics.FormFieldStatisticsValue;
import se.streamsource.streamflow.web.application.statistics.JdbcStatisticsStore;
import se.streamsource.streamflow.web.application.statistics.LoggingStatisticsStore;
import se.streamsource.streamflow.web.application.statistics.OrganizationalStructureValue;
import se.streamsource.streamflow.web.application.statistics.OrganizationalUnitValue;
import se.streamsource.streamflow.web.application.statistics.RelatedStatisticsValue;
import se.streamsource.streamflow.web.application.statistics.StatisticsConfiguration;
import se.streamsource.streamflow.web.domain.util.ToJson;
import se.streamsource.streamflow.web.infrastructure.caching.CaseCountCacheService;
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;
import se.streamsource.streamflow.web.infrastructure.plugin.LdapImporterServiceConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImportJob;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImporterService;
import se.streamsource.streamflow.web.infrastructure.scheduler.Qi4JQuartzJobFactory;
import se.streamsource.streamflow.web.infrastructure.scheduler.QuartzSchedulerService;
import se.streamsource.streamflow.web.rest.service.conversation.EmailTemplatesUpdateService;
import se.streamsource.streamflow.web.rest.service.mail.MailSenderService;

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

      velocity( layer.module( "Velocity" ));

      scheduler( layer.module( "Scheduler" ) );

      dueOnNotifiation(layer.module("DueOn Notification"));

      knowledgebase(layer.module("Knowledgebase"));

      ldapimport( layer.module( "Ldapimport" ) );

      external( layer.module( "External" ) );

      // All configurations must be visible in the Application scope
      configuration().layer().entities(Specifications.<Object>TRUE()).visibleIn(Visibility.application);
   }

    private void external( ModuleAssembly external )
   {
      external.services( IntegrationService.class )
            .identifiedBy( "integration" ).instantiateOnStartup().visibleIn( Visibility.application );
      configuration().entities( IntegrationConfiguration.class );
   }

   private void system( ModuleAssembly system )
   {
       NamedQueries namedQueries = new NamedQueries();
       namedQueries.addQuery(new NamedESDescriptor("esquery", ""));
       system.importedServices(NamedEntityFinder.class).
               importedBy(ServiceSelectorImporter.class).
               setMetaInfo(namedQueries).
               setMetaInfo(ServiceQualifier.withId("es-indexing"));

      system.services( SystemDefaultsService.class )
            .identifiedBy( "systemdefaults" ).instantiateOnStartup().visibleIn(Visibility.application);

      configuration().entities(SystemDefaultsConfiguration.class);
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
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().supportCaseTypeForOutgoingEmailName().set( bundle.getString( "supportCaseTypeForOutgoingEmailName" ) );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().webFormsProxyUrl().set( "https://localhost:8443/surface" );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().defaultMarkReadTimeout().set( 15L );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().mapDefaultStartLocation().set( "59.324258,18.070450" );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().mapDefaultZoomLevel().set( 6 );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().mapDefaultUrlPattern().set( "<a href=\"http://maps.google.com/maps?z=13&t=m&q={0}\" alt=\"Google Maps\">Klicka här för att visa karta</a>" );
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().mapquestReverseLookupUrlPattern().set("http://open.mapquestapi.com/nominatim/v1/reverse?lat=%f&lon=%f&format=json");
      configuration().forMixin( SystemDefaultsConfiguration.class ).declareDefaults().webclientBaseUrl().set("http://localhost/webclient/#/cases/" );

      // set circuitbreaker time out to 12 hours - availability circuit breaker should only be able to be handled manually
      system.services( AvailabilityService.class ).identifiedBy( "availability" ).
            instantiateOnStartup().
            visibleIn( Visibility.application ).
            setMetaInfo( new CircuitBreaker( 1, 1000 * 60 * 60 * 12 ) );
      configuration().entities( AvailabilityConfiguration.class );

      system.services( CaseCountCacheService.class ).instantiateOnStartup().visibleIn( Visibility.application );

   }

   private void archival(ModuleAssembly archival)
   {
      archival.services(ArchivalService.class).identifiedBy("archival").instantiateOnStartup().visibleIn(Visibility.application);
      configuration().entities(ArchivalConfiguration.class);
      configuration().forMixin(ArchivalConfiguration.class).declareDefaults().startScheduledArchival().set(false);
      configuration().forMixin(ArchivalConfiguration.class).declareDefaults().modulo().set(1000);
      // default schedule - between 19:00 - 23:30 every day
      configuration().forMixin(ArchivalConfiguration.class).declareDefaults().startSchedule().set("0 0 19 * * ?");
      configuration().forMixin(ArchivalConfiguration.class).declareDefaults().stopSchedule().set("0 30 23 * * ?");

      archival.transients(ArchivalStartJob.class, ArchivalStopJob.class).visibleIn(application);
      archival.objects(ToJson.class);
   }

   private void dueOnNotifiation(ModuleAssembly module)
   {
      module.services(DueOnNotificationService.class).identifiedBy("dueOnNotification").instantiateOnStartup().visibleIn(Visibility.application);
      configuration().entities(DueOnNotificationConfiguration.class);
      // default schedule - 08:00 every day
      configuration().forMixin( DueOnNotificationConfiguration.class ).declareDefaults().schedule().set( "0 0 8 * * ?" );

      module.transients( DueOnNotificationJob.class).visibleIn( application );
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
      module.services( MailSenderService.class ).identifiedBy( "mailsender" )
            .visibleIn( application ).instantiateOnStartup();


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
      Application.Mode mode = module.layer().application().mode();
      if (mode.equals( Application.Mode.production ))
      {
         module.values( UserDetailsValue.class, GroupDetailsValue.class );
         module.services( AuthenticationFilterService.class )
            .identifiedBy( "authentication" )
            .instantiateOnStartup()
            .setMetaInfo(new CircuitBreaker(10, 1000 * 60 * 5))
            .visibleIn(application);

         configuration().entities( AuthenticationFilterServiceConfiguration.class ).visibleIn( Visibility.application );
      }
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

   private void velocity(ModuleAssembly module) throws AssemblyException
   {
      Properties props = new Properties();
      try
      {
         props.load(getClass().getResourceAsStream("/velocity.properties"));

         VelocityEngine velocity = new VelocityEngine(props);

         module.importedServices(VelocityEngine.class)
                 .importedBy(INSTANCE).setMetaInfo(velocity).visibleIn( layer );

      } catch (Exception e)
      {
         throw new AssemblyException("Could not load velocity properties", e);
      }
      module.objects( HtmlMailGenerator.class ).visibleIn( Visibility.application );
   }

   private void knowledgebase(ModuleAssembly knowledgebase) throws AssemblyException
   {
      knowledgebase.services(KnowledgebaseService.class).identifiedBy("knowledgebase").instantiateOnStartup().visibleIn(Visibility.application);
      configuration().entities(KnowledgebaseConfiguration.class);
   }

   private void ldapimport( ModuleAssembly module )
   {
      module.services( LdapImporterService.class )
            .identifiedBy( "ldapimport" )
            .instantiateOnStartup()
            .setMetaInfo(new CircuitBreaker(10, 1000 * 60 * 5))
            .visibleIn(application);

      configuration().entities( LdapImporterServiceConfiguration.class ).visibleIn( Visibility.application );
      // default schedule - run att 17:00 every day
      configuration().forMixin( LdapImporterServiceConfiguration.class ).declareDefaults().schedule().set( "0 0 17 * * ?" );

      module.transients( LdapImportJob.class).visibleIn( application );

      module.values( UserDetailsValue.class,
            GroupDetailsValue.class,
            UserListValue.class,
            GroupListValue.class,
            GroupMemberDetailValue.class ).visibleIn( Visibility.application );
   }

   private void scheduler( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( Qi4JQuartzJobFactory.class, QuartzSchedulerService.class ).visibleIn( Visibility.application );

   }
}
