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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;

/**
 * JAVADOC
 */
@Mixins(GroupContext.Mixin.class)
public interface GroupContext
   extends DeleteInteraction, DescribableContext, Interactions
{
   @SubContext
   ParticipantsContext participants();

   abstract class Mixin
      extends InteractionsMixin
      implements GroupContext
   {
      public void delete()
      {
         Groups groups = context.get(Groups.class);
         Group group = context.get(Group.class);
         groups.removeGroup( group );
      }

      public ParticipantsContext participants()
      {
         return subContext( ParticipantsContext.class );
      }
   }
}
