/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.entity.organization;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.ExternalReference;
import se.streamsource.streamflow.web.domain.entity.user.ProjectQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.group.Participation;

/**
 * JAVADOC
 */
public interface GroupEntity
      extends Group,

      ProjectQueries,

      Describable.Data,
      Ownable.Data,
      Participation.Data,
      Participants.Data,
      Removable.Data,
      ExternalReference.Data,
      DomainEntity
{
}
