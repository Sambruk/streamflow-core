/**
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 * <p>
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/agpl.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.rest.resource.workspace.cases;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.workspace.cases.SubCasesContext;


public class SubCasesResource
        extends CommandQueryResource implements SubResources {

    public SubCasesResource() {
        super(SubCasesContext.class);
    }

    public void resource(String segment) throws ResourceException {
        System.out.println("asked for resource");
//      findList(RoleMap.role( Contacts.Data.class ).contacts().get(), segment);
//      subResourceContexts( ContactContext.class );
//
//      findList( ((CaseLog.Data) RoleMap.role( CaseLoggable.Data.class ).caselog().get()).entries().get(), segment );
//      subResourceContexts( CaseLogEntryContext.class );
    }
}
