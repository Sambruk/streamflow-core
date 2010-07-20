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

package se.streamsource.streamflow.web.application.statistics;

import com.sun.tools.internal.ws.wsdl.framework.NoSuchEntityException;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventQuery;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventRouter;
import se.streamsource.streamflow.infrastructure.event.source.helper.TransactionEventAdapter;
import se.streamsource.streamflow.infrastructure.event.source.helper.TransactionTracker;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Participation;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.Date;

/**
 * JAVADOC
 */
@Mixins(CaseStatisticsService.Mixin.class)
public interface CaseStatisticsService
      extends ServiceComposite, Activatable, CaseStatistics
{
   class Mixin
         implements TransactionVisitor, Activatable, CaseStatistics
   {
      @Service
      EventStore eventStore;

      @Service
      EventSource source;

      @Service
      Iterable<StatisticsStore> statisticsStores;

      @Structure
      ModuleSPI module;

      @This
      Configuration<StatisticsConfiguration> config;

      TransactionTracker tracker;
      EventRouter router;

      Logger log;
      public TransactionEventAdapter transactionAdapter;

      public void activate() throws Exception
      {
         log = LoggerFactory.getLogger( CaseStatisticsService.class );

         router = new EventRouter().route( new EventQuery()
         {
            @Override
            public boolean accept( DomainEvent event )
            {
               return super.accept( event ) && EventParameters.getParameter( event, "param1" ).equals( CaseStates.CLOSED.name() );
            }
         }.withNames( "changedStatus" ), new EventVisitor()
         {
            public boolean visit( DomainEvent event )
            {
               // Case was closed
               UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Create statistics" ) );
               try
               {
                  CaseEntity entity = uow.get( CaseEntity.class, event.entity().get() );
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
         } ).route( new EventQuery().withNames( "changedDescription" ).onEntityTypes(
               UserEntity.class.getName(),
               LabelEntity.class.getName(),
               GroupEntity.class.getName(),
               ProjectEntity.class.getName(),
               OrganizationEntity.class.getName(),
               OrganizationalUnitEntity.class.getName() ),
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
               } );


         transactionAdapter = new TransactionEventAdapter( router );
         tracker = new TransactionTracker( eventStore, source, config, this );
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public void refresh() throws StatisticsStoreException
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

               // Group
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

               // OU
               {
                  log.debug( "Update all OU's" );
                  Query<OrganizationalUnitEntity> ous = module.queryBuilderFactory().newQueryBuilder( OrganizationalUnitEntity.class ).newQuery( uow );
                  for (OrganizationalUnitEntity ou : ous)
                  {
                     notifyStores( createRelated( ou, RelatedEnum.organizationalUnit ) );
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

      public boolean visit( TransactionEvents transaction )
      {
         return transactionAdapter.visit( transaction );
      }

      private RelatedStatisticsValue createRelated( EntityComposite entity, RelatedEnum type )
      {
         ValueBuilder<RelatedStatisticsValue> builder = module.valueBuilderFactory().newValueBuilder( RelatedStatisticsValue.class );
         builder.prototype().identity().set( entity.identity().get() );
         builder.prototype().description().set( ((Describable) entity).getDescription() );
         builder.prototype().relatedType().set( type );
         return builder.newInstance();
      }

      private CaseStatisticsValue createStatistics( CaseEntity aCase )
      {
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
            UnitOfWork uow = module.unitOfWorkFactory().getUnitOfWork( aCase );
            prototype.caseTypeId().set( ((Identity) caseType).identity().get() );

            QueryBuilder<Identity> caseOwnerQuery = module.queryBuilderFactory().newQueryBuilder( Identity.class );
            ManyAssociation<CaseType> caseTypes = QueryExpressions.templateFor( CaseTypes.Data.class ).caseTypes();
            Identity caseTypeOwner = caseOwnerQuery.where( QueryExpressions.contains( caseTypes, caseType ) ).newQuery( uow ).find();
            if (caseTypeOwner != null)
               prototype.caseTypeOwnerId().set( caseTypeOwner.identity().get() );

            if (aCase.resolution().get() != null)
               prototype.resolutionId().set( ((Identity)aCase.resolution().get()).identity().get());
         }

         Owner owner = aCase.owner().get();
         OwningOrganizationalUnit.Data po = (OwningOrganizationalUnit.Data) owner;
         OrganizationalUnit organizationalUnit = po.organizationalUnit().get();
         prototype.organizationalUnit().set( organizationalUnit.getDescription() );

         String groupId = null;
         Participation.Data participant = (Participation.Data) assignee;
         findgroup:
         for (Group group : participant.groups())
         {
            Members.Data members = (Members.Data) owner;
            if (members.members().contains( group ))
            {
               groupId = ((Identity)group).identity().get();
               break findgroup;
            }
         }
         prototype.groupId().set( groupId );

         for (Label label : aCase.labels())
         {
            prototype.labels().get().add( label.toString() );
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
   }
}
