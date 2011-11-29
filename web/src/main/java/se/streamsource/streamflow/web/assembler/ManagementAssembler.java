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

import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.ServiceReference;
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
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.infrastructure.circuitbreaker.jmx.CircuitBreakerManagement;
import se.streamsource.infrastructure.management.DatasourceConfigurationManagerService;
import se.streamsource.streamflow.web.application.statistics.StatisticsStoreException;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.MessagesContext;
import se.streamsource.streamflow.web.domain.entity.caselog.CaseLogEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLog;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinition;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.management.CompositeMBean;
import se.streamsource.streamflow.web.management.ErrorLogService;
import se.streamsource.streamflow.web.management.EventManagerService;
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

   public void assemble(LayerAssembly layer) throws AssemblyException
   {
      super.assemble( layer );
      jmx( layer.module( "JMX" ) );

      update( layer.module( "Update" ) );
   }

   private void jmx(ModuleAssembly module) throws AssemblyException
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

      module.services( InstantMessagingAdminService.class ).identifiedBy( "imadmin" ).instantiateOnStartup();
      configuration().entities( InstantMessagingAdminConfiguration.class ).visibleIn( Visibility.application );
   }

   private void update(ModuleAssembly update)
   {
     UpdateBuilder updateBuilder = new UpdateBuilder("1.4.0.0");
      updateBuilder.toVersion( "1.4.1" ).atStartup( new UpdateOperation()
      {
         public void update(Application app, Module module) throws StatisticsStoreException
         {
            ManagerService mgrService = (ManagerService) module.serviceFinder().findService( ManagerService.class ).get();
            try
            {
               mgrService.getManager().refreshStatistics();
            } catch (StatisticsStoreException e)
            {
               logger.info( "Could not refresh statistics", e );
            }
         }
      } ).toVersion( "1.5.0.1" ).atStartup( new UpdateOperation()
      {

         public void update(Application app, Module module) throws Exception
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "AddDefaultDatatypes") );

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
               newDatatype.changeRegularExpression( "\\d{5},\\d{3};\\d{5},\\d{3}" );

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
            if( mgrService != null )
               mgrService.getManager().reindex();

            // DataSourceConfiguration has moved to SPI and java prefs have to reflect the structural change

            if( Preferences.userRoot().nodeExists( "/streamsource/streamflow/StreamflowServer/streamflowds" ))
            {
               Preferences preference = Preferences.userRoot().node( "/streamsource/streamflow/StreamflowServer/streamflowds" );
               preference.put( "type", "se.streamsource.infrastructure.database.DataSourceConfiguration" );

               preference.flush();
            }
         }
      } ).toVersion(  "1.5.0.3" ).atStartup( new UpdateOperation()
      {
         
         public void update(Application app, Module module) throws Exception
         {
            // For each case create a new Notes association, create a NoteValue, put it into the notes list and delete the note from Notable.
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase("Upgrade_1.5.0.3") );
            try {
               Query<CaseEntity> caseQuery = module.queryBuilderFactory().newQueryBuilder( CaseEntity.class ).newQuery( uow );
               for(CaseEntity caze : caseQuery )
               {
                  ServiceReference<IdentityGenerator> identityGenerator = module.serviceFinder().findService( IdentityGenerator.class );
                  CaseLogEntity caseLog = module.unitOfWorkFactory().currentUnitOfWork().newEntity( CaseLogEntity.class, identityGenerator.get().generate( Identity.class ) );
                  caze.caselog().set( caseLog );
                  Conversation history = ((History.Data)caze).history().get();
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
                        builder.prototype().authorizationType().set( CaseLogEntryValue.AuthorizationType.user );

                        if (messageData.body().get() != null && messageData.body().get().startsWith( "{" ))
                        {
                           builder.prototype().entryType().set( CaseLogEntryValue.EntryType.system );
                        } else
                        {
                           builder.prototype().entryType().set( CaseLogEntryValue.EntryType.custom );
                        }
                        builder.prototype().message().set( messageData.body().get() );
                        ((CaseLog.Data) caze.caselog().get()).addedEntry( null, builder.newInstance() );
                     }
                  } 
               }
               uow.complete();
            } catch (Throwable e) {
               uow.discard();
               throw new RuntimeException( "Upgrade failed!", e );
            }
         }
      });
      update.services( UpdateService.class ).identifiedBy( "update" ).setMetaInfo( updateBuilder )
            .visibleIn( layer ).instantiateOnStartup();
      configuration().entities( UpdateConfiguration.class ).visibleIn( application );
   }
}
