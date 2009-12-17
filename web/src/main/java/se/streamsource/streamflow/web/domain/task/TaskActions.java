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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.user.User;

import java.util.Collection;

/**
 * Figure out what possible actions can be performed
 * on a task by a user
 */
@Mixins(TaskActions.Mixin.class)
public interface TaskActions
{
   void addActions( User user, Collection<String> actions );

   class Mixin
         implements TaskActions
   {
      @This
      TaskEntity task;

      public void addActions( User user, Collection<String> actions )
      {
         if (task.owner().get() instanceof Project)
         {
            // Project owned task
            Project project = (Project) task.owner().get();
            if (project.isMember( user ))
            {
               if (task.isAssignedTo( user ))
               {
                  if (task.isDelegated())
                  {
                     // Assignments (delegated)
                     if (task.isStatus( TaskStates.ACTIVE ))
                     {
                        actions.add( "done" );
                        actions.add( "reject" );
                     }
                  } else
                  {
                     // Assignments (mine)
                     if (task.isStatus( TaskStates.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "forward" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                     }
                  }
               } else
               {
                  if (task.isDelegatedBy( user ))
                  {
                     // WaitingFor (not assigned)
                     if (task.isStatus( TaskStates.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "forward" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                     }

                  } else if (!task.isAssigned())
                  {
                     // Inbox
                     if (task.isStatus( TaskStates.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "forward" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                     }
                  } else
                  {
                     // Someone else in my project is assigned to it
                  }
               }
            } else
            {
               if (task.isDelegatedBy( user ))
               {
                  // WaitingFor (assigned)
                  if (task.isStatus( TaskStates.ACTIVE ))
                  {
                     actions.add( "complete" );
                     actions.add( "assign" );
                     actions.add( "forward" );
                     actions.add( "delegate" );
                     actions.add( "drop" );
                     actions.add( "delete" );
                  } else if (task.isStatus( TaskStates.DONE ))
                  {
                     actions.add( "finish" );
                     actions.add( "redo" );
                  }

               } else if (task.isDelegatedTo( user ))
               {
                  // Delegations
                  if (task.isStatus( TaskStates.ACTIVE ))
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
            if (task.isOwnedBy( user ))
            {
               if (task.isAssignedTo( user ))
               {
                  if (task.isDelegatedTo( user ))
                  {
                     // Assignments (delegated)
                     if (task.isStatus( TaskStates.ACTIVE ))
                     {
                        actions.add( "done" );
                        actions.add( "reject" );
                     }
                  } else
                  {
                     // Assignments (mine)
                     if (task.isStatus( TaskStates.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "forward" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                     }
                  }
               } else
               {
                  if (task.isDelegatedBy( user ))
                  {
                     // WaitingFor (not assigned)
                     if (task.isStatus( TaskStates.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "forward" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                     }
                  } else
                  {
                     // Inbox
                     if (task.isStatus( TaskStates.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "forward" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                     }
                  }
               }
            } else
            {
               if (task.isDelegatedBy( user ))
               {
                  // WaitingFor (assigned)
                  if (task.isStatus( TaskStates.ACTIVE ))
                  {
                     actions.add( "complete" );
                     actions.add( "assign" );
                     actions.add( "forward" );
                     actions.add( "delegate" );
                     actions.add( "drop" );
                     actions.add( "delete" );
                  } else if (task.isStatus( TaskStates.DONE ))
                  {
                     actions.add( "finish" );
                     actions.add( "redo" );
                  }


               } else if (task.isDelegatedTo( user ))
               {
                  // Delegations
                  if (task.isStatus( TaskStates.ACTIVE ))
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
