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
package se.streamsource.streamflow.web.rest.resource.surface.endusers;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.surface.endusers.OpenCaseContext;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.HasConversation;
import se.streamsource.streamflow.web.rest.resource.surface.endusers.conversation.MyCasesConversationsResource;
import se.streamsource.streamflow.web.rest.resource.surface.endusers.submittedforms.MyPagesSubmittedFormsResource;

/**
 * Resource for an open case
 */
public class OpenCaseResource
        extends CommandQueryResource
{
   public OpenCaseResource()
   {
      super(OpenCaseContext.class);
   }
   
   @SubResource
   public void submittedforms( )
   {
      subResource( MyPagesSubmittedFormsResource.class );
   }
   
   @SubResource
   @HasConversation(true)
   public void conversations()
   {
      subResource( MyCasesConversationsResource.class );
   }
}
