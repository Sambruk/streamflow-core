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

package se.streamsource.streamflow.web.context.users.workspace;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.web.context.gtd.AssignmentsContext;
import se.streamsource.streamflow.web.context.gtd.InboxContext;

/**
 * JAVADOC
 */
@Mixins(WorkspaceProjectContext.Mixin.class)
public interface WorkspaceProjectContext
   extends Interactions
{
   @SubContext
   InboxContext inbox();

   @SubContext
   AssignmentsContext assignments();

   abstract class Mixin
      extends InteractionsMixin
      implements WorkspaceProjectContext
   {
      public InboxContext inbox()
      {
         return subContext( InboxContext.class );
      }

      public AssignmentsContext assignments()
      {
         return subContext( AssignmentsContext.class );
      }
   }
}