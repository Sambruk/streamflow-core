/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.entity.task;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import java.util.Collection;

/**
 * Figure out what possible actions can be performed
 * on a task by a user
 */
@Mixins(PossibleActions.Mixin.class)
public interface PossibleActions
{
   void addActions( Actor actor, Collection<String> actions );

   class Mixin
         implements PossibleActions
   {
      @This
      TaskEntity task;

      public void addActions( Actor actor, Collection<String> actions )
      {
         if (task.owner().get() instanceof Project)
         {
            // Project owned task
            Project project = (Project) task.owner().get();
            if (((Member) actor).isMember( project ))
            {
               if (task.isAssignedTo( actor ))
               {
                  if (task.isDelegated())
                  {
                     // Assignments (delegated)
                     if (task.isStatus( States.ACTIVE ))
                     {
                        actions.add( "done" );
                        actions.add( "reject" );
                        actions.add( "label" );
                     }
                  } else
                  {
                     // Assignments (mine)
                     if (task.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "unassign" );
                        actions.add( "label" );
                     }
                  }
               } else
               {
                  if (task.isDelegatedBy( actor ))
                  {
                     // WaitingFor (not assigned)
                     if (task.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "tasktype" );
                     } else if (task.isStatus( States.DONE ))
                     {
                        actions.add( "complete" );
                        actions.add( "redo" );
                     }

                  } else if (!task.isAssigned())
                  {
                     // Inbox
                     if (task.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "tasktype" );
                        actions.add( "label" );
                     }
                  } else
                  {
                     // Someone else in my project is assigned to it
                  }
               }
            } else
            {
               if (task.isDelegatedBy( actor ))
               {
                  // WaitingFor (assigned)
                  if (task.isStatus( States.ACTIVE ))
                  {
                     actions.add( "complete" );
                     actions.add( "assign" );
                     actions.add( "sendto" );
                     actions.add( "delegate" );
                     actions.add( "drop" );
                     actions.add( "delete" );
                  } else if (task.isStatus( States.DONE ))
                  {
                     actions.add( "complete" );
                     actions.add( "redo" );
                  }

               } else if (task.isDelegatedTo( actor ))
               {
                  // Delegations
                  if (task.isStatus( States.ACTIVE ))
                  {
                     actions.add( "accept" );
                     actions.add( "reject" );
                     actions.add( "done" );
                  }
               }
            }

         } else
         {
            // User actions
            if (task.isOwnedBy( actor ))
            {
               if (task.isAssignedTo( actor ))
               {
                  if (task.isDelegatedTo( actor ))
                  {
                     // Assignments (delegated)
                     if (task.isStatus( States.ACTIVE ))
                     {
                        actions.add( "done" );
                        actions.add( "reject" );
                        actions.add( "label" );
                     }
                  } else
                  {
                     // Assignments (mine)
                     if (task.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "unassign" );
                        actions.add( "tasktype" );
                        actions.add( "label" );
                     }
                  }
               } else
               {
                  if (task.isDelegatedBy( actor ))
                  {
                     // WaitingFor (not assigned)
                     if (task.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "tasktype" );
                        actions.add( "label" );
                     }
                  } else
                  {
                     // Inbox
                     if (task.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "tasktype" );
                        actions.add( "label" );
                     }
                  }
               }
            } else
            {
               if (task.isDelegatedBy( actor ))
               {
                  // WaitingFor (assigned)
                  if (task.isStatus( States.ACTIVE ))
                  {
                     actions.add( "complete" );
                     actions.add( "assign" );
                     actions.add( "sendto" );
                     actions.add( "delegate" );
                     actions.add( "drop" );
                     actions.add( "delete" );
                  } else if (task.isStatus( States.DONE ))
                  {
                     actions.add( "complete" );
                     actions.add( "redo" );
                  }


               } else if (task.isDelegatedTo( actor ))
               {
                  // Delegations
                  if (task.isStatus( States.ACTIVE ))
                  {
                     actions.add( "accept" );
                     actions.add( "reject" );
                     actions.add( "done" );
                  }
               }
            }
         }
      }
   }
}
