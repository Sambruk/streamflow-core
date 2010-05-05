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

package se.streamsource.streamflow.web.domain.entity.caze;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Project;

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
         switch (aCase.status().get())
         {
            case DRAFT:
            {
               if (aCase.createdBy().get().equals(actor))
               {
                   if (aCase.owner().get() != null)
                     actions.add( "open" );
                   else
                     actions.add( "sendto" );

                  actions.add("delete");
               }

               return;
            }

            case OPEN:
            {
               // Project owned aCase
               Project project = (Project) aCase.owner().get();
               if (((Member) actor).isMember( project ))
               {
                  if (aCase.isAssigned())
                  {
                     actions.add( "onhold" );
                     actions.add( "unassign" );
                  } else
                  {
                     actions.add( "assign" );
                     actions.add(  "sendto" );
                  }

                  CaseType caseType = aCase.caseType().get();
                  actions.add( "close" );
                  actions.add( "delete" );
               }

               return;
            }

            case CLOSED:
            {
               Project project = (Project) aCase.owner().get();
               if (((Member) actor).isMember( project ))
               {
                  actions.add( "reopen" );
               }

               return;
            }

            case ON_HOLD:
            {
               Project project = (Project) aCase.owner().get();
               if (((Member) actor).isMember( project ))
               {
                  actions.add( "resume" );
               }

               return;
            }
         }
      }
   }
}
