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

package se.streamsource.streamflow.web.resource.organizations;

import org.restlet.resource.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.api.*;
import se.streamsource.streamflow.web.context.administration.*;
import se.streamsource.streamflow.web.context.structure.*;

/**
 * JAVADOC
 */
public class GroupResource
   extends CommandQueryResource
{
   public GroupResource()
   {
      super( GroupContext.class, DescribableContext.class );
   }

   @SubResource
   public void participants(  ) throws ResourceException
   {
      subResource( ParticipantsResource.class );
   }
}
