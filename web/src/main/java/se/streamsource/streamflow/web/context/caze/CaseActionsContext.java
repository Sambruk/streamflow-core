/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
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
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseTypeQueries;
import se.streamsource.streamflow.web.domain.entity.caze.PossibleActions;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresAssigned;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresDelegated;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresOwner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.List;

import static se.streamsource.streamflow.domain.interaction.gtd.States.*;

/**
 * JAVADOC
 */
@Mixins(CaseActionsContext.Mixin.class)
public interface CaseActionsContext
      extends DeleteInteraction // , InteractionConstraints
{
   // List possible actions
   public Actions actions();

   public LinksValue possiblesendtoprojects();

   @RequiresOwner(User.class)
   public LinksValue possiblesendtousers();

   public LinksValue possibledelegateprojects();

   @RequiresOwner(User.class)
   public LinksValue possibledelegateusers();

   // Commands

   /**
    * Accept a delegated case.
    */
   @RequiresDelegated(true)
   public void accept();

   /**
    * Assign the case to the user invoking the method
    */
   @RequiresAssigned(false)
   public void assign();

   /**
    * Mark the case as completed
    */
   @RequiresStatus({ACTIVE, DONE})
   public void complete();

   /**
    * Mark the case as done.
    */
   @RequiresStatus({ACTIVE})
   public void done();

   public void onhold();

   public void sendto( EntityReferenceDTO entity );

   @RequiresDelegated(false)
   public void delegate( EntityReferenceDTO entity );

   @RequiresStatus({ACTIVE, DONE})
   public void drop();

   @RequiresStatus({COMPLETED, DROPPED})
   public void reactivate();

   @RequiresStatus({DONE})
   public void redo();

   @RequiresDelegated(true)
   public void reject();

   @RequiresStatus({States.ON_HOLD})
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

      public LinksValue possiblesendtoprojects()
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

      public LinksValue possiblesendtousers()
      {
         List<User> users = context.get( CaseTypeQueries.class ).possibleUsers();

         LinksBuilder links = new LinksBuilder(module.valueBuilderFactory()).command( "sendto" );
         Ownable ownable = context.get(Ownable.class);

         for (User user : users)
         {
            if (!ownable.isOwnedBy( (Owner) user ))
            {
               String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
               links.addDescribable( user, group );
            }
         }

         return links.newLinks();
      }

      public LinksValue possibledelegateprojects()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "delegate" );
         List<Project> projects = context.get( CaseTypeQueries.class ).possibleProjects();
         CaseType caseType = context.get( TypedCase.Data.class).caseType().get();
         Ownable ownable = context.get(Ownable.class);
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

      public LinksValue possibledelegateusers()
      {
         List<User> users = context.get( CaseTypeQueries.class ).possibleUsers();

         LinksBuilder links = new LinksBuilder(module.valueBuilderFactory()).command( "delegate" );

         Ownable ownable = context.get(Ownable.class);
         for (User user : users)
         {
            if (!ownable.isOwnedBy( (Owner) user ))
            {
               String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
               links.addDescribable( user, group );
            }
         }

         return links.newLinks();
      }

      // Commands
      public void accept()
      {
         Assignable assignable = context.get( Assignable.class );
         Ownable ownable = context.get( Ownable.class );
         Delegatable.Data delegatable = context.get( Delegatable.Data.class );

         Actor actor = context.get(Actor.class);

         if (!assignable.isAssigned())
         {
            // Delegations
            Owner owner = (Owner) delegatable.delegatedTo().get();
            assignable.assignTo( actor );
            ownable.sendTo( owner );
         }
      }

      public void assign()
      {
         Assignable assignable = context.get(Assignable.class);

         Assignee assignee = context.get(Actor.class);

         if (!assignable.isAssigned())
         {
            assignable.assignTo( assignee );
         }
      }

      public void complete()
      {
         CaseEntity aCase = context.get( CaseEntity.class);

         Owner owner = aCase.owner().get();

         Actor actor = context.get(Actor.class);

         if (!aCase.isAssigned())
         {
            // Inbox or WaitingFor
            if (aCase.isDelegatedBy( actor ))
            {
               aCase.sendTo( owner );

            }

            aCase.assignTo( actor );
         }

         aCase.complete();
      }

      public void done()
      {
         CaseEntity aCase = context.get( CaseEntity.class);

         aCase.done();
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

      public void delegate( EntityReferenceDTO entity )
      {
         CaseEntity aCase = context.get( CaseEntity.class);

         Delegatee to = uowf.currentUnitOfWork().get( Delegatee.class, entity.entity().get().identity() );

         Owner owner = aCase.owner().get();

         Actor actor = context.get(Actor.class);

         if (aCase.isAssigned())
            aCase.unassign();

         aCase.delegateTo( to, actor, owner );
      }

      public void drop()
      {
         CaseEntity aCase = context.get( CaseEntity.class);

         Actor actor = context.get(Actor.class);

         if (!aCase.isAssigned())
         {
            aCase.assignTo( actor );
         }

         aCase.drop();
      }

      public void reactivate()
      {
         Status caze = context.get( CaseEntity.class);
         caze.reactivate();
      }

      public void redo()
      {
         Status caze = context.get( CaseEntity.class);
         caze.redo();
      }

      public void reject()
      {
         CaseEntity aCase = context.get( CaseEntity.class);

         aCase.rejectDelegation();
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