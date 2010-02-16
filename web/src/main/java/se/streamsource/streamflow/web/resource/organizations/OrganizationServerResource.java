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
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationParticipationsQueries;
import se.streamsource.streamflow.web.domain.entity.tasktype.TaskTypesQueries;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to /organizations/{organization}
 */
public class OrganizationServerResource
      extends CommandQueryServerResource
{
   @Structure
   ValueBuilderFactory vbf;

   public void changedescription( StringDTO stringValue )
   {
      String orgId = (String) getRequest().getAttributes().get( "organization" );
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
      return getHtml( "resources/organization.html" );
   }

/*   public ListValue findusers( StringDTO query )
   {
      String orgId = getRequest().getAttributes().get( "organization" ).toString();

      OwningOrganization ouq = uowf.currentUnitOfWork().get( OwningOrganization.class, orgId );
      checkPermission( ouq );

      return ((OrganizationQueries) ouq.organization().get()).findUsers( query.string().get() );
   }


   public ListValue findgroups( StringDTO query )
   {
      String orgId = getRequest().getAttributes().get( "organization" ).toString();

      OwningOrganization ouq = uowf.currentUnitOfWork().get( OwningOrganization.class, orgId );
      checkPermission( ouq );

      return ((OrganizationQueries) ouq.organization().get()).findGroups( query.string().get() );
   }

   public ListValue findprojects( StringDTO query )
   {
      String orgId = getRequest().getAttributes().get( "organization" ).toString();

      OwningOrganization ouq = uowf.currentUnitOfWork().get( OwningOrganization.class, orgId );
      checkPermission( ouq );

      return ((OrganizationQueries) ouq.organization().get()).findProjects( query.string().get() );
   }

   public ListValue formdefinitions()
   {
      String ouId = (String) getRequest().getAttributes().get( "organization" );

      OwningOrganization ou = uowf.currentUnitOfWork().get( OwningOrganization.class, ouId );

      FormQueries forms = (FormQueries) ou.organization().get();

      return forms.getForms();
   }

   public ListValue participatingusers()
   {
      String orgId = (String) getRequest().getAttributes().get( "organization" );

      OrganizationParticipationsQueries participants = uowf.currentUnitOfWork().get( OrganizationParticipationsQueries.class, orgId );

      checkPermission( participants );

      return participants.participatingUsers();
   }

   public ListValue nonparticipatingusers()
   {
      String orgId = (String) getRequest().getAttributes().get( "organization" );

      OrganizationParticipationsQueries participants = uowf.currentUnitOfWork().get( OrganizationParticipationsQueries.class, orgId );

      checkPermission( participants );

      return participants.nonParticipatingUsers();
   }

   public ListValue tasktypes()
   {
      String orgId = (String) getRequest().getAttributes().get( "organization" );

      TaskTypesQueries taskTypes = uowf.currentUnitOfWork().get( TaskTypesQueries.class, orgId );

      checkPermission( taskTypes );

      return taskTypes.taskTypeList();
   }

   public void join( ListValue users )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String id = (String) getRequest().getAttributes().get( "organization" );
      Organization org = uowf.currentUnitOfWork().get( Organization.class, id );

      checkPermission( org );

      for (ListItemValue value : users.items().get())
      {
         OrganizationParticipations user = uow.get( OrganizationParticipations.class, value.entity().get().identity() );
         user.join( org );
      }
   }

   public void leave( ListValue users )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String id = (String) getRequest().getAttributes().get( "organization" );
      Organization org = uowf.currentUnitOfWork().get( Organization.class, id );

      checkPermission( org );

      for (ListItemValue value : users.items().get())
      {
         OrganizationParticipations uop = uow.get( OrganizationParticipations.class, value.entity().get().identity() );
         uop.leave( org );
      }
   }
*/

}