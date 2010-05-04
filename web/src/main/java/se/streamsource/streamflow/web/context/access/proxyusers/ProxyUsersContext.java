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

package se.streamsource.streamflow.web.context.access.proxyusers;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.resource.user.NewProxyUserCommand;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;

/**
 * JAVADOC
 */
@Mixins(ProxyUsersContext.Mixin.class)
public interface ProxyUsersContext
   extends Interactions
{
   // commands
   void createproxyuser( NewProxyUserCommand proxyUser );

   abstract class Mixin
      extends InteractionsMixin
      implements ProxyUsersContext
   {
      public void createproxyuser( NewProxyUserCommand proxyUser )
      {
         Organization organization = context.get( Organization.class );
         organization.createProxyUser( proxyUser.name().get(), proxyUser.username().get(), proxyUser.password().get() );
      }
   }
}