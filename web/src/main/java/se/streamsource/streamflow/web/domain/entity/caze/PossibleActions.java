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

package se.streamsource.streamflow.web.domain.entity.caze;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;

import java.util.Collection;

/**
 * Figure out what possible actions can be performed
 * on a aCase by a user
 */
@Mixins(PossibleActions.Mixin.class)
public interface PossibleActions
{
   void addActions( Actor actor, Collection<String> actions );

   class Mixin
         implements PossibleActions
   {
      @This
      CaseEntity aCase;

      public void addActions( Actor actor, Collection<String> actions )
      {
         // droped cases can be reactivated from anyone from anywhere
         if( aCase.isStatus( States.DROPPED ) && !actions.contains( "reactivate" ))
         {
            actions.add( "reactivate" );
         }
         
         if (aCase.owner().get() instanceof Project)
         {
            // Project owned aCase
            Project project = (Project) aCase.owner().get();
            if (((Member) actor).isMember( project ))
            {
               if (aCase.isAssignedTo( actor ))
               {
                  if (aCase.isDelegated())
                  {
                     if (aCase.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "onhold" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "unassign" );
                     }
                     // Assignments (delegated)
                     else if (aCase.isStatus( States.DELEGATED ))
                     {
                        actions.add( "done" );
                        actions.add( "reject" );
                     }
                     // Waiting for
                     else if ( aCase.isStatus( States.DONE ))
                     {
                        actions.add( "complete" );
                        actions.add( "redo" );
                     } else if (aCase.isStatus( States.COMPLETED ))
                     {
                        actions.add( "reactivate" );
                     }

                  } else
                  {
                     // Assignments (mine)
                     if (aCase.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "onhold" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "unassign" );
                     } else if (aCase.isStatus( States.COMPLETED ))
                     {
                        actions.add( "reactivate" );
                     } else if (aCase.isStatus( States.ON_HOLD ))
                     {
                        actions.add( "resume" );
                     }
                  }
               } else
               {
                  if (aCase.isDelegatedBy( actor ))
                  {
                     // WaitingFor (not assigned)
                     if (aCase.isStatus( States.ACTIVE )
                           || aCase.isStatus( States.DELEGATED ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                     } else if (aCase.isStatus( States.DONE ))
                     {
                        actions.add( "complete" );
                        actions.add( "redo" );
                     } else if (aCase.isStatus( States.COMPLETED ))
                     {
                        actions.add( "reactivate" );
                     }

                  } else if (!aCase.isAssigned())
                  {
                     // Inbox
                     if (aCase.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );

                        CaseType type = aCase.caseType().get();
                        if (type == null || project.hasSelectedCaseType( type ))
                        {
                           actions.add( "assign" );
                        }
                        
                        actions.add( "sendto" );
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
               if (aCase.isDelegatedBy( actor ))
               {
                  // WaitingFor (assigned)
                  if (aCase.isStatus( States.ACTIVE )
                        || aCase.isStatus( States.DELEGATED ))
                  {
                     actions.add( "complete" );
                     actions.add( "assign" );
                     actions.add( "sendto" );
                     actions.add( "delegate" );
                     actions.add( "drop" );
                     actions.add( "delete" );
                  } else if (aCase.isStatus( States.DONE ))
                  {
                     actions.add( "complete" );
                     actions.add( "redo" );
                  } else if (aCase.isStatus( States.COMPLETED ))
                  {
                     actions.add( "reactivate" );
                  }

               } else if (aCase.isDelegatedTo( actor ))
               {
                  // Delegations
                  if (aCase.isStatus( States.DELEGATED ))
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
            if (aCase.isOwnedBy( actor ))
            {
               if (aCase.isAssignedTo( actor ))
               {
                  if (aCase.isDelegatedTo( actor ))
                  {
                     if (aCase.isStatus( States.ACTIVE ) )
                     {
                        actions.add( "complete" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "onhold" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "unassign" );
                     }
                     // Assignments (delegated)
                     else if (aCase.isStatus( States.DELEGATED ))
                     {
                        actions.add( "done" );
                        actions.add( "reject" );
                     } else if ( aCase.isStatus( States.DONE ))
                     {
                        actions.add( "complete" );
                        actions.add( "redo" );
                     } else if (aCase.isStatus( States.COMPLETED ))
                     {
                        actions.add( "reactivate" );
                     }
                  } else
                  {
                     // Assignments (mine)
                     if (aCase.isStatus( States.ACTIVE )
                           || aCase.isStatus( States.DELEGATED ))
                     {
                        actions.add( "complete" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "onhold" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                        actions.add( "unassign" );
                     } else if (aCase.isStatus( States.COMPLETED ))
                     {
                        actions.add( "reactivate" );
                     } else if (aCase.isStatus( States.ON_HOLD ))
                     {
                        actions.add( "resume" );
                     } else if ( aCase.isStatus( States.DONE ))
                     {
                        actions.add( "complete" );
                        actions.add( "redo" );
                     }
                  }
               } else
               {
                  if (aCase.isDelegatedBy( actor ))
                  {
                     // WaitingFor (not assigned)
                     if (aCase.isStatus( States.ACTIVE )
                           || aCase.isStatus( States.DELEGATED ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                     }
                  } else
                  {
                     // Inbox
                     if (aCase.isStatus( States.ACTIVE ))
                     {
                        actions.add( "complete" );
                        actions.add( "assign" );
                        actions.add( "sendto" );
                        actions.add( "delegate" );
                        actions.add( "drop" );
                        actions.add( "delete" );
                     }
                  }
               }
            } else
            {
               if (aCase.isDelegatedBy( actor ))
               {
                  // WaitingFor (assigned)
                  if (aCase.isStatus( States.ACTIVE )
                        || aCase.isStatus( States.DELEGATED ))
                  {
                     actions.add( "complete" );
                     actions.add( "assign" );
                     actions.add( "sendto" );
                     actions.add( "delegate" );
                     actions.add( "drop" );
                     actions.add( "delete" );
                  } else if (aCase.isStatus( States.DONE ))
                  {
                     actions.add( "complete" );
                     actions.add( "redo" );
                  }


               } else if (aCase.isDelegatedTo( actor ))
               {
                  // Delegations
                  if (aCase.isStatus( States.DELEGATED ))
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
