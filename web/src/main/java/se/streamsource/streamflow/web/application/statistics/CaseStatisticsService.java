/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionVisitor;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventRouter;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTracker;
import se.streamsource.streamflow.util.HierarchicalVisitor;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.ResolutionEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.FieldId;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormId;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedPageValue;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Participation;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

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
         log = LoggerFactory.getLogger(CaseStatisticsService.class);

         router = new EventRouter().route(and(withNames("changedStatus"), paramIs("param1", CaseStates.CLOSED.name())),
               new EventVisitor()
               {
                  public boolean visit(DomainEvent event)
                  {
                     // Case was closed
                     UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Create statistics"));
                     try
                     {
                        CaseEntity entity = null;
                        try
                        {
                           entity = uow.get(CaseEntity.class, event.entity().get());
                        } catch (NoSuchEntityException e)
                        {
                           // Entity has been deleted. Ignore it
                           return true;
                        }

                        // case has been reopend and is still not closed again
                        // do nothing
                        if (!entity.isStatus(CaseStates.CLOSED) || !entity.isAssigned())
                           return true;

                        CaseStatisticsValue stats = createStatistics(entity);
                        try
                        {
                           notifyStores(stats);
                           return true;
                        } catch (StatisticsStoreException e)
                        {
                           log.warn(e.getMessage(), e.getCause());
                           return false;
                        }
                     } finally
                     {
                        uow.discard();
                     }
                  }
               }).route(and(withNames("changedDescription", "changedFieldId", "changedFormId"), onEntityTypes(
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
         )),
               new EventVisitor()
               {
                  public boolean visit(DomainEvent event)
                  {
                     // Description of related entity was updated
                     UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Change related description"));
                     try
                     {
                        EntityComposite entity = uow.get(DomainEntity.class, event.entity().get());
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

                        RelatedStatisticsValue related = createRelated(entity, type);
                        try
                        {
                           notifyStores(related);
                           return true;
                        } catch (StatisticsStoreException e)
                        {
                           log.warn(e.getMessage(), e.getCause());
                           return false;
                        }
                     } catch (NoSuchEntityException ex)
                     {
                        log.warn("Could not update database information due to missing entity", ex);
                        return true;
                     } finally
                     {
                        uow.discard();
                     }
                  }
               }).route(and(withNames("deletedEntity"), onEntityTypes(CaseEntity.class.getName())),
               new EventVisitor()
               {
                  public boolean visit(DomainEvent event)
                  {
                     try
                     {
                        notifyStores(event.entity().get());
                        return true;
                     } catch (StatisticsStoreException e)
                     {
                        log.warn(e.getMessage(), e.getCause());
                        return false;
                     }
                  }
               }).route(withNames("addedOrganizationalUnit", "removedOrganizationalUnit"), new EventVisitor()
         {
            public boolean visit(DomainEvent event)
            {
               // Create organizational structure
               UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Changed structure"));
               try
               {
                  OrganizationalStructureValue structure = createStructure();
                  notifyStores(structure);
                  return true;
               } catch (StatisticsStoreException e)
               {
                  log.warn(e.getMessage(), e.getCause());
                  return false;
               } finally
               {
                  uow.discard();
               }
            }
         });

         transactionAdapter = Events.adapter(router);
         tracker = new TransactionTracker(stream, eventSource, config, this);
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public void refreshStatistics() throws StatisticsStoreException
      {
         DateTime dateTimeStart = new DateTime(  );
         log.info("Refresh all statistics started at: " + dateTimeStart.toString( "YYYY-MM-dd HH:mm" ) );

         try
         {
            // stop service
            passivate();

            // First clear the statistics stores of all their existing data
            log.debug("Clear all statistics stores");
            for (StatisticsStore statisticsStore : statisticsStores)
            {
               statisticsStore.clearAll();
            }

            // reset configuration
            config.configuration().lastEventDate().set( 0L );
            config.save();

            // start service
            activate();

            DateTime dateTimeEnd = new DateTime(  );
            log.info( "Refresh all statistics stoped at: " + dateTimeEnd.toString( "YYYY-MM-dd HH:mm" ));
            Period period = new Duration( dateTimeStart, dateTimeEnd ).toPeriod();
            log.info(  "Time elapsed: " + period.getDays() + " days " + period.getHours() + " hours " + period.getMinutes() + " minutes " + period.getSeconds() + " seconds." );

         } catch (Exception e)
         {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new StatisticsStoreException( "Refresh statistics failed.", e  );
         }
      }

      public boolean visit(TransactionDomainEvents transactionDomain)
      {
         return transactionAdapter.visit(transactionDomain);
      }

      private RelatedStatisticsValue createRelated(EntityComposite entity, RelatedEnum type)
      {
         ValueBuilder<RelatedStatisticsValue> builder = module.valueBuilderFactory().newValueBuilder(RelatedStatisticsValue.class);
         builder.prototype().identity().set(entity.identity().get());
         if (entity instanceof Form)
         {
            builder.prototype().description().set(((FormId.Data) entity).formId().get());
         } else if (entity instanceof Field)
         {
            builder.prototype().description().set(((FieldId.Data) entity).fieldId().get());
         } else
         {
            builder.prototype().description().set(((Describable) entity).getDescription());
         }

         builder.prototype().relatedType().set(type);
         return builder.newInstance();
      }

      private OrganizationalStructureValue createStructure()
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         OrganizationsEntity organizations = uow.get(OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID);

         final List<OrganizationalUnitValue> ous = new ArrayList<OrganizationalUnitValue>();

         OrganizationEntity org = (OrganizationEntity) organizations.organization().get();
         org.accept(new HierarchicalVisitor<Object, Object, RuntimeException>()
         {
            int idx = 0;

            Stack<ValueBuilder<OrganizationalUnitValue>> builders = new Stack<ValueBuilder<OrganizationalUnitValue>>();

            @Override
            public boolean visitEnter(Object visited) throws RuntimeException
            {
               if (visited instanceof OrganizationalUnit || visited instanceof Organization)
               {
                  ValueBuilder<OrganizationalUnitValue> builder = module.valueBuilderFactory().newValueBuilder(OrganizationalUnitValue.class);

                  builder.prototype().name().set(((Describable)visited).getDescription());
                  builder.prototype().id().set(visited.toString());
                  builder.prototype().left().set(idx);
                  if (visited instanceof OrganizationalUnit) {
                     builder.prototype().parent().set( EntityReference.getEntityReference( ((Ownable.Data)visited).owner().get()).identity() );
                  }
                  builders.push(builder);

                  idx++;
               }

               return super.visitEnter(visited);
            }

            @Override
            public boolean visitLeave(Object visited) throws RuntimeException
            {
               if (visited instanceof OrganizationalUnit || visited instanceof Organization)
               {
                  ValueBuilder<OrganizationalUnitValue> builder = builders.pop();
                  builder.prototype().right().set(idx);
                  ous.add(builder.newInstance());

                  idx++;
               }

               return super.visitLeave(visited);
            }
         });

         ValueBuilder<OrganizationalStructureValue> builder = module.valueBuilderFactory().newValueBuilder(OrganizationalStructureValue.class);
         builder.prototype().structure().get().addAll(ous);
         return builder.newInstance();
      }

      private CaseStatisticsValue createStatistics(CaseEntity aCase)
      {
         ValueBuilder<CaseStatisticsValue> builder = module.valueBuilderFactory().newValueBuilder(CaseStatisticsValue.class);
         CaseStatisticsValue prototype = builder.prototype();

         prototype.identity().set(aCase.identity().get());
         prototype.description().set(aCase.getDescription() == null ? "" : aCase.getDescription() );
         Assignee assignee = aCase.assignedTo().get();
         prototype.assigneeId().set(((Identity) assignee).identity().get());
         prototype.caseId().set(aCase.caseId().get());
         prototype.createdOn().set(new Date(aCase.createdOn().get().getTime()));
         Date closeDate = aCase.closedOn().get();
         prototype.closedOn().set(new Date(closeDate.getTime()));
         prototype.duration().set(closeDate.getTime() - aCase.createdOn().get().getTime());
         prototype.dueOn().set(aCase.dueOn().get());
         if (aCase.casepriority().get() != null)
         {
            prototype.priority().set(aCase.casepriority().get().getDescription());
         }

         CaseType caseType = aCase.caseType().get();
         if (caseType != null)
         {
            prototype.caseTypeId().set(((Identity) caseType).identity().get());

            Owner caseTypeOwner = ((Ownable.Data) caseType).owner().get();
            if (caseTypeOwner != null)
               prototype.caseTypeOwnerId().set(((Identity) caseTypeOwner).identity().get());

            if (aCase.resolution().get() != null)
               prototype.resolutionId().set(((Identity) aCase.resolution().get()).identity().get());
         }

         Owner owner = aCase.owner().get();
         prototype.projectId().set(((Identity) owner).identity().get());
         OwningOrganizationalUnit.Data po = (OwningOrganizationalUnit.Data) owner;
         OrganizationalUnit organizationalUnit = po.organizationalUnit().get();
         prototype.organizationalUnitId().set(((Identity) organizationalUnit).identity().get());

         String groupId = null;
         Participation.Data participant = (Participation.Data) assignee;
         findgroup:
         for (Group group : participant.groups())
         {
            Members.Data members = (Members.Data) owner;
            if (members.members().contains(group))
            {
               groupId = ((Identity) group).identity().get();
               break findgroup;
            }
         }
         prototype.groupId().set(groupId);

         for (Label label : aCase.labels())
         {
            prototype.labels().get().add(label.toString());
         }

         ValueBuilder<FormFieldStatisticsValue> formBuilder = module.valueBuilderFactory().newValueBuilder(FormFieldStatisticsValue.class);

         for (SubmittedFormValue submittedFormValue : aCase.getLatestSubmittedForms())
         {
            for (SubmittedPageValue submittedPageValue : submittedFormValue.pages().get())
            {
               for (SubmittedFieldValue submittedFieldValue : submittedPageValue.fields().get())
               {
                  FieldEntity fieldEntity  = module.unitOfWorkFactory().currentUnitOfWork()
                           .get( FieldEntity.class, submittedFieldValue.field().get().identity() );
                     
                  if (fieldEntity.isStatistical()) {
                     formBuilder.prototype().formId().set(submittedFormValue.form().get().identity());
                     formBuilder.prototype().fieldId().set(submittedFieldValue.field().get().identity());
                     if (fieldEntity.datatype().get() != null) {
                        formBuilder.prototype().datatype().set( fieldEntity.datatype().get().getUrl() );
                     } else {
                        formBuilder.prototype().datatype().set( "" );
                     }
                     // truncate field value if greater than 500 chars.
                     // value in fields table is varchar(500)
                     String fieldValue = submittedFieldValue.value().get();
                     fieldValue = fieldValue.length() > 500 ? fieldValue.substring(0, 500) : fieldValue;
                     formBuilder.prototype().value().set(fieldValue);
                     prototype.fields().get().add(formBuilder.newInstance());
                  }
               }
            }
         }

         return builder.newInstance();
      }

      private void notifyStores(RelatedStatisticsValue relatedStatisticsValue) throws StatisticsStoreException
      {
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.related(relatedStatisticsValue);
         }
      }

      private void notifyStores(OrganizationalStructureValue structureValue) throws StatisticsStoreException
      {
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.structure(structureValue);
         }
      }

      private void notifyStores(CaseStatisticsValue caseStatisticsValue) throws StatisticsStoreException
      {
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.caseStatistics(caseStatisticsValue);
         }
      }

      private void notifyStores(String id) throws StatisticsStoreException
      {
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.removedCase(id);
         }
      }
   }
}
