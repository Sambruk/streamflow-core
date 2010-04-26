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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.InteractionsMixin;
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
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresOwner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.List;

import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.CLOSED;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.DRAFT;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.OPEN;

/**
 * JAVADOC
 */
@Mixins(CaseActionsContext.Mixin.class)
public interface CaseActionsContext
      extends DeleteInteraction // , InteractionConstraints
{
   // List possible actions
   public Actions actions();

   public LinksValue possiblesendto();

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
    * Mark the case as closed // TODO Add resolution here
    */
   @RequiresStatus({OPEN})
   public void close();

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
         extends InteractionsMixin
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

         PossibleActions possibleActions = context.get( PossibleActions.class );
         Actor actor = context.get(Actor.class);

         possibleActions.addActions( actor, actions );

         return builder.newInstance();
      }

      public LinksValue possiblesendto()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "sendto" );
         List<Project> projects = context.get( CaseTypeQueries.class ).possibleProjects();
         Ownable ownable = context.get(Ownable.class);
         CaseType caseType = context.get( TypedCase.Data.class).caseType().get();
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

      // Commands
      public void assign()
      {
         Assignable assignable = context.get(Assignable.class);

         Assignee assignee = context.get(Actor.class);

         if (!assignable.isAssigned())
         {
            assignable.assignTo( assignee );
         }
      }

      public void open()
      {
         Status aCase = context.get( Status.class);

         aCase.open();
      }

      public void close()
      {
         CaseEntity aCase = context.get( CaseEntity.class);

         Actor actor = context.get(Actor.class);

         if (!aCase.isAssigned())
         {
            aCase.assignTo( actor );
         }

         aCase.close();
      }

      public void onhold()
      {
         context.get(Status.class).onHold();
      }

      public void sendto( EntityReferenceDTO entity )
      {
         CaseEntity aCase = context.get( CaseEntity.class);

         Owner toOwner = uowf.currentUnitOfWork().get( Owner.class, entity.entity().get().identity() );

         aCase.unassign();

         aCase.sendTo( toOwner );
      }

      public void reopen()
      {
         Status caze = context.get( CaseEntity.class);
         caze.reopen();
      }

      public void resume()
      {
         context.get(Status.class).resume();
      }

      public void unassign()
      {
         Assignable caze = context.get( CaseEntity.class);

         caze.unassign();
      }

      public void delete()
      {
         Removable caze = context.get( CaseEntity.class);
         caze.deleteEntity();
      }
   }
}