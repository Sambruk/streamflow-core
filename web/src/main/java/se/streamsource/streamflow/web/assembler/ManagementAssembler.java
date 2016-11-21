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

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.sql.DataSource;

import org.qi4j.api.cache.CacheOptions;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Transforms;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.library.jmx.JMXAssembler;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.infrastructure.circuitbreaker.jmx.CircuitBreakerManagement;
import se.streamsource.infrastructure.management.DatasourceConfigurationManagerService;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes;
import se.streamsource.streamflow.web.application.defaults.AvailabilityService;
import se.streamsource.streamflow.web.application.statistics.StatisticsStoreException;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.caselog.CaseLogEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLog;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;
import se.streamsource.streamflow.web.domain.structure.caze.Notes;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinition;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.note.NoteValue;
import se.streamsource.streamflow.web.domain.structure.note.NotesTimeLine;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.management.CompositeMBean;
import se.streamsource.streamflow.web.management.ErrorLogService;
import se.streamsource.streamflow.web.management.EventManagerService;
import se.streamsource.streamflow.web.management.HistoryCleanup;
import se.streamsource.streamflow.web.management.InstantMessagingAdminConfiguration;
import se.streamsource.streamflow.web.management.InstantMessagingAdminService;
import se.streamsource.streamflow.web.management.ManagerComposite;
import se.streamsource.streamflow.web.management.ManagerService;
import se.streamsource.streamflow.web.management.ReindexOnStartupService;
import se.streamsource.streamflow.web.management.UpdateBuilder;
import se.streamsource.streamflow.web.management.UpdateConfiguration;
import se.streamsource.streamflow.web.management.UpdateOperation;
import se.streamsource.streamflow.web.management.UpdateService;
import se.streamsource.streamflow.web.management.jmxconnector.JmxConnectorConfiguration;
import se.streamsource.streamflow.web.management.jmxconnector.JmxConnectorService;

/**
 * Assembler for management layer
 */
public class ManagementAssembler extends AbstractLayerAssembler
{
   final Logger logger = LoggerFactory.getLogger( ManagementAssembler.class.getName() );

   @Structure
   ModuleSPI moduleSPI;

   public void assemble( LayerAssembly layer ) throws AssemblyException
   {
      super.assemble( layer );
      jmx( layer.module( "JMX" ) );

      update( layer.module( "Update" ) );
   }

   private void jmx( ModuleAssembly module ) throws AssemblyException
   {
      new JMXAssembler().assemble( module );

      module.objects( CompositeMBean.class );
      module.transients( ManagerComposite.class );

      module.services( ManagerService.class, DatasourceConfigurationManagerService.class,
            ReindexOnStartupService.class, EventManagerService.class, ErrorLogService.class,
            CircuitBreakerManagement.class ).visibleIn( application ).instantiateOnStartup();

      module.services( ReindexerService.class ).identifiedBy( "reindexer" ).visibleIn( layer );

      module.services( JmxConnectorService.class ).identifiedBy( "jmxconnector" ).instantiateOnStartup();
      configuration().entities( JmxConnectorConfiguration.class ).visibleIn( Visibility.application );
      configuration().forMixin( JmxConnectorConfiguration.class ).declareDefaults().enabled().set( Boolean.TRUE );
      configuration().forMixin( JmxConnectorConfiguration.class ).declareDefaults().port().set( 1099 );

      module.services( InstantMessagingAdminService.class ).identifiedBy( "imadmin" ).instantiateOnStartup();
      configuration().entities( InstantMessagingAdminConfiguration.class ).visibleIn( Visibility.application );
      configuration().forMixin( InstantMessagingAdminConfiguration.class ).declareDefaults().enabled().set( Boolean.FALSE );
   }

