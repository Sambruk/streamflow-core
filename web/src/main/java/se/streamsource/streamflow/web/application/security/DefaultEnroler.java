/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.application.security;

import org.restlet.data.ClientInfo;
import org.restlet.security.Enroler;
import org.restlet.security.User;

/**
 * Accept login if user with the given username has the given password
 * in the Streamflow user database.
 */
public class DefaultEnroler
      implements Enroler
{
   public void enrole( ClientInfo clientInfo )
   {
      User user = clientInfo.getUser();
      if (user != null)
      {
         clientInfo.getPrincipals().add( new UserPrincipal( user.getIdentifier() ) );


      }
   }
}