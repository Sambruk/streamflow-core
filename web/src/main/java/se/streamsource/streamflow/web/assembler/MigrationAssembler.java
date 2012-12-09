/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.map.StateStore;
import org.qi4j.migration.MigrationService;
import org.qi4j.migration.Migrator;
import org.qi4j.migration.assembly.EntityMigrationOperation;
import org.qi4j.migration.assembly.MigrationBuilder;
import org.qi4j.migration.assembly.MigrationOperation;

import java.io.IOException;

/**
 * This assembler contains all the migration rules for Streamflow
 */
public class
      MigrationAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {

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
            custom( new RenameValue( "fieldValue", "se.streamsource.streamflow.domain.form.PageBreakFieldValue", "se.streamsource.streamflow.domain.form.CommentFieldValue" ) )
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
            } ).end().
            toVersion( "1.2.9.2945" ).
            forEntities( "se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity" ).
            renameAssociation( "selectedTemplate", "formPdfTemplate" ).
            end().
            forEntities( "se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity" ).
            renameAssociation( "selectedTemplate", "formPdfTemplate" ).
            renameAssociation( "caseTemplate", "casePdfTemplate" ).
            end().
            toVersion( "1.3.0.0" ).
            renameEntity( "se.streamsource.streamflow.web.domain.entity.user.profile.SavedSearchEntity", "se.streamsource.streamflow.web.domain.entity.user.profile.PerspectiveEntity" ).
            forEntities( "se.streamsource.streamflow.web.domain.entity.user.UserEntity" ).
            renameAssociation( "searches", "perspectives" ).
            end().
            toVersion( "1.3.0.1" ).
            forEntities( "se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" ).
            removeProperty( "effectiveFieldValues", null ).
            custom( new EntityMigrationOperation()
            {
               public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
               {
                  try
                  {
                     JSONArray submittedForms = state.getJSONObject( "properties" ).getJSONArray( "submittedForms" );

                     for (int i = 0; i < submittedForms.length(); i++)
                     {
                        JSONObject submittedForm = submittedForms.getJSONObject( i );
                        JSONArray fields = submittedForm.getJSONArray( "values" );

                        JSONObject formState = stateStore.getState( submittedForm.getString( "form" ) );

                        JSONArray formPages = formState.getJSONObject( "manyassociations" ).getJSONArray( "pages" );

                        JSONArray submittedPages = new JSONArray();

                        for (int k = 0; k < formPages.length(); k++)
                        {
                           JSONObject submittedPage = new JSONObject();

                           submittedPage.put( "page", formPages.getString( k ) );

                           JSONObject pageState = stateStore.getState( formPages.getString( k ) );

                           JSONArray fieldsState = pageState.getJSONObject( "manyassociations" ).getJSONArray( "fields" );

                           JSONArray submittedFields = new JSONArray();
                           for (int j = 0; j < fieldsState.length(); j++)
                           {
                              for (int m = 0; m < fields.length(); m++)
                              {
                                 JSONObject field = fields.getJSONObject( m );

                                 if (field.getString( "field" ).equals( fieldsState.getString( j ) ))
                                 {
                                    submittedFields.put( field );
                                    break;
                                 }
                              }
                           }
                           submittedPage.put( "fields", submittedFields );

                           submittedPages.put( submittedPage );
                        }

                        submittedForm.put( "pages", submittedPages );
                        submittedForm.remove( "values" );
                     }

                     return true;
                  } catch (IOException e)
                  {
                     throw new JSONException( e );
                  }
               }

               public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
               {
                  return false;
               }
            } ).
            end().
            toVersion( "1.4.0.0" ).atStartup( new MigrationOperation()
      {
         public void upgrade( StateStore stateStore, Migrator migrator ) throws IOException
         {


         }

         public void downgrade( StateStore stateStore, Migrator migrator ) throws IOException
         {
            //To change body of implemented methods use File | Settings | File Templates.
         }
      } ).
            forEntities( "se.streamsource.streamflow.web.domain.entity.form.FieldEntity" ).
            custom( new EntityMigrationOperation()
            {
               public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
               {
                  JSONObject fieldValue = state.getJSONObject( "properties" ).getJSONObject( "fieldValue" );

                  String type = fieldValue.get( "_type" ).toString();

                  if (type.startsWith( "se.streamsource.streamflow.domain.form." ))
                  {
                     try
                     {
                        type = type.replace( "se.streamsource.streamflow.domain.form.", "se.streamsource.streamflow.api.administration.form." );
                        fieldValue.put( "_type", type );
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
            } ).end().
            forEntities( "se.streamsource.streamflow.web.domain.entity.form.FormDraftEntity" ).
            custom( new EntityMigrationOperation()
            {
               public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
               {
                  JSONObject properties = state.getJSONObject( "properties" );
                  JSONObject formDraftValue = properties.getJSONObject( "formDraftValue" );

                  String formString = formDraftValue.toString();

                  String newFormString = formString.replace( "se.streamsource.streamflow.domain.form.", "se.streamsource.streamflow.api.administration.form." );

                  if (formString.equals( newFormString ))
                     return false;

                  formDraftValue = new JSONObject( newFormString );

                  properties.put( "formDraftValue", formDraftValue );

                  return true;
               }

               public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
               {
                  return false;
               }
            } ).end().
            renameEntity( "se.streamsource.streamflow.web.domain.entity.user.profile.PerspectiveEntity", "se.streamsource.streamflow.web.domain.entity.user.PerspectiveEntity" ).
            renameEntity( "se.streamsource.streamflow.web.domain.entity.user.AnonymousEndUserEntity", "se.streamsource.streamflow.web.domain.entity.user.EndUserEntity" )
            .toVersion( "1.5.0.2" ).atStartup( new MigrationOperation()
            {
               public void upgrade( StateStore stateStore, Migrator migrator ) throws IOException
               {

               }

               public void downgrade( StateStore stateStore, Migrator migrator ) throws IOException
               {

               }
            } )
            .toVersion( "1.6.0.0" ).atStartup( new MigrationOperation()
            {
               public void upgrade( StateStore stateStore, Migrator migrator ) throws IOException
               {

               }

               public void downgrade( StateStore stateStore, Migrator migrator ) throws IOException
               {

               }
            } )
            .toVersion( "1.8.0.0" )
            .forEntities( "se.streamsource.streamflow.web.domain.entity.form.FormDraftEntity",
                  "se.streamsource.streamflow.web.domain.entity.caze.CaseEntity")
            .custom( new EntityMigrationOperation()
            {
               public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
               {
                  String type = (String)state.get( "type" );
                  if( "se.streamsource.streamflow.web.domain.entity.form.FormDraftEntity".equals( type ) )
                  {
                     JSONObject formDraftValue = state.getJSONObject( "properties" ).getJSONObject( "formDraftValue" );
                     if (!formDraftValue.has( "secondsignee" ))
                     {
                        formDraftValue.put( "secondsignee", JSONObject.NULL );
                     }
                  } else if( "se.streamsource.streamflow.web.domain.entity.caze.CaseEntity".equals( type ) )
                  {
                     JSONObject properties = state.getJSONObject( "properties" );
                     JSONArray submittedForms = properties.getJSONArray( "submittedForms" );

                     for (int i = 0; i < submittedForms.length(); i++)
                     {
                        JSONObject submittedForm = submittedForms.getJSONObject( i );
                        if (!submittedForm.has( "secondsignee" ))
                           submittedForm.put( "secondsignee", JSONObject.NULL );
                     }
                  }

                  return true;
               }

               public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator ) throws JSONException
               {
                  return false;
               }
            } ).end();

      module.services( MigrationService.class ).setMetaInfo( migrationBuilder );
   }
}
