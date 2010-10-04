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

package se.streamsource.streamflow.web.context.caze;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseTypeQueries;
import se.streamsource.streamflow.web.domain.entity.caze.PossibleActions;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresAssigned;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolvable;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import java.util.List;

import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.CLOSED;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.DRAFT;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.OPEN;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountCacheConcern.class)
@Mixins(CaseActionsContext.Mixin.class)
public interface CaseActionsContext
      extends DeleteContext // , InteractionConstraints
{
   // List possible actions
   public Actions actions();

   public LinksValue possiblesendto();

   public LinksValue possibleresolutions();

   // Commands
   /**
    * Assign the case to the user invoking the method
    */
   @RequiresAssigned(false)
   public void assign();

   /**
    * Mark the draft case as open
    */
   @RequiresStatus({DRAFT})
   public void open();

   /**
    * Mark the case as closed
    */
   @RequiresStatus({OPEN})
   public void close();


   /**
    * Mark the case as resolved and closed
    */
   @RequiresStatus({OPEN})
   public void resolve(EntityReferenceDTO resolution);

   /**
    * Mark the case as on-hold
    */
   @RequiresStatus({OPEN})
   public void onhold();

   public void sendto( EntityReferenceDTO entity );

   @RequiresStatus({CLOSED})
   public void reopen();

   @RequiresStatus({CaseStates.ON_HOLD})
   public void resume();

   @RequiresAssigned(true)
   public void unassign();

   public void delete();

   abstract class Mixin
         extends ContextMixin
         implements CaseActionsContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      // List possible actions
      public Actions actions()
      {
         ValueBuilder<Actions> builder = vbf.newValueBuilder( se.streamsource.streamflow.domain.interaction.gtd.Actions.class );
         List<String> actions = builder.prototype().actions().get();

         PossibleActions possibleActions = roleMap.get( PossibleActions.class );
         Actor actor = roleMap.get(Actor.class);

         possibleActions.addActions( actor, actions );

         return builder.newInstance();
      }

      public LinksValue possiblesendto()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "sendto" );
         List<Project> projects = roleMap.get( CaseTypeQueries.class ).possibleProjects();
         Ownable ownable = roleMap.get(Ownable.class);
         CaseType caseType = roleMap.get( TypedCase.Data.class).caseType().get();
         for (Project project : projects)
         {
            if (!ownable.isOwnedBy( (Owner) project ))
            {
               if (caseType == null || project.hasSelectedCaseType( caseType ))
                  builder.addDescribable( project, ((OwningOrganizationalUnit.Data)project).organizationalUnit().get() );
            }
         }
         return builder.newLinks();
      }

      public LinksValue possibleresolutions()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "resolve" );
         CaseType type = roleMap.get( TypedCase.Data.class ).caseType().get();
         if (type != null)
         {
            Iterable<Resolution> resolutions = type.getSelectedResolutions();
            builder.addDescribables( resolutions );
         }
         return builder.newLinks();
      }

      // Commands
      public void assign()
      {
         Assignable assignable = roleMap.get(Assignable.class);

         Assignee assignee = roleMap.get(Actor.class);

         if (!assignable.isAssigned())
         {
            assignable.assignTo( assignee );
         }
      }

      public void open()
      {
         Status aCase = roleMap.get( Status.class);

         aCase.open();
      }

      public void close()
      {
         CaseEntity aCase = roleMap.get( CaseEntity.class);

         Actor actor = roleMap.get(Actor.class);

         if (!aCase.isAssigned())
         {
            aCase.assignTo( actor );
         }

         aCase.close();
      }

      public void resolve(EntityReferenceDTO resolutionDTO)
      {
         Resolution resolution = uowf.currentUnitOfWork().get( Resolution.class, resolutionDTO.entity().get().identity() );

         Assignable assignable = roleMap.get( Assignable.class);
         Resolvable resolvable = roleMap.get( Resolvable.class);
         Status status = roleMap.get( Status.class);

         Actor actor = roleMap.get(Actor.class);

         if (!assignable.isAssigned())
         {
            assignable.assignTo( actor );
         }

         resolvable.resolve( resolution );

         status.close();
      }

      public void onhold()
      {
         roleMap.get(Status.class).onHold();
      }

      public void sendto( EntityReferenceDTO entity )
      {
         CaseEntity aCase = roleMap.get( CaseEntity.class);

         Owner toOwner = uowf.currentUnitOfWork().get( Owner.class, entity.entity().get().identity() );

         aCase.unassign();

         aCase.changeOwner( toOwner );
      }

      public void reopen()
      {
         // Reopen the case, take away resolution, and assign to user who did the reopen
         Status caze = roleMap.get( Status.class);
         caze.reopen();
         Resolvable resolvable = roleMap.get( Resolvable.class);
         resolvable.unresolve();
         Assignable assignable = roleMap.get( Assignable.class );
         Assignee assignee = roleMap.get( Assignee.class );
         assignable.assignTo( assignee );
      }

      public void resume()
      {
         roleMap.get(Status.class).resume();
      }

      public void unassign()
      {
         Assignable caze = roleMap.get( Assignable.class);

         caze.unassign();
      }

      public void delete()
      {
         Removable caze = roleMap.get( Removable.class);
         caze.deleteEntity();
      }
   }
}