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
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.organization.MergeOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.MoveOrganizationalUnitException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.form.FormQueries;
import se.streamsource.streamflow.web.domain.organization.*;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to /organizations/{organization}
 */
public class OrganizationServerResource
        extends CommandQueryServerResource
{
    @Structure
    ValueBuilderFactory vbf;

    public void describe(StringDTO stringValue)
    {
        String orgId = (String) getRequest().getAttributes().get("organization");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, orgId);

        checkPermission(describable);
        describable.changeDescription(stringValue.string().get());
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        if (getRequest().getResourceRef().hasQuery())
        {
            return super.get(variant);
        }
        return getHtml("resources/organization.html");
    }

    public ListValue findUsers(StringDTO query)
    {
        String orgId = getRequest().getAttributes().get("organization").toString();

        OrganizationalUnitRefactoring.Data ouq  = uowf.currentUnitOfWork().get(OrganizationalUnitRefactoring.Data.class, orgId);
        checkPermission(ouq);

        return ((OrganizationQueries)ouq.organization().get()).findUsers(query.string().get());
    }


    public ListValue findGroups(StringDTO query)
    {
        String orgId = getRequest().getAttributes().get("organization").toString();

        OrganizationalUnitRefactoring.Data ouq  = uowf.currentUnitOfWork().get(OrganizationalUnitRefactoring.Data.class, orgId);
        checkPermission(ouq);

        return ((OrganizationQueries)ouq.organization().get()).findGroups(query.string().get());
    }

    public ListValue findProjects(StringDTO query)
    {
        String orgId = getRequest().getAttributes().get("organization").toString();

        OrganizationalUnitRefactoring.Data ouq  = uowf.currentUnitOfWork().get(OrganizationalUnitRefactoring.Data.class, orgId);
        checkPermission(ouq);

        return ((OrganizationQueries)ouq.organization().get()).findProjects(query.string().get());
    }

    public void move(EntityReferenceDTO moveValue) throws ResourceException
    {
        String ouId = (String) getRequest().getAttributes().get("organization");
        OrganizationalUnitEntity ou = uowf.currentUnitOfWork().get(OrganizationalUnitEntity.class, ouId);
        OrganizationalUnits toEntity = uowf.currentUnitOfWork().get(OrganizationalUnits.class, moveValue.entity().get().identity());

        checkPermission(ou);

        try
        {
            ou.moveOrganizationalUnit(toEntity);
        } catch (MoveOrganizationalUnitException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
        }
    }

    public void merge(EntityReferenceDTO moveValue) throws ResourceException
    {
        String ouId = (String) getRequest().getAttributes().get("organization");
        OrganizationalUnitEntity ou = uowf.currentUnitOfWork().get(OrganizationalUnitEntity.class, ouId);
        OrganizationalUnitRefactoring toEntity = uowf.currentUnitOfWork().get( OrganizationalUnitRefactoring.class, moveValue.entity().get().identity());

        checkPermission(ou);

        try
        {
            ou.mergeOrganizationalUnit(toEntity);
        } catch (MergeOrganizationalUnitException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
        }
    }


    public ListValue formDefinitions()
    {
        String ouId = (String) getRequest().getAttributes().get("organization");

        OrganizationalUnitRefactoring.Data ou = uowf.currentUnitOfWork().get( OrganizationalUnitRefactoring.Data.class, ouId);

        FormQueries forms = (FormQueries) ou.organization().get();

        return forms.getForms();
    }

    public ListValue participatingUsers()
    {
        String orgId = (String) getRequest().getAttributes().get("organization");

        OrganizationParticipationsQueries participants = uowf.currentUnitOfWork().get(OrganizationParticipationsQueries.class, orgId);

        checkPermission(participants);

        return participants.participatingUsers();
    }

    public ListValue nonParticipatingUsers()
    {
         String orgId = (String) getRequest().getAttributes().get("organization");

        OrganizationParticipationsQueries participants = uowf.currentUnitOfWork().get(OrganizationParticipationsQueries.class, orgId);

        checkPermission(participants);

        return participants.nonParticipatingUsers();       
    }

    public void join(ListValue users)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String id = (String) getRequest().getAttributes().get("organization");
        Organization org = uowf.currentUnitOfWork().get(Organization.class, id);

        checkPermission(org);

        for(ListItemValue value : users.items().get())
        {
            OrganizationParticipations user = uow.get(OrganizationParticipations.class, value.entity().get().identity());
            user.join(org);
        }
    }

    public void leave(ListValue users)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String id = (String) getRequest().getAttributes().get("organization");
        Organization org = uowf.currentUnitOfWork().get(Organization.class, id);

        checkPermission(org);

        for(ListItemValue value : users.items().get())
        {
            OrganizationParticipations uop = uow.get(OrganizationParticipations.class, value.entity().get().identity());
            uop.leave(org);
        }
    }


}