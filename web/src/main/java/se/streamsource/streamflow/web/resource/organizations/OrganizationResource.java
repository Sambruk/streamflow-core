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

package se.streamsource.streamflow.web.resource.organizations;

import se.streamsource.dci.api.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.api.*;
import se.streamsource.streamflow.web.application.mail.*;
import se.streamsource.streamflow.web.context.*;
import se.streamsource.streamflow.web.context.administration.*;
import se.streamsource.streamflow.web.context.structure.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;
import se.streamsource.streamflow.web.resource.administration.surface.*;
import se.streamsource.streamflow.web.resource.organizations.forms.*;
import se.streamsource.streamflow.web.resource.surface.administration.organizations.accesspoints.*;
import se.streamsource.streamflow.web.resource.surface.administration.organizations.emailaccesspoints.*;

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
}
