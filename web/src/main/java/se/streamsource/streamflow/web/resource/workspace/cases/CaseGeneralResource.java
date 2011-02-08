/*
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

package se.streamsource.streamflow.web.resource.workspace.cases;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.workspace.cases.general.CaseGeneralCommandsContext;
import se.streamsource.streamflow.web.context.workspace.cases.general.CaseGeneralContext;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.resource.organizations.LabelableResource;
import se.streamsource.streamflow.web.resource.workspace.cases.conversation.ConversationResource;

/**
 * JAVADOC
 */
public class CaseGeneralResource
      extends CommandQueryResource
{
   public CaseGeneralResource()
   {
      super( CaseGeneralContext.class, CaseGeneralCommandsContext.class );
   }

   @SubResource
   public void labels(  )
   {
      subResource( LabelableResource.class );
   }

}
