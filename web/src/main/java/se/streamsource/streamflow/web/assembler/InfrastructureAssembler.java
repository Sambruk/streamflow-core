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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmEntityStoreService;
import org.qi4j.entitystore.map.StateStore;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.RdfIndexingEngineService;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.library.rdf.repository.NativeRepositoryService;
import org.qi4j.migration.MigrationService;
import org.qi4j.migration.Migrator;
import org.qi4j.migration.assembly.EntityMigrationOperation;
import org.qi4j.migration.assembly.MigrationBuilder;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.event.TimeService;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.memory.MemoryEventStoreService;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStoreService;
import se.streamsource.streamflow.web.infrastructure.database.DataSourceService;
import se.streamsource.streamflow.web.infrastructure.database.LiquibaseService;
import se.streamsource.streamflow.web.infrastructure.database.ServiceInstanceImporter;
import se.streamsource.streamflow.web.infrastructure.event.EventSourceService;
import se.streamsource.streamflow.web.infrastructure.event.JdbmEventStoreService;
import se.streamsource.streamflow.web.infrastructure.index.EmbeddedSolrService;
import se.streamsource.streamflow.web.infrastructure.index.SolrQueryService;

import javax.sql.DataSource;

/**
 * JAVADOC
 */
public class InfrastructureAssembler
{
   public void assemble( LayerAssembly layer)
         throws AssemblyException
   {
      database(layer.moduleAssembly( "Database" ));
      entityStore(layer.moduleAssembly( "Entity store" ));
      entityFinder(layer.moduleAssembly( "Entity finder" ));
      events(layer.moduleAssembly( "Events" ));
      searchEngine(layer.moduleAssembly( "Search engine" ));
      attachments(layer.moduleAssembly( "Attachments store" ));
   }

   private void attachments( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( AttachmentStoreService.class ).identifiedBy( "attachments" ).visibleIn( Visibility.application );
   }

   private void searchEngine( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (!mode.equals( Application.Mode.test ))
      {
         module.addServices( EmbeddedSolrService.class ).visibleIn( Visibility.application ).instantiateOnStartup();
         module.addServices( SolrQueryService.class ).visibleIn( Visibility.application ).identifiedBy( "solr" ).instantiateOnStartup();

         module.addObjects( EntityStateSerializer.class );
      }
   }

   private void events( ModuleAssembly module ) throws AssemblyException
   {
      module.addValues( TransactionEvents.class, DomainEvent.class ).visibleIn( Visibility.application );
      module.addServices( EventSourceService.class ).identifiedBy( "eventsource" ).visibleIn( Visibility.application );
      module.addServices( DomainEventFactoryService.class ).visibleIn( Visibility.application );
      module.addObjects( TimeService.class );
      module.importServices( TimeService.class ).importedBy( NewObjectImporter.class );

      if (module.layerAssembly().applicationAssembly().mode() == Application.Mode.production)
         module.addServices( JdbmEventStoreService.class ).identifiedBy( "eventstore" ).visibleIn( Visibility.application );
      else
         module.addServices( MemoryEventStoreService.class ).identifiedBy( "eventstore" ).visibleIn( Visibility.application );
   }

   private void entityFinder( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (mode.equals( Application.Mode.development ) || mode.equals( Application.Mode.test ))
      {
         // In-memory store
         module.addServices( MemoryRepositoryService.class ).instantiateOnStartup().visibleIn( Visibility.application ).identifiedBy( "rdf-repository" );
      } else if (mode.equals( Application.Mode.production ))
      {
         // Native storage
         module.addServices( NativeRepositoryService.class ).visibleIn( Visibility.application ).instantiateOnStartup().identifiedBy( "rdf-repository" );
      }

      module.addObjects( EntityStateSerializer.class, EntityTypeSerializer.class );
      module.addServices( RdfIndexingEngineService.class ).instantiateOnStartup().visibleIn( Visibility.application );
      module.addServices( RdfQueryParserFactory.class);
   }

