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
package se.streamsource.streamflow.web.rest.resource.surface.administration.organizations.accesspoints;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.administration.surface.accesspoints.ReplacementSelectionFieldContext;
import se.streamsource.streamflow.web.context.administration.surface.accesspoints.ReplacementSelectionFieldsContext;
import se.streamsource.streamflow.web.domain.structure.form.Field;

/**
 *
 */
public class ReplacementSelectionFieldsResource
        extends CommandQueryResource
    implements SubResources{

    @Structure
    Module module;

    public ReplacementSelectionFieldsResource()
    {
        super( ReplacementSelectionFieldsContext.class );
    }

    public void resource(String segment) throws ResourceException {

        RoleMap.current().set( module.unitOfWorkFactory().currentUnitOfWork().get( Field.class, segment ));
        subResourceContexts( ReplacementSelectionFieldContext.class );
    }
}
