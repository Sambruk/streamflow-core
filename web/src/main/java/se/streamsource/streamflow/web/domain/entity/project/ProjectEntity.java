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
package se.streamsource.streamflow.web.domain.entity.project;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.api.administration.filter.ActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailNotificationActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.IdGenerator;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessDefaults;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.DueOnNotificationSettings;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@SideEffects(ProjectEntity.RemoveMemberSideEffect.class)
@Mixins({ProjectEntity.ProjectIdGeneratorMixin.class})
@Concerns({ProjectEntity.RemovableConcern.class, ProjectEntity.RemoveMemberConcern.class})
public interface ProjectEntity
        extends DomainEntity,

        // Interactions
        IdGenerator,

        // Structure
        Project,
        OwningOrganizationalUnit,

        // Data
        CaseAccessDefaults.Data,
        Members.Data,
        Describable.Data,
        OwningOrganizationalUnit.Data,
        Ownable.Data,
        Ownable.Events,
        Forms.Data,
        Labels.Data,
        SelectedLabels.Data,
        CaseTypes.Data,
        Removable.Data,
        SelectedCaseTypes.Data,
        Filters.Data,
        Filters.Events,
        DueOnNotificationSettings.Data,
        DueOnNotificationSettings.Events,

        // Queries
        AssignmentsQueries,
        InboxQueries,
        ProjectLabelsQueries
{
   class ProjectIdGeneratorMixin
           implements IdGenerator
   {
      @This
      OwningOrganizationalUnit.Data state;

      public void assignId(CaseId aCase)
      {
         Organization organization = ((OwningOrganization) state.organizationalUnit().get()).organization().get();
         ((IdGenerator) organization).assignId(aCase);
      }
   }

   abstract class RemoveMemberSideEffect
           extends SideEffectOf<Members>
           implements Members
   {
      @This
      AssignmentsQueries assignments;

      @Structure
      Module module;

      public void removeMember(Member member)
      {
         // Get all active cases in a project for a particular user and unassign.
         for (Assignable caze : assignments.assignments((Assignee) member, null).newQuery(module.unitOfWorkFactory().currentUnitOfWork()))
         {
            caze.unassign();
         }
      }
   }

   abstract class RemovableConcern
           extends ConcernOf<Removable>
           implements Removable
   {
      @This
      Identity id;

      @This
      Members members;

      @This
      InboxQueries inbox;

      @This
      AssignmentsQueries assignments;

      public boolean removeEntity()
      {
         if (inbox.inboxHasActiveCases()
                 || assignments.assignmentsHaveActiveCases())
         {
            throw new IllegalStateException( ErrorResources.project_remove_failed_open_cases.name() );

         } else
         {
            members.removeAllMembers();
            return next.removeEntity();
         }
      }
   }

   abstract class RemoveMemberConcern
         extends ConcernOf<Members>
         implements Members
   {
      @This
      Filters.Data filters;

      @Structure
      Module module;

      public void removeMember( final Member member )
      {
         next.removeMember( member );

         // find filters that contain actions that are either EmailActionValue or EmailNotificationActionValue
         // that contain the removed member
         // remove the action from the filter
         // and call filters.update to save the change in a fashion that allows event replay

         for( FilterValue filterValue : Iterables.filter( new Specification<FilterValue>()
            {
               public boolean satisfiedBy( FilterValue filter )
               {
                  return Iterables.matchesAny( new Specification<ActionValue>()
                  {
                     public boolean satisfiedBy(ActionValue action )
                     {
                        if( action instanceof EmailActionValue )
                        {
                           return ((EmailActionValue)action).participant().get().equals( EntityReference.getEntityReference( member ) );
                        } else if ( action instanceof EmailNotificationActionValue )
                        {
                           return ((EmailNotificationActionValue)action).participant().get().equals( EntityReference.getEntityReference( member ) );
                        }
                        return false;
                     }
                  }, filter.actions().get() ) ;
               }
            }, filters.filters().get() ) )
         {
            int index = ((Filters) filters).indexOf( filterValue );

            ValueBuilder<FilterValue> builder = module.valueBuilderFactory().newValueBuilder( FilterValue.class ).withPrototype( filterValue );
            List<ActionValue> actionValueList = new ArrayList<ActionValue>(  );
            for( ActionValue action : builder.prototype().actions().get() )
            {
               if( !( action instanceof EmailActionValue || action instanceof EmailNotificationActionValue ) )
               {
                  actionValueList.add( action );
               } else if( action instanceof EmailActionValue )
               {
                  if( !((EmailActionValue)action).participant().get().equals( EntityReference.getEntityReference( member ) ))
                  {
                     actionValueList.add( action );
                  }
               } else if ( action instanceof  EmailNotificationActionValue )
               {
                  if( !((EmailNotificationActionValue)action).participant().get().equals( EntityReference.getEntityReference( member ) ))
                  {
                     actionValueList.add( action );
                  }
               }
            }
            builder.prototype().actions().set( actionValueList );

            ((Filters)filters).updateFilter( index, builder.newInstance() );
         }
      }
   }
}
