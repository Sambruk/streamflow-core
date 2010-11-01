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
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.rest.MBeanServerImporter;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;
import se.streamsource.streamflow.infrastructure.ConfigurationManagerService;
import se.streamsource.streamflow.infrastructure.event.replay.DomainEventPlayerService;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;
import se.streamsource.streamflow.web.application.console.ConsoleResultValue;
import se.streamsource.streamflow.web.application.console.ConsoleScriptValue;
import se.streamsource.streamflow.web.application.console.ConsoleService;
import se.streamsource.streamflow.web.application.contact.StreamflowContactLookupService;
import se.streamsource.streamflow.web.application.mail.MailService;
import se.streamsource.streamflow.web.application.management.CompositeMBean;
import se.streamsource.streamflow.web.application.management.DatasourceConfigurationManagerService;
import se.streamsource.streamflow.web.application.management.ErrorLogService;
import se.streamsource.streamflow.web.application.management.EventManagerService;
import se.streamsource.streamflow.web.application.management.LoggingService;
import se.streamsource.streamflow.web.application.management.ManagerComposite;
import se.streamsource.streamflow.web.application.management.ManagerService;
import se.streamsource.streamflow.web.application.management.ReindexOnStartupService;
import se.streamsource.streamflow.web.application.management.jmxconnector.JmxConnectorService;
import se.streamsource.streamflow.web.application.migration.StartupMigrationService;
import se.streamsource.streamflow.web.application.notification.NotificationService;
import se.streamsource.streamflow.web.application.organization.BootstrapAssembler;
import se.streamsource.streamflow.web.application.pdf.CasePdfGenerator;
import se.streamsource.streamflow.web.application.pdf.SubmittedFormPdfGenerator;
import se.streamsource.streamflow.web.application.security.AuthenticationFilter;
import se.streamsource.streamflow.web.application.security.AuthenticationFilterFactoryService;
import se.streamsource.streamflow.web.application.statistics.CaseStatisticsService;
import se.streamsource.streamflow.web.application.statistics.CaseStatisticsValue;
import se.streamsource.streamflow.web.application.statistics.FormFieldStatisticsValue;
import se.streamsource.streamflow.web.application.statistics.JdbcStatisticsStore;
import se.streamsource.streamflow.web.application.statistics.LoggingStatisticsStore;
import se.streamsource.streamflow.web.application.statistics.RelatedStatisticsValue;
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;

import javax.management.MBeanServer;

import static org.qi4j.api.common.Visibility.*;

/**
 * JAVADOC
 */
public class AppAssembler
{
   public void assemble( LayerAssembly layer )
         throws AssemblyException
   {
      console( layer.moduleAssembly( "Console" ) );
      migration( layer.moduleAssembly( "Migration" ) );

      if (layer.applicationAssembly().mode().equals( Application.Mode.production ))
      {
         management( layer.moduleAssembly( "Management" ) );
         notification( layer.moduleAssembly( "Notification" ) );
         mail( layer.moduleAssembly( "Mail" ) );
      }

      security( layer.moduleAssembly( "Security" ) );

      new BootstrapAssembler().assemble( layer.moduleAssembly( "Bootstrap" ) );

      statistics( layer.moduleAssembly( "Statistics" ) );

      contactLookup( layer.moduleAssembly( "Contact lookup" ) );

      pdf( layer.moduleAssembly( "Pdf" ) );
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
      module.addServices( MailService.class ).identifiedBy( "mail" ).instantiateOnStartup().visibleIn( Visibility.application );
   }

   private void notification( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( NotificationService.class )
            .identifiedBy( "notification" )
            .instantiateOnStartup()
            .visibleIn( layer );
   }

   private void statistics( ModuleAssembly module ) throws AssemblyException
   {
      if (module.layerAssembly().applicationAssembly().mode().equals( Application.Mode.production ))
      {
         module.addServices( CaseStatisticsService.class ).
               identifiedBy( "statistics" ).
               instantiateOnStartup().
               visibleIn( layer );
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
      module.addObjects( AuthenticationFilter.class );
      module.addValues(UserDetailsValue.class);
      module.addServices( AuthenticationFilterFactoryService.class )
            .identifiedBy( "authentication" )
            .instantiateOnStartup()
            .visibleIn( application );
   }

   private void management( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( CompositeMBean.class );
      module.addTransients( ManagerComposite.class );

      module.importServices( MBeanServer.class ).importedBy( MBeanServerImporter.class );
      module.addServices( ManagerService.class ).visibleIn( application ).instantiateOnStartup();

      module.addServices( ConfigurationManagerService.class ).instantiateOnStartup();
      module.addServices( DatasourceConfigurationManagerService.class ).instantiateOnStartup();

      module.addServices( JmxConnectorService.class ).identifiedBy( "jmxconnector" ).instantiateOnStartup();

      module.addServices( ReindexerService.class ).identifiedBy( "reindexer" ).visibleIn( layer );
      module.addServices( ReindexOnStartupService.class ).instantiateOnStartup();

      module.addServices( EventManagerService.class, DomainEventPlayerService.class ).instantiateOnStartup();
      module.addServices( ErrorLogService.class ).instantiateOnStartup();

      module.addServices( LoggingService.class ).instantiateOnStartup();
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
      }
   }

   private void console( ModuleAssembly module ) throws AssemblyException
   {
      module.addValues( ConsoleScriptValue.class, ConsoleResultValue.class ).visibleIn( application );

      module.addServices( ConsoleService.class ).visibleIn( application );
   }
}
