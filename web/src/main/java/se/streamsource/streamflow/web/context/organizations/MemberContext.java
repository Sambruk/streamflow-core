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
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.dci.api.Interactions;

/**
 * JAVADOC
 */
@Mixins(MemberContext.Mixin.class)
public interface MemberContext
   extends DeleteInteraction, Interactions
{
   abstract class Mixin
      extends InteractionsMixin
      implements MemberContext
   {
      public void delete()
      {
         Member member = context.get(Member.class);
         Members members = context.get( Members.class );
         members.removeMember( member );
      }

   }
}
