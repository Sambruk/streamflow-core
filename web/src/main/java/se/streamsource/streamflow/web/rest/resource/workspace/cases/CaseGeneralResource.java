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
package se.streamsource.streamflow.web.rest.resource.workspace.cases;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.workspace.cases.general.CaseGeneralCommandsContext;
import se.streamsource.streamflow.web.context.workspace.cases.general.CaseGeneralContext;
import se.streamsource.streamflow.web.rest.resource.organizations.LabelableResource;

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
