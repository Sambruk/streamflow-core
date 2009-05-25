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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.organizations.OrganizationClientResource;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.domain.roles.DetailView;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class OrganizationalStructureAdministrationNode
        extends DefaultMutableTreeNode
        implements DetailView
{
    @Structure
    ObjectBuilderFactory obf;

    @Service
    OrganizationalUnitAdministrationModel model;

    @Service
    OrganizationalUnitAdministrationView view;

    OrganizationClientResource resource;

    public OrganizationalStructureAdministrationNode(@Uses TreeNodeValue ou, @Uses OrganizationsClientResource orgResource, @Structure ObjectBuilderFactory obf) throws ResourceException
    {
        super(ou);

        resource = orgResource.organization(ou.entity().get().identity());

        for (TreeNodeValue treeNodeValue : ou.children().get())
        {
            add(obf.newObjectBuilder(OrganizationalStructureAdministrationNode.class).use(treeNodeValue, orgResource).newInstance());
        }
    }

    @Override
    public String toString()
    {
        return ou().description().get();
    }

    public TreeNodeValue ou()
    {
        return (TreeNodeValue) getUserObject();
    }

    public JComponent detailView()
    {
        try
        {
            model.setOrganization(resource);
            return view;
        } catch (ResourceException e)
        {
            return new JLabel(e.getMessage());
        }
    }
}