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

package se.streamsource.streamflow.web.rest.resource.organizations;

import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.application.mail.CreateCaseFromEmailService;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.administration.OrganizationalUnitsContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.rest.resource.administration.surface.OrganizationAttachmentsResource;
import se.streamsource.streamflow.web.rest.resource.administration.surface.ProxyUsersResource;
import se.streamsource.streamflow.web.rest.resource.organizations.forms.DataTypeDefinitionsResource;
import se.streamsource.streamflow.web.rest.resource.organizations.forms.FormsResource;
import se.streamsource.streamflow.web.rest.resource.surface.administration.organizations.accesspoints.AccessPointsAdministrationResource;
import se.streamsource.streamflow.web.rest.resource.surface.administration.organizations.emailaccesspoints.EmailAccessPointsAdministrationResource;

/**
 * JAVADOC
 */
@RequiresPermission(PermissionType.administrator)
public class OrganizationResource
      extends CommandQueryResource
{
   public OrganizationResource()
   {
      super( OrganizationalUnitsContext.class, DescribableContext.class );
   }
   
   @SubResource
   public void administrators( )
   {
      subResource( AdministratorsResource.class );
   }

   @SubResource
   public void labels()
   {
      subResource( LabelsResource.class );
   }

   @SubResource
   public void selectedlabels()
   {
      subResource( SelectedLabelsResource.class );
   }


   @SubResource
   public void organizationusers()
   {
      subResource( OrganizationUsersResource.class );
   }

   @SubResource
   public void roles()
   {
      subResource( RolesResource.class );
   }

   @SubResource
   public void forms()
   {
      subResource( FormsResource.class );
   }

   @SubResource
   public void casetypes()
   {
      subResource( CaseTypesResource.class );
   }

   @SubResource 
   public void accesspoints()
   {
      subResource( AccessPointsAdministrationResource.class );
   }

   @SubResource @ServiceAvailable(CreateCaseFromEmailService.class)
   public void emailaccesspoints()
   {
      subResource(EmailAccessPointsAdministrationResource.class);
   }

   @SubResource
   public void proxyusers()
   {
      subResource( ProxyUsersResource.class );
   }

   @SubResource
   public void attachments()
   {
      subResource( OrganizationAttachmentsResource.class );
   }
   
   @SubResource
   public void templates()
   {
      subResource( SelectedTemplatesResource.class );
   }
   
   @SubResource
   public void datatypedefinitions()
   {
      subResource( DataTypeDefinitionsResource.class );
   }
}
