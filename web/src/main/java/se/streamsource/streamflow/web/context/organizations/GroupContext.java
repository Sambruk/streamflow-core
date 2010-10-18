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
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;

/**
 * JAVADOC
 */
@Mixins(GroupContext.Mixin.class)
public interface GroupContext
   extends DeleteContext, DescribableContext, Context
{
   @SubContext
   ParticipantsContext participants();

   abstract class Mixin
      extends ContextMixin
      implements GroupContext
   {
      public void delete()
      {
         Groups groups = roleMap.get(Groups.class);
         Group group = roleMap.get(Group.class);
         groups.removeGroup( group );
      }

      public ParticipantsContext participants()
      {
         return subContext( ParticipantsContext.class );
      }
   }
}
