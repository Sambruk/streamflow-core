package se.streamsource.streamflow.web.application.entityexport;

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
 * JAVADOC
 */
public enum EntityInfo
{
   AttachmentEntity( AttachmentEntity.class.getName() ),
   CaseLogEntity( CaseLogEntity.class.getName() ),
   CaseTypeEntity( CaseTypeEntity.class.getName() ),
   ResolutionEntity( ResolutionEntity.class.getName() ),
   CaseEntity( CaseEntity.class.getName() ),
   ConversationEntity( ConversationEntity.class.getName() ),
   MessageEntity( MessageEntity.class.getName() ),
   CustomerEntity( CustomerEntity.class.getName() ),
   CustomersEntity( CustomersEntity.class.getName() ),
   ShadowCaseEntity( ShadowCaseEntity.class.getName() ),
   DatatypeDefinitionEntity( DatatypeDefinitionEntity.class.getName() ),
   FieldEntity( FieldEntity.class.getName() ),
   FieldGroupEntity( FieldGroupEntity.class.getName() ),
   FieldGroupFieldInstanceEntity( FieldGroupFieldInstanceEntity.class.getName() ),
   FormDraftEntity( FormDraftEntity.class.getName() ),
   FormEntity( FormEntity.class.getName() ),
   PageEntity( PageEntity.class.getName() ),
   LabelEntity( LabelEntity.class.getName() ),
   NotesTimeLineEntity( NotesTimeLineEntity.class.getName() ),
   AccessPointEntity( AccessPointEntity.class.getName() ),
   EmailAccessPointEntity( EmailAccessPointEntity.class.getName() ),
   GlobalCaseIdStateEntity( GlobalCaseIdStateEntity.class.getName() ),
   GroupEntity( GroupEntity.class.getName() ),
   IntegrationPointEntity( IntegrationPointEntity.class.getName() ),
   MailRestrictionEntity( MailRestrictionEntity.class.getName() ),
   OrganizationEntity( OrganizationEntity.class.getName() ),
   OrganizationalUnitEntity( OrganizationalUnitEntity.class.getName() ),
   OrganizationsEntity( OrganizationsEntity.class.getName() ),
   PriorityEntity( PriorityEntity.class.getName() ),
   RoleEntity( RoleEntity.class.getName() ),
   ProjectEntity( ProjectEntity.class.getName() ),
   ProjectRoleEntity( ProjectRoleEntity.class.getName() ),
   DoubleSignatureTaskEntity( DoubleSignatureTaskEntity.class.getName() ),
   EmailUserEntity( EmailUserEntity.class.getName() ),
   EndUserEntity( EndUserEntity.class.getName() ),
   PerspectiveEntity( PerspectiveEntity.class.getName() ),
   ProxyUserEntity( ProxyUserEntity.class.getName() ),
   UserEntity( UserEntity.class.getName() ),
   UsersEntity( UsersEntity.class.getName() ),
   UNKNOWN( "UNKNOWN" );

   private String entityClass;

   EntityInfo( String entityClass )
   {
      this.entityClass = entityClass;
   }

   public static EntityInfo fromClass(String clazzName) {
      for ( EntityInfo entityInfo : values() )
      {
         if ( clazzName.equals( entityInfo.entityClass ) ) {
            return entityInfo;
         }
      }

      return UNKNOWN;
   }
}

