/*
 * Copyright (c) 2009, Rickard �berg. All Rights Reserved.
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

package se.streamsource.streamflow.web.resource.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.organization.MergeOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.MoveOrganizationalUnitException;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to /organizations/{organization}/organizationalunits/{ou}
 */
public class OrganizationalUnitServerResource
      extends CommandQueryServerResource
{
   @Structure
   ValueBuilderFactory vbf;

   public void changedescription( StringDTO stringValue )
   {
      String orgId = (String) getRequest().getAttributes().get( "ou" );
      Describable describable = uowf.currentUnitOfWork().get( Describable.class, orgId );

      checkPermission( describable );
      describable.changeDescription( stringValue.string().get() );
   }

   @Override
   protected Representation get( Variant variant ) throws ResourceException
   {
      if (getRequest().getResourceRef().hasQuery())
      {
         return super.get( variant );
      }
      return getHtml( "resources/ou.html" );
   }

   public void move( EntityReferenceDTO moveValue ) throws ResourceException
   {
      String ouId = (String) getRequest().getAttributes().get( "ou" );
      OrganizationalUnitEntity ou = uowf.currentUnitOfWork().get( OrganizationalUnitEntity.class, ouId );
      OrganizationalUnits toEntity = uowf.currentUnitOfWork().get( OrganizationalUnits.class, moveValue.entity().get().identity() );

      checkPermission( ou );

      try
      {
         ou.moveOrganizationalUnit( toEntity );
      } catch (MoveOrganizationalUnitException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
      }
   }

   public void merge( EntityReferenceDTO moveValue ) throws ResourceException
   {
      String ouId = (String) getRequest().getAttributes().get( "ou" );
      OrganizationalUnitEntity ou = uowf.currentUnitOfWork().get( OrganizationalUnitEntity.class, ouId );
      OrganizationalUnitRefactoring toEntity = uowf.currentUnitOfWork().get( OrganizationalUnitRefactoring.class, moveValue.entity().get().identity() );

      checkPermission( ou );

      try
      {
         ou.mergeOrganizationalUnit( toEntity );
      } catch (MergeOrganizationalUnitException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
      }
   }
}