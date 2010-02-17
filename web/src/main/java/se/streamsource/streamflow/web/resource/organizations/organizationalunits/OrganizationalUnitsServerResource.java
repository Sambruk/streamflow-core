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

package se.streamsource.streamflow.web.resource.organizations.organizationalunits;

import org.qi4j.api.entity.EntityReference;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.organization.OpenProjectExistsException;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organizationalunits}/organizationalunits
 * /organizations/{organization}/organizationalunits/{organizationalunits}/organizationalunits
 */
public class OrganizationalUnitsServerResource
      extends CommandQueryServerResource
{
   public ListValue organizationalunits()
   {
      String identity = getRequest().getAttributes().get( "organizationalunits" ).toString();
      OrganizationalUnits.Data ous = uowf.currentUnitOfWork().get( OrganizationalUnits.Data.class, identity );

      checkPermission( ous );

      ListValueBuilder builder = new ListValueBuilder( vbf );
      for (OrganizationalUnitRefactoring ou : ous.organizationalUnits())
      {
         Describable describable = (Describable) ou;
         builder.addListItem( describable.getDescription(), EntityReference.getEntityReference( ou ) );
      }
      return builder.newList();
   }

   public void createorganizationalunit( StringDTO value ) throws ResourceException
   {
      String organization = getRequestAttributes().get( "organizationalunits" ).toString();
      OrganizationalUnits ous = uowf.currentUnitOfWork().get( OrganizationalUnits.class, organization );

      checkPermission( ous );

      ous.createOrganizationalUnit( value.string().get() );
   }

   public void removeorganizationalunit( EntityReferenceDTO entity ) throws ResourceException
   {
      String organization = getRequest().getAttributes().get( "organizationalunits" ).toString();
      OrganizationalUnits ous = uowf.currentUnitOfWork().get( OrganizationalUnits.class, organization );
      OrganizationalUnitRefactoring ou = uowf.currentUnitOfWork().get( OrganizationalUnitRefactoring.class, entity.entity().get().identity() );

      try
      {
         checkPermission( ous );

         ou.deleteOrganizationalUnit();

      } catch (OpenProjectExistsException pe)
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, pe.getMessage() );
      }
   }
}
