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
package se.streamsource.streamflow.web.rest.resource.organizations;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.administration.CasePriorityDefinitionContext;
import se.streamsource.streamflow.web.context.administration.CasePriorityDefinitionsContext;
import se.streamsource.streamflow.web.domain.structure.organization.CasePriorityDefinitions;

/**
 *Case priorities resource.
 */
public class CasePriorityDefinitionsResource
   extends CommandQueryResource
   implements SubResources
{
   public CasePriorityDefinitionsResource()
   {
      super( CasePriorityDefinitionsContext.class );
   }
   public void resource( String segment ) throws ResourceException
   {
      findList( RoleMap.role( CasePriorityDefinitions.Data.class ).prioritys().get() , segment );
      subResourceContexts( CasePriorityDefinitionContext.class );
   }
}
