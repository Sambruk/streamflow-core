/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context.administration;

import se.streamsource.dci.api.*;
import se.streamsource.streamflow.web.domain.structure.group.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.role.Role;
import se.streamsource.streamflow.web.domain.structure.role.*;

/**
 * JAVADOC
 */
public class AdministratorContext
   implements DeleteContext
{
   public void delete()
   {
      RolePolicy role = RoleMap.role(RolePolicy.class);

      Participant participant = RoleMap.role(Participant.class);

      Roles roles = RoleMap.role(Roles.class);
      Role adminRole = roles.getAdministratorRole();

      role.revokeRole( participant, adminRole );
   }
}