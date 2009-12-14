/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource.organizations.groups;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.group.Groups;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/organizationalunits/{ou}/groups
 */
public class GroupsServerResource
      extends CommandQueryServerResource
{

   public ListValue groups()
   {
      String identity = getRequest().getAttributes().get( "ou" ).toString();
      Groups.Data groups = uowf.currentUnitOfWork().get( Groups.Data.class, identity );

      return new ListValueBuilder( vbf ).addDescribableItems( groups.groups() ).newList();
   }

   public void createGroup( StringDTO name ) throws ResourceException
   {
      String identity = getRequest().getAttributes().get( "ou" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      Groups groups = uow.get( Groups.class, identity );

      checkPermission( groups );
      groups.createGroup( name.string().get() );
   }
}
