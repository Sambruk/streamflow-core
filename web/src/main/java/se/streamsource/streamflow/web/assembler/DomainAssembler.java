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

import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.entitystore.jdbm.JdbmEntityStoreService;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.migration.MigrationConfiguration;
import org.qi4j.migration.MigrationEventLogger;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import se.streamsource.dci.restlet.client.ClientAssembler;
import se.streamsource.infrastructure.database.DataSourceService;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchConfiguration;
import se.streamsource.infrastructure.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import se.streamsource.infrastructure.index.elasticsearch.assembly.ESMemoryIndexQueryAssembler;
import se.streamsource.streamflow.api.assembler.ClientAPIAssembler;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.factory.ApplicationEventFactoryService;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.factory.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.time.TimeService;
import se.streamsource.streamflow.server.plugin.address.StreetList;
import se.streamsource.streamflow.server.plugin.address.StreetValue;
import se.streamsource.streamflow.server.plugin.contact.*;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.entity.caselog.CaseLogEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.ResolutionEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.MessageEntity;
import se.streamsource.streamflow.web.domain.entity.customer.CustomerEntity;
import se.streamsource.streamflow.web.domain.entity.customer.CustomersEntity;
import se.streamsource.streamflow.web.domain.entity.external.ShadowCaseEntity;
import se.streamsource.streamflow.web.domain.entity.form.DatatypeDefinitionEntity;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.FieldGroupEntity;
import se.streamsource.streamflow.web.domain.entity.form.FieldGroupFieldInstanceEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormDraftEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.entity.form.PageEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.note.NotesTimeLineEntity;
import se.streamsource.streamflow.web.domain.entity.organization.*;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectRoleEntity;
import se.streamsource.streamflow.web.domain.entity.task.DoubleSignatureTaskEntity;
import se.streamsource.streamflow.web.domain.entity.user.EmailUserEntity;
import se.streamsource.streamflow.web.domain.entity.user.EndUserEntity;
import se.streamsource.streamflow.web.domain.entity.user.PerspectiveEntity;
import se.streamsource.streamflow.web.domain.entity.user.ProxyUserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedPageValue;
import se.streamsource.streamflow.web.domain.structure.note.NoteValue;
import se.streamsource.streamflow.web.domain.structure.organization.ParticipantRolesValue;
import se.streamsource.streamflow.web.domain.structure.project.PermissionValue;
import se.streamsource.streamflow.web.domain.util.FormVisibilityRuleValidator;
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
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;
import se.streamsource.streamflow.web.infrastructure.index.SolrQueryService;
import se.streamsource.streamflow.web.infrastructure.logging.LoggingService;
import se.streamsource.streamflow.web.infrastructure.plugin.address.StreetAddressLookupService;
import se.streamsource.streamflow.web.infrastructure.plugin.contact.ContactLookupService;
import se.streamsource.streamflow.web.infrastructure.plugin.map.KartagoMapService;
import se.streamsource.streamflow.web.rest.resource.EventsCommandResult;

import javax.sql.DataSource;

import static org.qi4j.api.common.Visibility.*;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

/**
 * JAVADOC
 */
