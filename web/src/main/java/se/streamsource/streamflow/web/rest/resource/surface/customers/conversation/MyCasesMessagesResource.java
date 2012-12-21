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
package se.streamsource.streamflow.web.rest.resource.surface.customers.conversation;

import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.surface.customers.conversation.MyCasesMessagesContext;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 *
 *
 */
public class MyCasesMessagesResource extends CommandQueryResource implements SubResources
{
   public MyCasesMessagesResource()
   {
      super( MyCasesMessagesContext.class );
   }

   public void resource(String segment) throws ContextNotFoundException
   {

      findManyAssociation( RoleMap.role( Messages.Data.class ).messages(), segment );
      subResource( MyCasesMessageResource.class );
   }
}