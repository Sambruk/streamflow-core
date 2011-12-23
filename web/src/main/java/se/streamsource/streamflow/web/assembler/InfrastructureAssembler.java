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

package se.streamsource.streamflow.web.assembler;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.entitystore.jdbm.JdbmEntityStoreService;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.RdfIndexingEngineService;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.library.rdf.repository.NativeRepositoryService;
import org.qi4j.migration.MigrationConfiguration;
import org.qi4j.migration.MigrationEventLogger;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import se.streamsource.dci.restlet.client.ClientAssembler;
import se.streamsource.infrastructure.database.DataSourceService;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.factory.ApplicationEventFactoryService;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.factory.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.time.TimeService;
import se.streamsource.streamflow.server.plugin.address.StreetList;
import se.streamsource.streamflow.server.plugin.address.StreetValue;
import se.streamsource.streamflow.server.plugin.contact.ContactAddressValue;
import se.streamsource.streamflow.server.plugin.contact.ContactEmailValue;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactPhoneValue;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStoreService;
import se.streamsource.streamflow.web.infrastructure.caching.CachingServiceComposite;
import se.streamsource.streamflow.web.infrastructure.database.LiquibaseConfiguration;
import se.streamsource.streamflow.web.infrastructure.database.LiquibaseService;
import se.streamsource.streamflow.web.infrastructure.database.ServiceInstanceImporter;
import se.streamsource.streamflow.web.infrastructure.event.JdbmApplicationEventStoreService;
import se.streamsource.streamflow.web.infrastructure.event.JdbmEventStoreService;
import se.streamsource.streamflow.web.infrastructure.event.MemoryApplicationEventStoreService;
import se.streamsource.streamflow.web.infrastructure.event.MemoryEventStoreService;
import se.streamsource.streamflow.web.infrastructure.index.EmbeddedSolrService;
import se.streamsource.streamflow.web.infrastructure.index.SolrQueryService;
import se.streamsource.streamflow.web.infrastructure.logging.LoggingService;
import se.streamsource.streamflow.web.infrastructure.plugin.address.StreetAddressLookupService;
import se.streamsource.streamflow.web.infrastructure.plugin.contact.ContactLookupService;
import se.streamsource.streamflow.web.infrastructure.plugin.map.KartagoMapService;
import se.streamsource.streamflow.web.rest.resource.EventsCommandResult;

import javax.sql.DataSource;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * JAVADOC
 */
