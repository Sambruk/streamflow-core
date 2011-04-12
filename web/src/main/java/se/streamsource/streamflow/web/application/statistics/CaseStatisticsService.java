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

package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.configuration.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.query.*;
import org.qi4j.api.service.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.usecase.*;
import org.qi4j.api.value.*;
import org.qi4j.spi.structure.*;
import org.slf4j.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.domain.interaction.gtd.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;
import se.streamsource.streamflow.web.domain.entity.*;
import se.streamsource.streamflow.web.domain.entity.casetype.*;
import se.streamsource.streamflow.web.domain.entity.caze.*;
import se.streamsource.streamflow.web.domain.entity.form.*;
import se.streamsource.streamflow.web.domain.entity.label.*;
import se.streamsource.streamflow.web.domain.entity.organization.*;
import se.streamsource.streamflow.web.domain.entity.project.*;
import se.streamsource.streamflow.web.domain.entity.user.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.form.*;
import se.streamsource.streamflow.web.domain.structure.group.*;
import se.streamsource.streamflow.web.domain.structure.label.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

import java.util.*;

import static org.qi4j.api.specification.Specifications.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * Consumes domain events and creates application events for statistics.
 */
@Mixins(CaseStatisticsService.Mixin.class)
public interface CaseStatisticsService
      extends ServiceComposite, Activatable, CaseStatistics, Configuration
{
   class Mixin
         implements TransactionVisitor, Activatable, CaseStatistics
   {
      @Service
      EventSource eventSource;

      @Service
      EventStream stream;

      @Service
      Iterable<StatisticsStore> statisticsStores;

      @Structure
      ModuleSPI module;

      @This
      Configuration<StatisticsConfiguration> config;

      TransactionTracker tracker;
      EventRouter router;

      Logger log;
      public TransactionVisitor transactionAdapter;

      public void activate() throws Exception
      {
         log = LoggerFactory.getLogger( CaseStatisticsService.class );

         router = new EventRouter().route( and( withNames( "changedStatus" ), paramIs( "param1", CaseStates.CLOSED.name() ) ),
               new EventVisitor()
               {
                  public boolean visit( DomainEvent event )
                  {
                     // Case was closed
                     UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Create statistics" ) );
                     try
                     {
                        CaseEntity entity = null;
                        try
                        {
                           entity = uow.get( CaseEntity.class, event.entity().get() );
                        } catch (NoSuchEntityException e)
                        {
                           // Entity has been deleted. Ignore it
                           return true;
                        }

                        // case has been reopend and is still not closed again
                        // do nothing
                        if (!entity.isStatus( CaseStates.CLOSED ))
                           return true;

                        CaseStatisticsValue stats = createStatistics( entity );
                        try
                        {
                           notifyStores( stats );
                           return true;
                        } catch (StatisticsStoreException e)
                        {
                           log.warn( e.getMessage(), e.getCause() );
                           return false;
                        }
                     } finally
                     {
                        uow.discard();
                     }
                  }
               } ).route( and( withNames( "changedDescription", "changedFieldId", "changedFormId" ), onEntityTypes(
               LabelEntity.class.getName(),
               UserEntity.class.getName(),
               GroupEntity.class.getName(),
               ProjectEntity.class.getName(),
               OrganizationEntity.class.getName(),
               OrganizationalUnitEntity.class.getName(),
               ResolutionEntity.class.getName(),
               FormEntity.class.getName(),
               FieldEntity.class.getName(),
               CaseTypeEntity.class.getName()
         ) ),
               new EventVisitor()
               {
                  public boolean visit( DomainEvent event )
                  {
                     // Description of related entity was updated
                     UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Change related description" ) );
                     try
                     {
                        EntityComposite entity = uow.get( DomainEntity.class, event.entity().get() );
                        RelatedEnum type;
                        if (entity instanceof Label)
                           type = RelatedEnum.label;
                        else if (entity instanceof User)
                           type = RelatedEnum.user;
                        else if (entity instanceof Group)
                           type = RelatedEnum.group;
                        else if (entity instanceof Project)
                           type = RelatedEnum.project;
                        else if (entity instanceof Organization)
                           type = RelatedEnum.organization;
                        else if (entity instanceof OrganizationalUnit)
                           type = RelatedEnum.organizationalUnit;
                        else if (entity instanceof Resolution)
                           type = RelatedEnum.resolution;
                        else if (entity instanceof Form)
                           type = RelatedEnum.form;
                        else if (entity instanceof Field)
                           type = RelatedEnum.field;
                        else if (entity instanceof CaseType)
                           type = RelatedEnum.caseType;
                        else
                           return true;

                        RelatedStatisticsValue related = createRelated( entity, type );
                        try
                        {
                           notifyStores( related );
                           return true;
                        } catch (StatisticsStoreException e)
                        {
                           log.warn( e.getMessage(), e.getCause() );
                           return false;
                        }
                     } catch (NoSuchEntityException ex)
                     {
                        log.warn( "Could not update database information due to missing entity", ex );
                        return true;
                     } finally
                     {
                        uow.discard();
                     }
                  }
               } ).route( and( withNames( "deletedEntity" ), onEntityTypes( CaseEntity.class.getName() ) ),
               new EventVisitor()
               {
                  public boolean visit( DomainEvent event )
                  {
                     try
                     {
                        notifyStores( event.entity().get() );
                        return true;
                     } catch (StatisticsStoreException e)
                     {
                        log.warn( e.getMessage(), e.getCause() );
                        return false;
                     }
                  }
               } );

         transactionAdapter = Events.adapter( router );
         tracker = new TransactionTracker( stream, eventSource, config, this );
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public void refreshStatistics() throws StatisticsStoreException
      {
         log.info( "Refresh all statistics" );

         // First clear the statistics stores of all their existing data
         log.debug( "Clear all statistics stores" );
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.clearAll();
         }

         // Update all related entities
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
            try
            {
               // Labels
               {
                  log.debug( "Update all labels" );
                  Query<LabelEntity> labels = module.queryBuilderFactory().newQueryBuilder( LabelEntity.class ).newQuery( uow );
                  for (LabelEntity label : labels)
                  {
                     notifyStores( createRelated( label, RelatedEnum.label ) );
                  }
               }
               uow.discard();
               uow = module.unitOfWorkFactory().newUnitOfWork();

               // Users
               {
                  log.debug( "Update all users" );
                  Query<UserEntity> users = module.queryBuilderFactory().newQueryBuilder( UserEntity.class ).newQuery( uow );
                  for (UserEntity user : users)
                  {
                     notifyStores( createRelated( user, RelatedEnum.user ) );
                  }
               }
               uow.discard();
               uow = module.unitOfWorkFactory().newUnitOfWork();

               // Groups
               {
                  log.debug( "Update all groups" );
                  Query<GroupEntity> groups = module.queryBuilderFactory().newQueryBuilder( GroupEntity.class ).newQuery( uow );
                  for (GroupEntity group : groups)
                  {
                     notifyStores( createRelated( group, RelatedEnum.group ) );
                  }
               }
               uow.discard();
               uow = module.unitOfWorkFactory().newUnitOfWork();

               // Projects
               {
                  log.debug( "Update all projects" );
                  Query<ProjectEntity> projects = module.queryBuilderFactory().newQueryBuilder( ProjectEntity.class ).newQuery( uow );
                  for (ProjectEntity project : projects)
                  {
                     notifyStores( createRelated( project, RelatedEnum.project ) );
                  }
               }
               uow.discard();
               uow = module.unitOfWorkFactory().newUnitOfWork();

               // Organizations
               {
                  log.debug( "Update all organizations" );
                  Query<OrganizationEntity> org = module.queryBuilderFactory().newQueryBuilder( OrganizationEntity.class ).newQuery( uow );
                  for (OrganizationEntity organization : org)
                  {
                     notifyStores( createRelated( organization, RelatedEnum.organization ) );
                  }
               }

               // OU
               {
                  log.debug( "Update all OUs" );
                  Query<OrganizationalUnitEntity> ous = module.queryBuilderFactory().newQueryBuilder( OrganizationalUnitEntity.class ).newQuery( uow );
                  for (OrganizationalUnitEntity ou : ous)
                  {
                     notifyStores( createRelated( ou, RelatedEnum.organizationalUnit ) );
                  }
               }

               // Resolutions
               {
                  log.debug( "Update all resolutions" );
                  Query<ResolutionEntity> resolutions = module.queryBuilderFactory().newQueryBuilder( ResolutionEntity.class ).newQuery( uow );
                  for (ResolutionEntity resolution : resolutions)
                  {
                     notifyStores( createRelated( resolution, RelatedEnum.resolution ) );
                  }
               }

               // Forms
               {
                  log.debug( "Update all forms" );
                  Query<FormEntity> forms = module.queryBuilderFactory().newQueryBuilder( FormEntity.class ).newQuery( uow );
                  for (FormEntity form : forms)
                  {
                     notifyStores( createRelated( form, RelatedEnum.form ) );

                     for (Page page : form.pages())
                     {
                        for (Field field : ((PageEntity) page).fields())
                        {
                           notifyStores( createRelated( (EntityComposite) field, RelatedEnum.field ) );
                        }
                     }
                  }
               }

               // Casetypes
               {
                  log.debug( "Update all Casetypes" );
                  Query<CaseTypeEntity> caseTypes = module.queryBuilderFactory().newQueryBuilder( CaseTypeEntity.class ).newQuery( uow );
                  for (CaseTypeEntity caseType : caseTypes)
                  {
                     notifyStores( createRelated( caseType, RelatedEnum.caseType ) );
                  }
               }


            } finally
            {
               uow.discard();
            }
         }

         // Update all case statistics
         {
            log.debug( "Update all case statistics" );
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
            QueryBuilder<CaseEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( CaseEntity.class );
            Query<CaseEntity> cases = queryBuilder.where( QueryExpressions.eq( QueryExpressions.templateFor( Status.Data.class ).status(), CaseStates.CLOSED ) ).newQuery( uow );
            for (CaseEntity aCase : cases)
            {
               if (aCase.caseId().get() != null)
               {
                  UnitOfWork caseUoW = module.unitOfWorkFactory().newUnitOfWork();
                  try
                  {
                     notifyStores( createStatistics( caseUoW.get( aCase ) ) );
                  } finally
                  {
                     caseUoW.discard();
                  }
               }
            }
            log.debug( "Finished refreshing case statistics" );
         }

      }

      public boolean visit( TransactionDomainEvents transactionDomain )
      {
         return transactionAdapter.visit( transactionDomain );
      }

      private RelatedStatisticsValue createRelated( EntityComposite entity, RelatedEnum type )
      {
         ValueBuilder<RelatedStatisticsValue> builder = module.valueBuilderFactory().newValueBuilder( RelatedStatisticsValue.class );
         builder.prototype().identity().set( entity.identity().get() );
         if (entity instanceof Form)
         {
            builder.prototype().description().set( ((FormId.Data) entity).formId().get() );
         } else if (entity instanceof Field)
         {
            builder.prototype().description().set( ((FieldId.Data) entity).fieldId().get() );
         } else
         {
            builder.prototype().description().set( ((Describable) entity).getDescription() );
         }

         builder.prototype().relatedType().set( type );
         return builder.newInstance();
      }

      private CaseStatisticsValue createStatistics( CaseEntity aCase )
      {
         UnitOfWork uow = module.unitOfWorkFactory().getUnitOfWork( aCase );

         ValueBuilder<CaseStatisticsValue> builder = module.valueBuilderFactory().newValueBuilder( CaseStatisticsValue.class );
         CaseStatisticsValue prototype = builder.prototype();

         prototype.identity().set( aCase.identity().get() );
         prototype.description().set( aCase.getDescription() );
         prototype.note().set( aCase.note().get() );
         Assignee assignee = aCase.assignedTo().get();
         prototype.assigneeId().set( ((Identity) assignee).identity().get() );
         prototype.caseId().set( aCase.caseId().get() );
         prototype.createdOn().set( new Date( aCase.createdOn().get().getTime() ) );
         Date closeDate = aCase.closedOn().get();
         prototype.closedOn().set( new Date( closeDate.getTime() ) );
         prototype.duration().set( closeDate.getTime() - aCase.createdOn().get().getTime() );

         CaseType caseType = aCase.caseType().get();
         if (caseType != null)
         {
            prototype.caseTypeId().set( ((Identity) caseType).identity().get() );

            Owner caseTypeOwner = ((Ownable.Data) caseType).owner().get();
            if (caseTypeOwner != null)
               prototype.caseTypeOwnerId().set( ((Identity) caseTypeOwner).identity().get() );

            if (aCase.resolution().get() != null)
               prototype.resolutionId().set( ((Identity) aCase.resolution().get()).identity().get() );
         }

         Owner owner = aCase.owner().get();
         prototype.projectId().set( ((Identity) owner).identity().get() );
         OwningOrganizationalUnit.Data po = (OwningOrganizationalUnit.Data) owner;
         OrganizationalUnit organizationalUnit = po.organizationalUnit().get();
         prototype.organizationalUnitId().set( ((Identity) organizationalUnit).identity().get() );

         String groupId = null;
         Participation.Data participant = (Participation.Data) assignee;
         findgroup:
         for (Group group : participant.groups())
         {
            Members.Data members = (Members.Data) owner;
            if (members.members().contains( group ))
            {
               groupId = ((Identity) group).identity().get();
               break findgroup;
            }
         }
         prototype.groupId().set( groupId );

         for (Label label : aCase.labels())
         {
            prototype.labels().get().add( label.toString() );
         }

         ValueBuilder<FormFieldStatisticsValue> formBuilder = module.valueBuilderFactory().newValueBuilder( FormFieldStatisticsValue.class );
         EffectiveFormFieldsValue value = aCase.effectiveFieldValues().get();
         if (value != null)
         {
            for (EffectiveFieldValue effectiveFieldValue : value.fields().get())
            {
               formBuilder.prototype().formId().set( effectiveFieldValue.form().get().identity() );
               formBuilder.prototype().fieldId().set( effectiveFieldValue.field().get().identity() );
               // truncate field value if greater than 500 chars.
               // value in fields table is varchar(500)
               String fieldValue = effectiveFieldValue.value().get();
               fieldValue = fieldValue.length() > 500 ? fieldValue.substring( 0, 500 ) : fieldValue;
               formBuilder.prototype().value().set( fieldValue );
               prototype.fields().get().add( formBuilder.newInstance() );
            }
         }

         return builder.newInstance();
      }

      private void notifyStores( RelatedStatisticsValue relatedStatisticsValue ) throws StatisticsStoreException
      {
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.related( relatedStatisticsValue );
         }
      }

      private void notifyStores( CaseStatisticsValue caseStatisticsValue ) throws StatisticsStoreException
      {
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.caseStatistics( caseStatisticsValue );
         }
      }

      private void notifyStores( String id ) throws StatisticsStoreException
      {
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.removedCase( id );
         }
      }
   }
}