   private void entityStore( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (mode.equals( Application.Mode.development ))
      {
         // In-memory store
         module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
      } else if (mode.equals( Application.Mode.test ))
      {
         // In-memory store
         module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
      } else if (mode.equals( Application.Mode.production ))
      {
         // JDBM storage
         module.addServices( JdbmEntityStoreService.class ).identifiedBy( "data" ).visibleIn( Visibility.application );
         module.addServices( UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );

         // Migration service
         // Enter all migration rules here
         // To-version should be of the form:
         // <major-version>.<minor-version>.<sprint>.<Svn-revision>
         // This way we can control how migrations are done from one
         // revision to the next.
         // ATTENTION: it is not possible to add toVersion without any rules!!
         MigrationBuilder migrationBuilder = new MigrationBuilder( "0.0" );
         migrationBuilder.
               toVersion( "0.1.14.357" ).
               renameEntity( "se.streamsource.streamflow.web.domain.project.RoleEntity",
                     "se.streamsource.streamflow.web.domain.project.ProjectRoleEntity" ).
               forEntities( "se.streamsource.streamflow.web.domain.organization.OrganizationEntity",
                     "se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity" ).
               renameManyAssociation( "roles", "projectRoles" ).
               end().

               toVersion("0.2.18.0").
               toVersion( "0.3.20.962" ).
               renamePackage( "se.streamsource.streamflow.web.domain.form", "se.streamsource.streamflow.web.domain.entity.form" ).
               withEntities( "FieldEntity",
                     "FieldTemplateEntity",
                     "FormEntity",
                     "FormTemplateEntity").
               end().
               renameEntity( "se.streamsource.streamflow.web.domain.label.LabelEntity","se.streamsource.streamflow.web.domain.entity.label.LabelEntity" ).
               renamePackage("se.streamsource.streamflow.web.domain.organization", "se.streamsource.streamflow.web.domain.entity.organization").
               withEntities( "OrganizationalUnitEntity",
                     "OrganizationEntity",
                     "OrganizationsEntity").
               end().
               renameEntity( "se.streamsource.streamflow.web.domain.group.GroupEntity", "se.streamsource.streamflow.web.domain.entity.organization.GroupEntity" ).
               renameEntity( "se.streamsource.streamflow.web.domain.role.RoleEntity", "se.streamsource.streamflow.web.domain.entity.organization.RoleEntity" ).
               renamePackage( "se.streamsource.streamflow.web.domain.project", "se.streamsource.streamflow.web.domain.entity.project" ).
               withEntities( "ProjectEntity", "ProjectRoleEntity" ).
               end().
               renamePackage( "se.streamsource.streamflow.web.domain.task", "se.streamsource.streamflow.web.domain.entity.task" ).
               withEntities( "CaseEntity").
               end().
               renamePackage( "se.streamsource.streamflow.web.domain.tasktype", "se.streamsource.streamflow.web.domain.entity.tasktype" ).
               withEntities( "CaseTypeEntity").
               end().
               renamePackage( "se.streamsource.streamflow.web.domain.user", "se.streamsource.streamflow.web.domain.entity.user" ).
               withEntities( "UserEntity").
               end().

               toVersion( "0.5.23.1349" ).forEntities( "se.streamsource.streamflow.web.domain.entity.form.FieldEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     JSONObject fieldValue = state.getJSONObject( "properties" ).getJSONObject( "fieldValue" );
                     if (fieldValue.get( "_type" ).equals("se.streamsource.streamflow.domain.form.PageBreakFieldValue"))
                     {
                        fieldValue.put( "_type", "se.streamsource.streamflow.domain.form.CommentFieldValue" );
                        return true;
                     }

                     return false;
                  }

                  public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     return false;
                  }
               })
               .end().

               toVersion( "0.6.24.1488" ).forEntities( "se.streamsource.streamflow.web.domain.entity.task.CaseEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     JSONArray contacts = state.getJSONObject( "properties" ).getJSONArray( "contacts" );

                     boolean changed = false;
                     for ( int i=0; i<contacts.length(); i++ )
                     {
                        JSONObject contact = contacts.getJSONObject( i );
                        JSONArray emails = contact.getJSONArray( "emailAddresses" );

                        for ( int j=0; j<emails.length(); j++ )
                        {
                           JSONObject email = emails.getJSONObject( j );
                           String emailString = (String) email.get( "emailAddress" );

                           if ( !emailString.matches( "(.*@.*)?" ) )
                           {
                              email.put( "emailAddress", "" );
                              changed = true;
                           }
                        }
                     }
                     return changed;
                  }

                  public boolean downgrade( JSONObject jsonObject, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     return false;
                  }
               }).end()
               .forEntities( "se.streamsource.streamflow.web.domain.entity.user.UserEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     JSONObject contact = state.getJSONObject( "properties" ).getJSONObject( "contact" );
                     JSONArray emails = contact.getJSONArray( "emailAddresses" );

                     boolean changed = false;
                     for ( int j=0; j<emails.length(); j++ )
                     {
                        JSONObject email = emails.getJSONObject( j );
                        String emailString = (String) email.get( "emailAddress" );

                        if ( !emailString.matches( "(.*@.*)?" ) )
                        {
                           email.put( "emailAddress", "" );
                           changed = true;
                        }
                     }

                     return changed;
                  }

                  public boolean downgrade( JSONObject jsonObject, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     return false;
                  }
               }).end().

               toVersion( "0.7.25.1665" ).
               renameEntity( "se.streamsource.streamflow.web.domain.entity.task.TaskEntity","se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" ).
               renameEntity( "se.streamsource.streamflow.web.domain.entity.tasktype.TaskTypeEntity", "se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity" ).
               forEntities( "se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity" ).
                  renameManyAssociation( "taskTypes", "caseTypes" ).
               end().
               forEntities( "se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" ).
                  renameAssociation( "taskType", "caseType" ).
                  renameProperty( "taskId", "caseId" ).
                  custom( new EntityMigrationOperation()
                  {
                     public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                     {
                        String status = state.getJSONObject( "properties" ).getString( "status" );

                        if (status.equals("ACTIVE"))
                           status = "OPEN";
                        else if (status.equals( "COMPLETED" ))
                           status = "CLOSED";
                        else if (status.equals("DROPPED"))
                           status = "CLOSED";
                        else if (status.equals("DONE"))
                           status = "OPEN";
                        else if (status.equals("DELEGATED"))
                           status = "OPEN";

                        state.getJSONObject( "properties" ).put( "status", status );

                        return true;
                     }

                     public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                     {
                        return false;
                     }
                  }).
               end().
               forEntities( "se.streamsource.streamflow.web.domain.entity.project.ProjectEntity" ).
                  renameManyAssociation( "selectedTaskTypes", "selectedCaseTypes" ).
               end().
               forEntities( "se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity" ).
                  renameAssociation( "taskType", "caseType" );

         module.addServices( MigrationService.class ).setMetaInfo( migrationBuilder );
      }
   }

   private void database( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( DataSourceService.class ).identifiedBy( "datasource" ).visibleIn( Visibility.application );
      module.importServices( DataSource.class ).
            importedBy( ServiceInstanceImporter.class ).
            setMetaInfo( "datasource" ).
            identifiedBy( "streamflowds" ).visibleIn( Visibility.application );

      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (mode.equals( Application.Mode.production ))
      {
         // Liquibase migration
    	 module.addServices( LiquibaseService.class ).instantiateOnStartup();
      }
   }
}