public class InfrastructureAssembler
   extends AbstractLayerAssembler
{
   public void assemble( LayerAssembly layer )
         throws AssemblyException
   {
      super.assemble( layer );

      logging (layer.module("Logging"));
      caching( layer.module( "Caching" ) );
      database( layer.module( "Database" ) );
      entityStore( layer.module( "Entity store" ) );
      entityFinder( layer.module( "Entity finder" ) );
      events( layer.module( "Events" ) );
      searchEngine( layer.module( "Search engine" ) );
      attachments( layer.module( "Attachments store" ) );
      plugins( layer.module( "Plugins" ) );
   }

   private void logging( ModuleAssembly module ) throws AssemblyException
   {
      module.services( LoggingService.class ).instantiateOnStartup();
   }

   private void plugins( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      new ClientAssembler().assemble( moduleAssembly );

      Application.Mode mode = moduleAssembly.layer().application().mode();
      if (!mode.equals( Application.Mode.test ))
      {
         moduleAssembly.services( ContactLookupService.class ).
               identifiedBy( "contactlookup" ).
               visibleIn( Visibility.application ).
               instantiateOnStartup();

         moduleAssembly.services( KartagoMapService.class ).
               identifiedBy( "kartagomap" ).
               visibleIn( Visibility.application ).
               instantiateOnStartup();

         moduleAssembly.services( StreetAddressLookupService.class ).
               identifiedBy( "streetaddresslookup" ).
               visibleIn( Visibility.application ).
               instantiateOnStartup();

         moduleAssembly.values( ContactList.class,
               ContactValue.class,
               ContactAddressValue.class,
               ContactEmailValue.class,
               ContactPhoneValue.class,
               StreetValue.class,
               StreetList.class).visibleIn( Visibility.application );
      }

   }

   private void caching( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.services( CachingServiceComposite.class ).visibleIn( Visibility.application );

 //     moduleAssembly.services( EhCachePoolService.class ).visibleIn( Visibility.layer );
   }

   private void attachments( ModuleAssembly module ) throws AssemblyException
   {
      module.services( AttachmentStoreService.class ).identifiedBy( "attachments" ).visibleIn( Visibility.application );
   }

   private void searchEngine( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layer().application().mode();
      if (!mode.equals( Application.Mode.test ))
      {
         module.services( EmbeddedSolrService.class ).visibleIn( Visibility.application ).instantiateOnStartup();
         module.services( SolrQueryService.class ).visibleIn( Visibility.application ).identifiedBy( "solr" ).instantiateOnStartup();

         module.objects( EntityStateSerializer.class );
      }
   }

   private void events( ModuleAssembly module ) throws AssemblyException
   {
      module.importedServices( EventsCommandResult.class ).importedBy( NEW_OBJECT ).visibleIn( Visibility.application );
      module.objects( EventsCommandResult.class );
      module.values( TransactionDomainEvents.class, DomainEvent.class ).visibleIn( Visibility.application );
      module.values( TransactionApplicationEvents.class, ApplicationEvent.class ).visibleIn( Visibility.application );
      module.services( DomainEventFactoryService.class ).visibleIn( Visibility.application );
      module.services( ApplicationEventFactoryService.class ).visibleIn( Visibility.application );
      module.objects( TimeService.class );
      module.importedServices( TimeService.class ).importedBy( NewObjectImporter.class );

      if (module.layer().application().mode() == Application.Mode.production)
      {
         module.services( JdbmEventStoreService.class ).identifiedBy( "eventstore" ).taggedWith( "domain" ).visibleIn( Visibility.application );
         module.services( JdbmApplicationEventStoreService.class ).identifiedBy( "applicationeventstore" ).visibleIn( Visibility.application );
      } else
      {
         module.services( MemoryEventStoreService.class ).identifiedBy( "eventstore" ).visibleIn( Visibility.application );
         module.services( MemoryApplicationEventStoreService.class ).identifiedBy( "applicationeventstore" ).visibleIn( Visibility.application );
      }
   }

   private void entityFinder( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layer().application().mode();
      if (mode.equals( Application.Mode.development ) || mode.equals( Application.Mode.test ))
      {
         // In-memory store
         module.services( MemoryRepositoryService.class ).instantiateOnStartup().visibleIn( Visibility.application ).identifiedBy( "rdf-repository" );
      } else if (mode.equals( Application.Mode.production ))
      {
         // Native storage
         module.services( NativeRepositoryService.class ).visibleIn( Visibility.application ).instantiateOnStartup().identifiedBy( "rdf-repository" );
         configuration().entities( NativeConfiguration.class ).visibleIn( Visibility.application );
      }

      module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
      module.services( RdfIndexingEngineService.class ).instantiateOnStartup().visibleIn( Visibility.application );
//            withConcerns( PerformanceLogConcern.class );
      module.services( RdfQueryParserFactory.class );
   }

   private void entityStore( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layer().application().mode();
      if (mode.equals( Application.Mode.development ))
      {
         // In-memory store
         module.services( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
      } else if (mode.equals( Application.Mode.test ))
      {
         // In-memory store
         module.services( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
      } else if (mode.equals( Application.Mode.production ))
      {
         // JDBM storage
         module.services( JdbmEntityStoreService.class ).identifiedBy( "data" ).visibleIn( Visibility.application );
//               withConcerns( EntityStorePerformanceCheck.class );
         module.services( UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );

         configuration().entities( JdbmConfiguration.class ).visibleIn( Visibility.application );

         // Migration service

         new MigrationAssembler().assemble(module);

         configuration().entities(MigrationConfiguration.class).visibleIn(Visibility.application);

         module.objects( MigrationEventLogger.class );
         module.importedServices( MigrationEventLogger.class ).importedBy( NEW_OBJECT );
      }
   }

   private void database( ModuleAssembly module ) throws AssemblyException
   {
      module.services( DataSourceService.class ).identifiedBy( "datasource" ).visibleIn( Visibility.application );
      module.importedServices( DataSource.class ).
            importedBy( ServiceInstanceImporter.class ).
            setMetaInfo( "datasource" ).
            identifiedBy( "streamflowds" ).visibleIn( Visibility.application );

      Application.Mode mode = module.layer().application().mode();
      if (mode.equals( Application.Mode.production ))
      {
         // Liquibase migration
         module.services( LiquibaseService.class ).instantiateOnStartup().visibleIn( Visibility.application );
         ModuleAssembly config = module.layer().application().layer( "Configuration" ).module( "DefaultConfiguration" );
         config.entities( LiquibaseConfiguration.class ).visibleIn( Visibility.application );
         config.forMixin( LiquibaseConfiguration.class ).declareDefaults().enabled().set(true);
         config.forMixin( LiquibaseConfiguration.class ).declareDefaults().changeLog().set("changelog.xml");
      }
   }

/*
   public abstract static class EntityStorePerformanceCheck
         extends ConcernOf<EntityStoreSPI>
         implements EntityStoreSPI
   {
      public StateCommitter applyChanges( EntityStoreUnitOfWork unitOfWork, Iterable<EntityState> state, String version, long lastModified )
      {
         long start = System.nanoTime();
         try
         {
            return next.applyChanges( unitOfWork, state, version, lastModified );
         } finally
         {
            long end = System.nanoTime();
            long timeMicro = (end - start) / 1000;
            double timeMilli = timeMicro / 1000.0;
            System.out.println("Apply changes"+":"+ timeMilli );
         }
      }
   }

   public static class PerformanceLogConcern
         extends GenericConcern
   {
      public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
      {
         long start = System.nanoTime();
         try
         {
            return next.invoke( proxy, method, args );
         } finally
         {
            long end = System.nanoTime();
            long timeMicro = (end - start) / 1000;
            double timeMilli = timeMicro / 1000.0;
            System.out.println(method.getName()+":"+ timeMilli );
         }
      }
   }
*/
}
