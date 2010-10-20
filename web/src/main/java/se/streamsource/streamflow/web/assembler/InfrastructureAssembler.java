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
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.GenericConcern;
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
import org.qi4j.migration.MigrationEventLogger;
import org.qi4j.migration.MigrationService;
import org.qi4j.migration.Migrator;
import org.qi4j.migration.assembly.EntityMigrationOperation;
import org.qi4j.migration.assembly.MigrationBuilder;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.factory.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.time.TimeService;
import se.streamsource.streamflow.server.plugin.contact.ContactAddressValue;
import se.streamsource.streamflow.server.plugin.contact.ContactEmailValue;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactPhoneValue;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStoreService;
import se.streamsource.streamflow.web.infrastructure.caching.CachingServiceComposite;
import se.streamsource.streamflow.web.infrastructure.database.DataSourceService;
import se.streamsource.streamflow.web.infrastructure.database.LiquibaseService;
import se.streamsource.streamflow.web.infrastructure.database.ServiceInstanceImporter;
import se.streamsource.streamflow.web.infrastructure.event.JdbmEventStoreService;
import se.streamsource.streamflow.web.infrastructure.event.MemoryEventStoreService;
import se.streamsource.streamflow.web.infrastructure.index.EmbeddedSolrService;
import se.streamsource.streamflow.web.infrastructure.index.SolrQueryService;
import se.streamsource.streamflow.web.infrastructure.plugin.contact.ContactLookupService;
import se.streamsource.streamflow.web.resource.EventsCommandResult;

import javax.sql.DataSource;
import java.lang.reflect.Method;

import static org.qi4j.api.service.qualifier.ServiceTags.*;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * JAVADOC
 */
public class InfrastructureAssembler
{
   public void assemble( LayerAssembly layer )
         throws AssemblyException
   {
      caching( layer.moduleAssembly( "Caching" ) );
      database( layer.moduleAssembly( "Database" ) );
      entityStore( layer.moduleAssembly( "Entity store" ) );
      entityFinder( layer.moduleAssembly( "Entity finder" ) );
      events( layer.moduleAssembly( "Events" ) );
      searchEngine( layer.moduleAssembly( "Search engine" ) );
      attachments( layer.moduleAssembly( "Attachments store" ) );
      plugins( layer.moduleAssembly( "Plugins" ) );
   }

   private void plugins( ModuleAssembly moduleAssembly ) throws AssemblyException
   {

      moduleAssembly.addObjects( CommandQueryClient.class
      ).visibleIn( Visibility.module );

      moduleAssembly.addServices( ContactLookupService.class ).
            identifiedBy( "contactlookup" ).
            visibleIn( Visibility.application ).
            instantiateOnStartup();

      moduleAssembly.addValues( ContactList.class,
            ContactValue.class,
            ContactAddressValue.class,
            ContactEmailValue.class,
            ContactPhoneValue.class ).visibleIn( Visibility.application );


   }

   private void caching( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.addServices( CachingServiceComposite.class ).visibleIn( Visibility.application );
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
      module.importServices( EventsCommandResult.class ).importedBy( NEW_OBJECT ).visibleIn( Visibility.application );
      module.addObjects( EventsCommandResult.class );
      module.addValues( TransactionEvents.class, DomainEvent.class ).visibleIn( Visibility.application );
      module.addServices( DomainEventFactoryService.class ).visibleIn( Visibility.application );
      module.addObjects( TimeService.class );
      module.importServices( TimeService.class ).importedBy( NewObjectImporter.class );

      if (module.layerAssembly().applicationAssembly().mode() == Application.Mode.production)
      {
         module.addServices( JdbmEventStoreService.class ).identifiedBy( "eventstore" ).setMetaInfo( tags( "domain" ) ).visibleIn( Visibility.application );
      } else
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
      module.addServices( RdfIndexingEngineService.class ).instantiateOnStartup().visibleIn( Visibility.application ).
            withConcerns( PerformanceLogConcern.class );
      module.addServices( RdfQueryParserFactory.class );
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
         module.addServices( JdbmEntityStoreService.class ).identifiedBy( "data" ).visibleIn( Visibility.application ).
               withConcerns( EntityStorePerformanceCheck.class );
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

               toVersion( "0.2.18.0" ).
               toVersion( "0.3.20.962" ).
               renamePackage( "se.streamsource.streamflow.web.domain.form", "se.streamsource.streamflow.web.domain.entity.form" ).
               withEntities( "FieldEntity",
                     "FieldTemplateEntity",
                     "FormEntity",
                     "FormTemplateEntity" ).
               end().
               renameEntity( "se.streamsource.streamflow.web.domain.label.LabelEntity", "se.streamsource.streamflow.web.domain.entity.label.LabelEntity" ).
               renamePackage( "se.streamsource.streamflow.web.domain.organization", "se.streamsource.streamflow.web.domain.entity.organization" ).
               withEntities( "OrganizationalUnitEntity",
                     "OrganizationEntity",
                     "OrganizationsEntity" ).
               end().
               renameEntity( "se.streamsource.streamflow.web.domain.group.GroupEntity", "se.streamsource.streamflow.web.domain.entity.organization.GroupEntity" ).
               renameEntity( "se.streamsource.streamflow.web.domain.role.RoleEntity", "se.streamsource.streamflow.web.domain.entity.organization.RoleEntity" ).
               renamePackage( "se.streamsource.streamflow.web.domain.project", "se.streamsource.streamflow.web.domain.entity.project" ).
               withEntities( "ProjectEntity", "ProjectRoleEntity" ).
               end().
               renamePackage( "se.streamsource.streamflow.web.domain.task", "se.streamsource.streamflow.web.domain.entity.task" ).
               withEntities( "TaskEntity" ).
               end().
               renamePackage( "se.streamsource.streamflow.web.domain.tasktype", "se.streamsource.streamflow.web.domain.entity.tasktype" ).
               withEntities( "TaskTypeEntity" ).
               end().
               renamePackage( "se.streamsource.streamflow.web.domain.user", "se.streamsource.streamflow.web.domain.entity.user" ).
               withEntities( "UserEntity" ).
               end().

               toVersion( "0.5.23.1349" ).forEntities( "se.streamsource.streamflow.web.domain.entity.form.FieldEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     JSONObject fieldValue = state.getJSONObject( "properties" ).getJSONObject( "fieldValue" );
                     if (fieldValue.get( "_type" ).equals( "se.streamsource.streamflow.domain.form.PageBreakFieldValue" ))
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
               } )
               .end().

