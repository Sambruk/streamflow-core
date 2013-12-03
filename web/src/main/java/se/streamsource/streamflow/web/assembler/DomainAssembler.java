/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
import org.qi4j.api.specification.Specifications;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;
import se.streamsource.streamflow.api.assembler.ClientAPIAssembler;
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
import se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity;
import se.streamsource.streamflow.web.domain.entity.organization.EmailAccessPointEntity;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.IntegrationPointEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.organization.PriorityEntity;
import se.streamsource.streamflow.web.domain.entity.organization.RoleEntity;
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
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;

import static org.qi4j.api.common.Visibility.*;

/**
 * JAVADOC
 */
public class DomainAssembler
{
   public void assemble(LayerAssembly layer)
           throws AssemblyException
   {
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
            PriorityEntity.class, IntegrationPointEntity.class).visibleIn( application );
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
