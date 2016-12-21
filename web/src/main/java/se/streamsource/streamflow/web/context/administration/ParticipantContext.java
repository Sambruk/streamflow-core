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
package se.streamsource.streamflow.web.context.administration;

import static se.streamsource.dci.api.RoleMap.role;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImporterService;

/**
 * JAVADOC
 */
public class ParticipantContext
   implements DeleteContext
{
   @ServiceAvailable( service = LdapImporterService.class, availability = false )
   public void delete()
   {
      Participant participant = role( Participant.class );
      Participants participants = role( Participants.class);
      participants.removeParticipant( participant );
   }
}