               toVersion( "0.6.24.1488" ).forEntities( "se.streamsource.streamflow.web.domain.entity.task.TaskEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     JSONArray contacts = state.getJSONObject( "properties" ).getJSONArray( "contacts" );

                     boolean changed = false;
                     for (int i = 0; i < contacts.length(); i++)
                     {
                        JSONObject contact = contacts.getJSONObject( i );
                        JSONArray emails = contact.getJSONArray( "emailAddresses" );

                        for (int j = 0; j < emails.length(); j++)
                        {
                           JSONObject email = emails.getJSONObject( j );
                           String emailString = (String) email.get( "emailAddress" );

                           if (!emailString.matches( "(.*@.*)?" ))
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
               } ).end()
               .forEntities( "se.streamsource.streamflow.web.domain.entity.user.UserEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     JSONObject contact = state.getJSONObject( "properties" ).getJSONObject( "contact" );
                     JSONArray emails = contact.getJSONArray( "emailAddresses" );

                     boolean changed = false;
                     for (int j = 0; j < emails.length(); j++)
                     {
                        JSONObject email = emails.getJSONObject( j );
                        String emailString = (String) email.get( "emailAddress" );

                        if (!emailString.matches( "(.*@.*)?" ))
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
               } ).end().

               toVersion( "0.7.25.1665" ).
               renameEntity( "se.streamsource.streamflow.web.domain.entity.task.TaskEntity", "se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" ).
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

                     if (status.equals( "ACTIVE" ))
                        status = "OPEN";
                     else if (status.equals( "COMPLETED" ))
                        status = "CLOSED";
                     else if (status.equals( "DROPPED" ))
                        status = "CLOSED";
                     else if (status.equals( "DONE" ))
                        status = "OPEN";
                     else if (status.equals( "DELEGATED" ))
                        status = "OPEN";

                     state.getJSONObject( "properties" ).put( "status", status );

                     return true;
                  }

