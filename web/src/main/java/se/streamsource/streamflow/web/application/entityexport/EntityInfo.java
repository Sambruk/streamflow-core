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
package se.streamsource.streamflow.web.application.entityexport;

import org.qi4j.api.entity.Identity;
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
import se.streamsource.streamflow.web.domain.entity.organization.GlobalCaseIdStateEntity;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.IntegrationPointEntity;
import se.streamsource.streamflow.web.domain.entity.organization.MailRestrictionEntity;
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

/**
 * Contains all entities, used by streamflow-web. Wrap every entity with enum same name.
 * <p/>
 * Provides methods to access {@link Class} or name information of the certain entity.
 */
public enum EntityInfo
{
   AttachmentEntity( AttachmentEntity.class ),
   CaseLogEntity( CaseLogEntity.class ),
   CaseTypeEntity( CaseTypeEntity.class ),
   ResolutionEntity( ResolutionEntity.class ),
   CaseEntity( CaseEntity.class ),
   ConversationEntity( ConversationEntity.class ),
   MessageEntity( MessageEntity.class ),
   CustomerEntity( CustomerEntity.class ),
   CustomersEntity( CustomersEntity.class ),
   ShadowCaseEntity( ShadowCaseEntity.class ),
   DatatypeDefinitionEntity( DatatypeDefinitionEntity.class ),
   FieldEntity( FieldEntity.class ),
   FieldGroupEntity( FieldGroupEntity.class ),
   FieldGroupFieldInstanceEntity( FieldGroupFieldInstanceEntity.class ),
   FormDraftEntity( FormDraftEntity.class ),
   FormEntity( FormEntity.class ),
   PageEntity( PageEntity.class ),
   LabelEntity( LabelEntity.class ),
   NotesTimeLineEntity( NotesTimeLineEntity.class ),
   AccessPointEntity( AccessPointEntity.class ),
   EmailAccessPointEntity( EmailAccessPointEntity.class ),
   GlobalCaseIdStateEntity( GlobalCaseIdStateEntity.class ),
   GroupEntity( GroupEntity.class ),
   IntegrationPointEntity( IntegrationPointEntity.class ),
   MailRestrictionEntity( MailRestrictionEntity.class ),
   OrganizationEntity( OrganizationEntity.class ),
   OrganizationalUnitEntity( OrganizationalUnitEntity.class ),
   OrganizationsEntity( OrganizationsEntity.class ),
   PriorityEntity( PriorityEntity.class ),
   RoleEntity( RoleEntity.class ),
   ProjectEntity( ProjectEntity.class ),
   ProjectRoleEntity( ProjectRoleEntity.class ),
   DoubleSignatureTaskEntity( DoubleSignatureTaskEntity.class ),
   EmailUserEntity( EmailUserEntity.class ),
   EndUserEntity( EndUserEntity.class ),
   PerspectiveEntity( PerspectiveEntity.class ),
   ProxyUserEntity( ProxyUserEntity.class ),
   UserEntity( UserEntity.class ),
   UsersEntity( UsersEntity.class ),
   UNKNOWN( Identity.class );

   private Class<? extends Identity> entityClass;

   EntityInfo( Class<? extends Identity> entityClass )
   {
      this.entityClass = entityClass;
   }

   public Class<? extends Identity> getEntityClass()
   {
      return entityClass;
   }

   public static EntityInfo from( Class<?> clazz )
   {
      for ( EntityInfo entityInfo : values() )
      {
         if ( clazz.equals( entityInfo.entityClass ) )
         {
            return entityInfo;
         }
      }

      return UNKNOWN;
   }

   public String getClassName()
   {
      return entityClass.getName();
   }

   public String getClassSimpleName()
   {
      return entityClass.getSimpleName();
   }
}

