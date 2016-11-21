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
package se.streamsource.streamflow.web.rest.resource.organizations;

import static se.streamsource.dci.api.RoleMap.role;

import org.restlet.resource.ResourceException;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.administration.ParticipantContext;
import se.streamsource.streamflow.web.context.administration.ParticipantsContext;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Participants;

/**
 * JAVADOC
 */
public class ParticipantsResource
   extends CommandQueryResource
   implements SubResources
{
   public ParticipantsResource()
   {
      super( ParticipantsContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      RoleMap.current().map( Group.class, Participants.class );
      findManyAssociation( role(Participants.Data.class).participants(), segment );
      subResourceContexts( ParticipantContext.class );
   }
}