                  public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     return false;
                  }
               } ).
               end().
               forEntities( "se.streamsource.streamflow.web.domain.entity.project.ProjectEntity" ).
               renameManyAssociation( "selectedTaskTypes", "selectedCaseTypes" ).
               end().
               forEntities( "se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity" ).
               renameAssociation( "taskType", "caseType" ).
               end().
               toVersion( "1.1.5.2083" ).
               forEntities( "se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity" ).
               removeManyAssociation( "fieldDefinitions" ).
               end().
               toVersion( "1.1.6.2236" ).
               forEntities( "se.streamsource.streamflow.web.domain.entity.form.FieldEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     JSONObject fieldValue = state.getJSONObject( "properties" ).getJSONObject( "fieldValue" );

                     if (fieldValue.get( "_type" ).equals( "se.streamsource.streamflow.domain.form.SelectionFieldValue" ))
                     {
                        try
                        {
                           if (fieldValue.getBoolean( "multiple" ))
                           {
                              fieldValue.put( "_type", "se.streamsource.streamflow.domain.form.CheckboxesFieldValue" );
                           } else
                           {
                              fieldValue.put( "_type", "se.streamsource.streamflow.domain.form.OptionButtonsFieldValue" );
                           }
                           fieldValue.remove( "multiple" );
                        } catch (JSONException e)
                        {
                           fieldValue.put( "_type", "se.streamsource.streamflow.domain.form.CheckboxesFieldValue" );
                        }
                        return true;
                     }
                     return false;
                  }

                  public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     return false;
                  }
               } )
               .end().
               toVersion( "1.1.7.2311" ).
               forEntities( "se.streamsource.streamflow.web.domain.entity.form.FieldEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     JSONObject fieldValue = state.getJSONObject( "properties" ).getJSONObject( "fieldValue" );

                     if (fieldValue.get( "_type" ).equals( "se.streamsource.streamflow.domain.form.ListBoxFieldValue" ))
                     {
                        try
                        {
                           if (!fieldValue.getBoolean( "multiple" ))
                           {
                              fieldValue.put( "_type", "se.streamsource.streamflow.domain.form.ComboBoxFieldValue" );
                           }
                           fieldValue.remove( "multiple" );
                        } catch (JSONException e)
                        {
                           return false;
                        }
                        return true;
                     }
                     return false;
                  }

                  public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     return false;
                  }
               } )
               .end().
               toVersion( "1.1.7.2360" ).
               forEntities( "se.streamsource.streamflow.web.domain.entity.form.FieldEntity" ).
               addProperty( "fieldId", "field" ).
               end().
               forEntities( "se.streamsource.streamflow.web.domain.entity.form.FormEntity" ).
               addProperty( "formId", "form" ).
               end().
               toVersion( "1.1.7.2429" ).
               forEntities( "se.streamsource.streamflow.web.domain.entity.form.FormSubmissionEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     boolean changed = false;
                     JSONObject formSubmissionValue = state.getJSONObject( "properties" ).getJSONObject( "formSubmissionValue" );

                     JSONArray pages = formSubmissionValue.getJSONArray( "pages" );

                     for (int i = 0; i < pages.length(); i++)
                     {
                        JSONArray fields = pages.getJSONObject( i ).getJSONArray( "fields" );
                        for (int j = 0; j < fields.length(); j++)
                        {
                           JSONObject fieldDefinitionValue = fields.getJSONObject( j ).getJSONObject( "field" );
                           JSONObject fieldValue = fieldDefinitionValue.getJSONObject( "fieldValue" );

                           if (fieldValue.get( "_type" ).equals( "se.streamsource.streamflow.domain.form.SelectionFieldValue" ))
                           {
                              try
                              {
                                 if (fieldValue.getBoolean( "multiple" ))
                                 {
                                    fieldValue.put( "_type", "se.streamsource.streamflow.domain.form.CheckboxesFieldValue" );
                                 } else
                                 {
                                    fieldValue.put( "_type", "se.streamsource.streamflow.domain.form.OptionButtonsFieldValue" );
                                 }
                                 fieldValue.remove( "multiple" );

                              } catch (JSONException e)
                              {
                                 fieldValue.put( "_type", "se.streamsource.streamflow.domain.form.CheckboxesFieldValue" );
                              }
                              changed = true;
                           }
                        }
                     }
                     return changed;
                  }

                  public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     return false;
                  }
               } ).end().
               toVersion( "1.2.9.2794" ).
               forEntities( "se.streamsource.streamflow.web.domain.entity.form.FormSubmissionEntity" ).
               custom( new EntityMigrationOperation()
               {
                  public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     state.put( "type", "se.streamsource.streamflow.web.domain.entity.form.FormDraftEntity" );
                     JSONObject formValue = state.getJSONObject( "properties" ).getJSONObject( "formSubmissionValue" );
                     formValue.remove( "currentPage" );

                     JSONArray pages = formValue.getJSONArray( "pages" );

                     for (int i = 0; i < pages.length(); i++)
                     {
                        JSONArray fields = pages.getJSONObject( i ).getJSONArray( "fields" );
                        for (int j = 0; j < fields.length(); j++)
                        {
                           JSONObject fieldDefinitionValue = fields.getJSONObject( j ).getJSONObject( "field" );
                           JSONObject fieldValue = fieldDefinitionValue.getJSONObject( "fieldValue" );

                           if (fieldValue.get( "_type" ).equals( "se.streamsource.streamflow.domain.form.SignatureFieldValue" ))
                           {
                              fieldValue.put( "_type", "se.streamsource.streamflow.domain.form.CommentFieldValue" );  
                           }
                        }
                     }


                     return true;
                  }

                  public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
                  {
                     state.put( "type", "se.streamsource.streamflow.web.domain.entity.form.FormSubmissionEntity" );
                     JSONObject formValue = state.getJSONObject( "properties" ).getJSONObject( "formDraftValue" );

                     formValue.put( "currentPage", 0 );

                     return true;
                  }
               }).end();

         module.addServices( MigrationService.class ).setMetaInfo( migrationBuilder );
         module.addObjects( MigrationEventLogger.class );
         module.importServices( MigrationEventLogger.class ).importedBy( NEW_OBJECT );
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
}