public class DomainAssembler
    extends AbstractLayerAssembler
{
   public void assemble(LayerAssembly layer)
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
       attachmentstore( layer.module( "Attachments store" ) );
       plugins( layer.module( "Plugins" ) );


      ModuleAssembly api = layer.module("API");
      new ClientAPIAssembler().assemble(api);

      conversations(layer.module("Conversations"));
      forms( layer.module("Forms") );
      groups( layer.module("Groups") );
      labels( layer.module("Labels") );
      organizations( layer.module("Organizations") );
      projects( layer.module("Projects") );
      roles( layer.module("Roles") );
      caselog( layer.module("Caselog") );
      cases( layer.module("Cases") );
      caseTypes( layer.module("Casetypes") );
      users( layer.module("Users") );
      attachments( layer.module("Attachments") );
      notes( layer.module( "Notes" ));
      tasks( layer.module("Tasks") );
      external( layer.module( "External" ) );
      util( layer.module(  "Util" ) );

      // All values are public
      layer.values(Specifications.<Object>TRUE()).visibleIn(Visibility.application);

      // All entities are public
      layer.entities(Specifications.<Object>TRUE()).visibleIn(Visibility.application);

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
                    StreetList.class ).visibleIn( Visibility.application );
        }

    }

    private void caching( ModuleAssembly moduleAssembly ) throws AssemblyException
    {
        moduleAssembly.services( CachingServiceComposite.class ).visibleIn( Visibility.application );

        //     moduleAssembly.services( EhCachePoolService.class ).visibleIn( Visibility.layer );
    }

    private void attachmentstore( ModuleAssembly module ) throws AssemblyException
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
            //.withConcerns( SolrPerformanceLogConcern.class );

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
            //module.services( MemoryRepositoryService.class ).instantiateOnStartup().visibleIn( Visibility.application ).identifiedBy( "rdf-repository" );
            new ESMemoryIndexQueryAssembler().withVisibility(Visibility.application)
                    .withConfigModule( module ).withConfigVisibility(Visibility.application).assemble(module);
        } else if (mode.equals( Application.Mode.production ))
        {
            // Native storage
            //module.services( NativeRepositoryService.class ).visibleIn( Visibility.application ).instantiateOnStartup().identifiedBy( "rdf-repository" );
            configuration().entities( NativeConfiguration.class ).visibleIn( Visibility.application );
            configuration().entities( ElasticSearchConfiguration.class ).visibleIn(Visibility.application);
            new ESFilesystemIndexQueryAssembler().withVisibility(Visibility.application)
                    .withConfigModule(module).withConfigVisibility(Visibility.application).assemble(module);
        }
        //configuration().entities( ElasticSearchConfiguration.class ).visibleIn( Visibility.application );
        //module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
        //module.services( RdfIndexingEngineService.class ).instantiateOnStartup().visibleIn( Visibility.application );
        //.withConcerns( RdfPerformanceLogConcern.class );

        //module.services( RdfQueryParserFactory.class );
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
            //.withConcerns( EntityStorePerformanceCheck.class );
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
        module.services( DataSourceService.class ).identifiedBy( "datasource" ).instantiateOnStartup().visibleIn( Visibility.application );
        module.importedServices( DataSource.class ).
                importedBy( ServiceInstanceImporter.class ).
                setMetaInfo( "datasource" ).
                identifiedBy( "streamflowds" ).visibleIn( Visibility.application );


        Application.Mode mode = module.layer().application().mode();
        if (mode.equals( Application.Mode.production ))
        {
            // Liquibase migration
            module.services( LiquibaseService.class ).identifiedBy( "liquibase" ).instantiateOnStartup().visibleIn( Visibility.application );
            ModuleAssembly config = module.layer().application().layer( "Configuration" ).module( "DefaultConfiguration" );
            config.entities( LiquibaseConfiguration.class ).visibleIn( Visibility.application );
            config.forMixin( LiquibaseConfiguration.class ).declareDefaults().changeLog().set("changelog.xml");
        }
    }


    /*public abstract static class EntityStorePerformanceCheck
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

    public static class RdfPerformanceLogConcern
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
             System.out.println("RDF." + method.getName()+":"+ timeMilli );
          }
       }
    }

    public static class SolrPerformanceLogConcern
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
             System.out.println("SOLR." + method.getName()+":"+ timeMilli );
          }
       }
    }*/


    private void util( ModuleAssembly module )
   {
      module.objects( FormVisibilityRuleValidator.class ).visibleIn( Visibility.application );
   }

   private void external( ModuleAssembly module )
   {
      module.entities( ShadowCaseEntity.class ).visibleIn( application );
   }

   private void tasks( ModuleAssembly module )
   {
      module.entities( DoubleSignatureTaskEntity.class ).visibleIn( application );
      module.values( EmailValue.class ).visibleIn( application );
   }

   private void notes( ModuleAssembly module )
   {
      module.entities( NotesTimeLineEntity.class ).visibleIn( application );
      module.values( NoteValue.class ).visibleIn( application );
   }

   private void attachments(ModuleAssembly module) throws AssemblyException
   {
      module.entities(AttachmentEntity.class).visibleIn( application );
      module.values(AttachedFileValue.class).visibleIn( application );
   }

   private void users(ModuleAssembly module) throws AssemblyException
   {
      module.entities( UsersEntity.class, UserEntity.class, EmailUserEntity.class, ProxyUserEntity.class, EndUserEntity.class,
            PerspectiveEntity.class, CustomersEntity.class, CustomerEntity.class ).visibleIn( application );

      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor("solrquery", "");
      namedQueries.addQuery(queryDescriptor);

      module.importedServices(NamedEntityFinder.class).
            importedBy( ServiceSelectorImporter.class ).
            setMetaInfo( ServiceQualifier.withId( "solr" ) ).
            setMetaInfo( namedQueries );
   }

   private void caseTypes(ModuleAssembly module) throws AssemblyException
   {
      module.entities(CaseTypeEntity.class, ResolutionEntity.class).visibleIn( Visibility.application );
   }

   private void caselog(ModuleAssembly module) throws AssemblyException
   {
      module.entities(
              CaseLogEntity.class).visibleIn( application );

      module.values(CaseLogEntryValue.class).visibleIn( application );
   }

   private void cases(ModuleAssembly module) throws AssemblyException
   {
      module.entities(CaseEntity.class).visibleIn( Visibility.application );
   }

   private void roles(ModuleAssembly module) throws AssemblyException
   {
      module.entities(RoleEntity.class).visibleIn( Visibility.application );
   }

   private void projects(ModuleAssembly module) throws AssemblyException
   {
      module.entities(
              ProjectRoleEntity.class,
              ProjectEntity.class).visibleIn( application );

      module.values(PermissionValue.class).visibleIn( application );
   }

   private void organizations(ModuleAssembly module) throws AssemblyException
   {
      module.entities(OrganizationsEntity.class, OrganizationEntity.class,
              OrganizationalUnitEntity.class, AccessPointEntity.class, EmailAccessPointEntity.class,
            PriorityEntity.class, IntegrationPointEntity.class, MailRestrictionEntity.class,
            GlobalCaseIdStateEntity.class).visibleIn( application );
      module.values(ParticipantRolesValue.class).visibleIn( Visibility.application );
   }

   private void labels(ModuleAssembly module) throws AssemblyException
   {
      module.entities(LabelEntity.class).visibleIn( application );
   }

   private void groups(ModuleAssembly module) throws AssemblyException
   {
      module.entities(GroupEntity.class).visibleIn( application );
   }

   private void forms(ModuleAssembly module) throws AssemblyException
   {
      module.entities(
              FormEntity.class,
              FormDraftEntity.class,
              FieldEntity.class,
              PageEntity.class,
              FieldGroupFieldInstanceEntity.class,
              DatatypeDefinitionEntity.class,
              FieldGroupEntity.class
      ).visibleIn(Visibility.application);

      module.values(SubmittedFormValue.class, SubmittedPageValue.class, SubmittedFieldValue.class).visibleIn(Visibility.application);
   }

   private void conversations(ModuleAssembly module) throws AssemblyException
   {
      module.entities(
              ConversationEntity.class,
              MessageEntity.class).visibleIn( Visibility.application );
   }
}
