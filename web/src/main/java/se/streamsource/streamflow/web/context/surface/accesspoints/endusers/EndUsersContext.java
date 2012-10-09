/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.context.surface.accesspoints.endusers;

import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.domain.structure.user.EndUser;
import se.streamsource.streamflow.web.domain.structure.user.EndUsers;

import java.util.UUID;

/**
 * JAVADOC
 */
public class EndUsersContext
   implements CreateContext<StringValue, EndUser>
{
   public EndUser create(StringValue userId)
   {
      EndUsers endUsers = RoleMap.role( EndUsers.class );
      EndUser user = endUsers.createEndUser(userId.string().get());
      String description = "Anonymous";

      try
      {
         String id = userId.string().get();
         if( id.indexOf( "-" ) != -1 )
         {
            // remove last count part per mixin type and check if it is a valid uuid
            // see UuidIdentityGeneratorMixin.generate( Class )
            UUID.fromString( id.substring( 0, id.lastIndexOf( "-" ) ) );
            description = "Webform";
         }
      } catch( IllegalArgumentException iae )
      {
         // do nothing - id is not an UUID use Anonymous instead
      }

      user.changeDescription( description );
      return user;
   }
}