   private void update( final ModuleAssembly update )
   {
      UpdateBuilder updateBuilder = new UpdateBuilder( "1.4.0.0" );
      updateBuilder.toVersion( "1.4.1" ).atStartup( new UpdateOperation()
      {
         public void update( Application app, Module module ) throws StatisticsStoreException
         {
            // Remove this code cause it breaks later version upgrades
         }
      } ).toVersion( "1.5.0.1" ).atStartup( new UpdateOperation()
      {

         public void update( Application app, Module module ) throws Exception
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "AddDefaultDatatypes" ) );

            try
            {
               OrganizationsEntity organizations = uow.get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
               Organization organization = organizations.organization().get();
               DatatypeDefinition newDatatype = organization
                     .createDatatypeDefinition( "http://www.w3.org/2006/vcard/ns#Email" );
               newDatatype.changeDescription( "Epost" );
               newDatatype.changeRegularExpression( "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$" );

               newDatatype = organization.createDatatypeDefinition( "http://www.w3.org/2006/vcard/ns#geo" );
               newDatatype.changeDescription( "Kartkoordinat" );
               newDatatype.changeRegularExpression( "\\d{5}\\.\\d{3},\\d{5}\\.\\d{3}" );

               newDatatype = organization.createDatatypeDefinition( "http://www.w3.org/2006/vcard/ns#fn" );
               newDatatype.changeDescription( "Namn" );

               newDatatype = organization.createDatatypeDefinition( "http://www.w3.org/2006/vcard/ns#street-address" );
               newDatatype.changeDescription( "Gatuadress" );
               newDatatype = organization.createDatatypeDefinition( "http://www.w3.org/2006/vcard/ns#postal-code" );
               newDatatype.changeDescription( "Postnummer" );
               newDatatype = organization.createDatatypeDefinition( "http://www.w3.org/2006/vcard/ns#locality" );
               newDatatype.changeDescription( "Postort" );

               newDatatype = organization.createDatatypeDefinition( "http://www.w3.org/2006/vcard/ns#tel" );
               newDatatype.changeDescription( "Telefon" );
               newDatatype = organization.createDatatypeDefinition( "http://www.w3.org/2006/vcard/ns#Cell" );

               newDatatype.changeDescription( "Mobilnummer" );

               uow.complete();
            } finally
            {
               uow.discard();
            }
         }
      } ).toVersion( "1.5.0.2" ).atStartup( new UpdateOperation()
      {
         public void update( Application app, Module module ) throws Exception
         {
            // reindex rdf and solr indexes since this version contains two solr core's.
            ManagerService mgrService = (ManagerService) module.serviceFinder().findService( ManagerService.class ).get();
            if (mgrService != null)
               mgrService.getManager().reindex();

            // DataSourceConfiguration has moved to SPI and java prefs have to reflect the structural change

            if (Preferences.userRoot().nodeExists( "/streamsource/streamflow/StreamflowServer/streamflowds" ))
            {
               Preferences preference = Preferences.userRoot().node( "/streamsource/streamflow/StreamflowServer/streamflowds" );
               preference.put( "type", "se.streamsource.infrastructure.database.DataSourceConfiguration" );

               preference.flush();
            }
         }
      } ).toVersion( "1.6.0.0" ).atStartup( new UpdateOperation()
      {

         public void update( Application app, final Module module ) throws Exception
         {
            // For each case create a new Notes association, create a NoteValue, put it into the notes list and delete the note from Notable.
            // Fetch a list of relevant case id's first and work with the list.
            // JDBM is not happy about committing stuff to the database
            // while traversing the index. ( results in random EOFExceptions!! )
            int count = 0;
            final List<String> caseIds = new ArrayList<String>();

            try
            {
               Input<EntityState, EntityStoreException> entities = ((EntityStore) module.serviceFinder().findService( EntityStore.class ).get()).entityStates( (ModuleSPI) module );
               entities.transferTo( Transforms.filter( new Specification<EntityState>()
               {
                  public boolean satisfiedBy( EntityState state )
                  {
                     return state.isOfType( TypeName.nameOf( CaseEntity.class ) ) &&
                           (state.getAssociation( QualifiedName.fromClass( Notes.Data.class, "notes" ) ) == null
                                 || state.getAssociation( QualifiedName.fromClass( CaseLoggable.Data.class, "caselog" ) ) == null);

                  }
               }, Outputs.withReceiver( new Receiver<EntityState, Throwable>()
               {
                  public void receive( EntityState state ) throws Throwable
                  {
                     caseIds.add( state.identity().identity() );
                  }
               } ) ) );

               logger.info( "Found " + caseIds.size() + " cases eligible for update migration." );
               UnitOfWork uow = null;

               ServiceReference<IdentityGenerator> identityGenerator = module.serviceFinder().findService( IdentityGenerator.class );
               for( String id : caseIds )
               {
                  try
                  {
                     if (uow == null)
                     {
                        uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.buildUsecase( "Upgrade_1.6.0.0" ).with( CacheOptions.NEVER ).newUsecase( ) );
                     }
                     CaseEntity caze = uow.get( CaseEntity.class, id );


                     if (caze.notes().get() == null)
                     {
                        // Create list of Notes
                        NotesTimeLine notesEntity = module.unitOfWorkFactory().currentUnitOfWork().newEntity( NotesTimeLine.class, identityGenerator.get().generate( Identity.class ) );
                        caze.notes().set( notesEntity );
                        ValueBuilder<NoteValue> noteValueBuilder = module.valueBuilderFactory().newValueBuilder( NoteValue.class );
                        noteValueBuilder.prototype().note().set( caze.note().get() );
                        noteValueBuilder.prototype().createdBy().set( EntityReference.getEntityReference( caze.createdBy().get() ) );
                        noteValueBuilder.prototype().createdOn().set( caze.createdOn().get() );

                        ((NotesTimeLine.Data) caze.notes().get()).notes().get().add( noteValueBuilder.newInstance() );

                        caze.note().set( "" );
                     }

                     if (caze.caselog().get() == null)
                     {
                        // Transform History to CaseLog
                        CaseLogEntity caseLog = module.unitOfWorkFactory().currentUnitOfWork().newEntity( CaseLogEntity.class, identityGenerator.get().generate( Identity.class ) );
                        caze.caselog().set( caseLog );
                        Conversation history = caze.history().get();
                        if (history != null)
                        {
                           for (Message message : ((Messages.Data) history).messages())
                           {
                              Message.Data messageData = (Message.Data) message;
                              ValueBuilder<CaseLogEntryValue> builder = module.valueBuilderFactory().newValueBuilder(
                                    CaseLogEntryValue.class );
                              builder.prototype().createdBy()
                                    .set( EntityReference.getEntityReference( messageData.sender().get() ) );
                              builder.prototype().createdOn().set( messageData.createdOn().get() );

                              if (messageData.body().get() != null && messageData.body().get().startsWith( "{" ))
                              {
                                 builder.prototype().entryType().set( CaseLogEntryTypes.system );
                              } else
                              {
                                 builder.prototype().entryType().set( CaseLogEntryTypes.custom );
                              }
                              builder.prototype().message().set( messageData.body().get() );
                              ((CaseLog.Data) caze.caselog().get()).addedEntry( null, builder.newInstance() );
                           }
                        }
                     }

                     count++;

                     if (count % 1000 == 0)
                     {
                        logger.info( " " + count + " cases notes and/or caselog migrated and about to commit" );
                        uow.complete();
                        uow = null;
                        logger.info( "Commit succeded." );
                     }
                  } catch (Throwable e)
                  {
                     uow.discard();
                     logger.error( e.getMessage() );
                     throw new RuntimeException( "Upgrade failed at case count " + count + " !", e );
                  }
               }

               // only try to commit if Outputs was not empty set
               // if we haven't received anything the uow will be null!
               if (uow != null)
                  uow.complete();
               logger.info( "Upgrade migration for 1.6.0.0 migrated " + count + " cases successfully." );

               // now we may open up for client trafik again - set the circuitbreaker to on
               // database is migrated and history was dereferenced before deleting.
               AvailabilityService availablilityService = (AvailabilityService) module.serviceFinder().findService( AvailabilityService.class ).get();
               availablilityService.getCircuitBreaker().turnOn();

               // Run refresh statistics only if we found any case's to migrate
               if (caseIds.size() > 0)
               {
                  ManagerService mgrService = (ManagerService) module.serviceFinder().findService( ManagerService.class ).get();
                  ServiceReference<DataSource> dataSource = module.serviceFinder().findService( DataSource.class );
                  try
                  {
                     if (dataSource != null && dataSource.isActive())
                        mgrService.getManager().refreshStatistics();
                     else
                        logger.info( "Could not refresh statistics, DataSource streamflowds is not active!" );
                  } catch (StatisticsStoreException e)
                  {
                     logger.info( "Could not refresh statistics", e );
                  }
               }
            } catch (Throwable e)
            {
               logger.error( e.getMessage() );
               throw new RuntimeException( "Upgrade failed at case count " + count + " !", e );
            }
         }
      } ).toVersion( "1.8.0.0" ).atStartup( new UpdateOperation()
      {
         public void update( Application app, Module module ) throws Exception
         {
            if (Preferences.userRoot().nodeExists( "/streamsource/streamflow/StreamflowServer/contactlookup" ))
            {
               Preferences preference = Preferences.userRoot().node( "/streamsource/streamflow/StreamflowServer/contactlookup" );
               preference.put( "type", "se.streamsource.streamflow.web.infrastructure.plugin.ContactLookupServiceConfiguration" );

               preference.flush();
            }

            int count = 0;
            final List<String> accessPointIds = new ArrayList<String>();

            try
            {
               Input<EntityState, EntityStoreException> entities = ((EntityStore) module.serviceFinder().findService( EntityStore.class ).get()).entityStates( (ModuleSPI) module );
               entities.transferTo( Transforms.filter( new Specification<EntityState>()
               {
                  public boolean satisfiedBy( EntityState state )
                  {
                     return state.isOfType( TypeName.nameOf( AccessPointEntity.class ) ) &&
                           (state.getManyAssociation( QualifiedName.fromClass( SelectedForms.Data.class, "selectedForms" ) ).count() > 0
                           &&  !(Boolean)state.getProperty( QualifiedName.fromClass( Removable.Data.class, "removed" ) ) );

                  }
               }, Outputs.withReceiver( new Receiver<EntityState, Throwable>()
               {
                  public void receive( EntityState state ) throws Throwable
                  {
                     accessPointIds.add( state.identity().identity() );
                  }
               } ) ) );

               logger.info( "Found " + accessPointIds.size() + " access points eligible for update migration." );
               UnitOfWork uow = null;

               for( String id : accessPointIds )
               {
                  try
                  {
                     if (uow == null)
                     {
                        uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.buildUsecase( "Upgrade_1.8.0.0" ).with( CacheOptions.NEVER ).newUsecase( ) );
                     }
                     AccessPointEntity accessPointEntity = uow.get( AccessPointEntity.class, id );

                     List<RequiredSignatureValue> signatureList = ((RequiredSignatures.Data) ((SelectedForms.Data) accessPointEntity).selectedForms().get( 0 )).requiredSignatures().get();
                     if( signatureList != null && !signatureList.isEmpty() && ((RequiredSignatures.Data)accessPointEntity).requiredSignatures().get().isEmpty() )
                     {
                        // Found a suitable signature on existing form and no signature present on access point - move it to access point instead.
                        RequiredSignatureValue signatureValue = signatureList.get( 0 );
                        ValueBuilder<RequiredSignatureValue> signatureBuilder = signatureValue.buildWith();
                        signatureBuilder.prototype().active().set( Boolean.TRUE );
                        signatureBuilder.prototype().mandatory().set( Boolean.TRUE );
                        signatureBuilder.prototype().formid().set( ((Identity)accessPointEntity.selectedForms().get( 0 )).identity().get() );
                        signatureBuilder.prototype().formdescription().set( accessPointEntity.selectedForms().get( 0 ).getDescription() );
                        accessPointEntity.createRequiredSignature( signatureBuilder.newInstance() );
                        logger.info( "Upgrade migration for 1.8.0.0 actually moved a signature to access point: " + id );
                     }
                     count++;
                  } catch (Throwable e)
                  {
                     uow.discard();
                     logger.error( e.getMessage() );
                     throw new RuntimeException( "Upgrade failed at access point count " + count + " !", e );
                  }
               }

               if (uow != null)
                  uow.complete();
               logger.info( "Upgrade migration for 1.8.0.0 migrated " + count + " access points successfully." );
            }
            catch (Throwable e)
            {
               logger.error( e.getMessage() );
               throw new RuntimeException( "Upgrade failed at access point count " + count + " !", e );
            }
         }
      } );

      update.services( UpdateService.class ).identifiedBy( "update" ).setMetaInfo( updateBuilder )
            .visibleIn( layer ).instantiateOnStartup();
      update.objects( HistoryCleanup.class );
      configuration().entities( UpdateConfiguration.class ).visibleIn( application );
      // default value for first installation has to be the same version as the UpdateBuilder start version.
      configuration().forMixin( UpdateConfiguration.class ).declareDefaults().lastStartupVersion().set( "1.4.0.0" );

   }
}
