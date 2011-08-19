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

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

/**
 * JAVADOC
 */
public class UserContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      return new LinksBuilder( module.valueBuilderFactory() ).
            addLink( "Drafts", "drafts", "drafts", "workspace/user/drafts/cases", null ).
            addLink( "Inbox", "inbox", "inbox", "workspace/user/inbox/cases", null ).
            addLink( "Assignments", "assignments", "assignments", "workspace/user/assignments/cases", null ).
            newLinks();
   }

   public void resetpassword( @Name("password") String password )
   {
      UserAuthentication user = RoleMap.role( UserAuthentication.class );

      user.resetPassword( password );
   }

   public void changedisabled()
   {
      UserAuthentication user = RoleMap.role( UserAuthentication.class );
      UserAuthentication.Data userData = RoleMap.role( UserAuthentication.Data.class );

      user.changeEnabled( userData.disabled().get() );
   }
}
