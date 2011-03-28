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
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.entitystore.jdbm.JdbmEntityStoreService;
import org.qi4j.entitystore.map.StateStore;
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
import org.qi4j.migration.MigrationService;
import org.qi4j.migration.Migrator;
import org.qi4j.migration.assembly.EntityMigrationOperation;
import org.qi4j.migration.assembly.MigrationBuilder;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import se.streamsource.dci.restlet.client.ClientAssembler;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.factory.ApplicationEventFactoryService;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.factory.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.time.TimeService;
import se.streamsource.streamflow.server.plugin.contact.ContactAddressValue;
import se.streamsource.streamflow.server.plugin.contact.ContactEmailValue;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactPhoneValue;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStoreService;
import se.streamsource.streamflow.web.infrastructure.caching.CachingServiceComposite;
import se.streamsource.streamflow.web.infrastructure.database.DataSourceService;
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
import se.streamsource.streamflow.web.infrastructure.plugin.contact.ContactLookupService;
import se.streamsource.streamflow.web.resource.EventsCommandResult;

import javax.sql.DataSource;

import java.io.IOException;

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

      moduleAssembly.services( ContactLookupService.class ).
            identifiedBy("contactlookup").
            visibleIn(Visibility.application).
            instantiateOnStartup();

      moduleAssembly.values( ContactList.class,
            ContactValue.class,
            ContactAddressValue.class,
            ContactEmailValue.class,
            ContactPhoneValue.class ).visibleIn( Visibility.application );


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
         module.services( JdbmEventStoreService.class ).identifiedBy( "eventstore" ).taggedWith("domain").visibleIn( Visibility.application );
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
                     state.getJSONObject( "properties" ).remove( "formSubmissionValue" );
                     state.getJSONObject( "properties" ).put( "formDraftValue", formValue );
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
               }).end().
               toVersion( "1.2.9.2945" ).
                  forEntities( "se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity" ).
                     renameAssociation( "selectedTemplate", "formPdfTemplate" ).
                  end().
                  forEntities( "se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity" ).
                     renameAssociation( "selectedTemplate", "formPdfTemplate" ).
                     renameAssociation( "caseTemplate", "casePdfTemplate" ).
               end().
               toVersion("1.3.0.0").
                  renameEntity( "se.streamsource.streamflow.web.domain.entity.user.profile.SavedSearchEntity", "se.streamsource.streamflow.web.domain.entity.user.profile.PerspectiveEntity").
                  forEntities("se.streamsource.streamflow.web.domain.entity.user.UserEntity").
                     renameAssociation("searches", "perspectives").
                  end().
               toVersion("1.3.0.1").
                 forEntities("se.streamsource.streamflow.web.domain.entity.caze.CaseEntity").
                     removeProperty("effectiveFieldValues", null).
                     custom(new EntityMigrationOperation()
                     {
                        public boolean upgrade(JSONObject state, StateStore stateStore, Migrator migrator) throws JSONException
                        {
                           try
                           {
                              JSONArray submittedForms = state.getJSONObject( "properties" ).getJSONArray("submittedForms");

                              for (int i = 0; i < submittedForms.length(); i++)
                              {
                                 JSONObject submittedForm = submittedForms.getJSONObject(i);
                                 JSONArray fields = submittedForm.getJSONArray("values");

                                 JSONObject formState = stateStore.getState(submittedForm.getString("form"));

                                 JSONArray formPages = formState.getJSONObject("manyassociations").getJSONArray("pages");

                                 JSONArray submittedPages = new JSONArray();

                                 for (int k = 0; k < formPages.length(); k++)
                                 {
                                    JSONObject submittedPage = new JSONObject();

                                    submittedPage.put("page", formPages.getString(k));

                                    JSONObject pageState = stateStore.getState(formPages.getString(k));

                                    JSONArray fieldsState = pageState.getJSONObject("manyassociations").getJSONArray("fields");

                                    JSONArray submittedFields = new JSONArray();
                                    for (int j = 0; j < fieldsState.length(); j++)
                                    {
                                       for (int m = 0; m < fields.length(); m++)
                                       {
                                          JSONObject field = fields.getJSONObject(m);

                                          if (field.getString("field").equals(fieldsState.getString(j)))
                                          {
                                             submittedFields.put(field);
                                             break;
                                          }
                                       }
                                    }
                                    submittedPage.put("fields", submittedFields);

                                    submittedPages.put(submittedPage);
                                 }

                                 submittedForm.put("pages", submittedPages);
                                 submittedForm.remove("values");
                              }

                              return true;
                           } catch (IOException e)
                           {
                              throw new JSONException(e);
                           }
                        }

                        public boolean downgrade(JSONObject state, StateStore stateStore, Migrator migrator) throws JSONException
                        {
                           return false;
                        }
                     }).
                 end();


                  

         module.services( MigrationService.class ).setMetaInfo( migrationBuilder );
         configuration().entities( MigrationConfiguration.class ).visibleIn( Visibility.application );

         module.objects( MigrationEventLogger.class );
         module.importedServices( MigrationEventLogger.class ).importedBy( NEW_OBJECT );
      }
   }

   private void database( ModuleAssembly module ) throws AssemblyException
   {
      module.services( DataSourceService.class ).identifiedBy( "datasource" ).visibleIn( Visibility.application );
      module.importedServices( DataSource.class ).
            importedBy(ServiceInstanceImporter.class).
            setMetaInfo("datasource").
            identifiedBy("streamflowds").visibleIn( Visibility.application );

      Application.Mode mode = module.layer().application().mode();
      if (mode.equals( Application.Mode.production ))
      {
         // Liquibase migration
         module.services( LiquibaseService.class ).instantiateOnStartup();
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
