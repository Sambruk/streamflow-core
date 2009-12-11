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

package se.streamsource.streamflow.client.ui.administration;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.organizations.OrganizationClientResource;
import se.streamsource.streamflow.client.ui.administration.form.FormDefinitionsModel;
import se.streamsource.streamflow.client.ui.administration.label.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.roles.RolesModel;
import se.streamsource.streamflow.client.ui.administration.tasktypes.TaskTypesModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * JAVADOC
 */
public class OrganizationAdministrationModel
    implements EventListener
{
    @Structure
    ValueBuilderFactory vbf;

    private AdministratorsModel administratorsModel;
    private RolesModel rolesModel;
    private LabelsModel labelsModel;
    private FormDefinitionsModel formsModel;
    private TaskTypesModel taskTypesModel;

    private OrganizationClientResource resource;

    public OrganizationAdministrationModel( @Structure ObjectBuilderFactory obf, @Uses OrganizationClientResource organization )
            throws ResourceException
    {
        resource = organization;

        rolesModel = obf.newObjectBuilder(RolesModel.class).use(organization.roles()).newInstance();
        labelsModel = obf.newObjectBuilder(LabelsModel.class).use(organization.getNext(), organization.labels()).newInstance();
        taskTypesModel = obf.newObjectBuilder(TaskTypesModel.class).use(organization.getNext(), organization.taskTypes()).newInstance();
        formsModel = obf.newObjectBuilder(FormDefinitionsModel.class).use(organization.forms()).newInstance();
        administratorsModel = obf.newObjectBuilder(AdministratorsModel.class).use(organization.administrators()).newInstance();
    }

    public RolesModel rolesModel()
    {
        return rolesModel;
    }

    public LabelsModel labelsModel()
    {
        return labelsModel;
    }

    public TaskTypesModel taskTypesModel()
    {
        return taskTypesModel;
    }

    public FormDefinitionsModel formsModel()
    {
        return formsModel;
    }

    public AdministratorsModel administratorsModel()
    {
        return administratorsModel;
    }

        public void changeDescription( String newDescription )
    {
        try
        {
            ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
            builder.prototype().string().set( newDescription );
            resource.changeDescription( builder.newInstance() );
        } catch (ResourceException e)
        {
            throw new OperationException( AdministrationResources.could_not_rename_organization, e );
        }
    }

    public void createOrganizationalUnit( String name )
    {
        try
        {
            ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
            builder.prototype().string().set( name );
            resource.organizationalUnits().createOrganizationalUnit( builder.newInstance() );
        } catch (ResourceException e)
        {
            throw new OperationException( AdministrationResources.could_not_create_new_organization, e );
        }
    }

    public void removeOrganizationalUnit( EntityReference id )
    {
        try
        {
            ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
            builder.prototype().entity().set( id );
            resource.organizationalUnits().removeOrganizationalUnit( builder.newInstance() );
        } catch (ResourceException e)
        {
            if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
            {
                throw new OperationException( AdministrationResources.could_not_remove_organisation_with_open_projects, e );

            } else
            {
                throw new OperationException( AdministrationResources.could_not_remove_organization, e );
            }

        }
    }

    public void notifyEvent( DomainEvent event )
    {
        rolesModel.notifyEvent(event);
        labelsModel.notifyEvent( event );
        formsModel.notifyEvent(event);
        administratorsModel.notifyEvent( event );
    }
}
