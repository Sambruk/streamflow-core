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
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;
import se.streamsource.streamflow.domain.CommonDomainAssembler;
import se.streamsource.streamflow.resource.CommonResourceAssembler;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.ResolutionEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.MessageEntity;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormDraftEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.entity.form.PageEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.organization.RoleEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectRoleEntity;
import se.streamsource.streamflow.web.domain.entity.user.AnonymousEndUserEntity;
import se.streamsource.streamflow.web.domain.entity.user.ProxyUserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.entity.user.profile.SavedSearchEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.organization.ParticipantRolesValue;
import se.streamsource.streamflow.web.domain.structure.project.PermissionValue;
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;

import static org.qi4j.api.common.Visibility.*;

/**
 * JAVADOC
 */
public class DomainAssembler
{
   public void assemble( LayerAssembly layer )
         throws AssemblyException
   {
      new CommonDomainAssembler().assemble( layer );
      new CommonResourceAssembler().assemble( layer.module("Common") );

      conversations( layer.module("Conversations") );
      forms( layer.module("Forms") );
      groups( layer.module("Groups") );
      labels( layer.module("Labels") );
      organizations( layer.module("Organizations") );
      projects( layer.module("Projects") );
      roles( layer.module("Roles") );
      cases( layer.module("Cases") );
      caseTypes( layer.module("Casetypes") );
      users( layer.module("Users") );
      attachments( layer.module("Attachments") );
   }

   private void attachments( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(AttachmentEntity.class).visibleIn( application );
      module.values(AttachedFileValue.class).visibleIn( application );
   }

   private void users( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(UsersEntity.class, UserEntity.class, ProxyUserEntity.class, AnonymousEndUserEntity.class,
              SavedSearchEntity.class).visibleIn( application );

      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor( "solrquery", "" );
      namedQueries.addQuery( queryDescriptor );

      module.importedServices(NamedEntityFinder.class).
            importedBy( ServiceSelectorImporter.class ).
            setMetaInfo( ServiceQualifier.withId( "solr" ) ).
            setMetaInfo( namedQueries );
   }

   private void caseTypes( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(CaseTypeEntity.class, ResolutionEntity.class).visibleIn( Visibility.application );
   }

   private void cases( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(CaseEntity.class).visibleIn( Visibility.application );
   }

   private void roles( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(RoleEntity.class).visibleIn( Visibility.application );
   }

   private void projects( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(
              ProjectRoleEntity.class,
              ProjectEntity.class).visibleIn( application );

      module.values(PermissionValue.class).visibleIn( application );
   }

   private void organizations( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(OrganizationsEntity.class, OrganizationEntity.class,
              OrganizationalUnitEntity.class, AccessPointEntity.class).visibleIn( application );
      module.values(ParticipantRolesValue.class).visibleIn( Visibility.application );
   }

   private void labels( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(LabelEntity.class).visibleIn( application );
   }

   private void groups( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(GroupEntity.class).visibleIn( application );
   }

   private void forms( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(
              FormEntity.class,
              FormDraftEntity.class,
              FieldEntity.class,
              PageEntity.class
      ).visibleIn(Visibility.application);
   }

   private void conversations( ModuleAssembly module ) throws AssemblyException
   {
      module.entities(
              ConversationEntity.class,
              MessageEntity.class).visibleIn( Visibility.application );
   }
}
