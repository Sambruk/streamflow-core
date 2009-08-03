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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.OrganizationClientResource;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.client.ui.DetailView;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * JAVADOC
 */
public class OrganizationalStructureAdministrationNode
        extends DefaultMutableTreeNode
        implements DetailView
{
    @Structure
    ObjectBuilderFactory obf;

    WeakModelMap<TreeNodeValue, OrganizationalStructureAdministrationNode> models = new WeakModelMap<TreeNodeValue, OrganizationalStructureAdministrationNode>()
    {
        @Override
        protected OrganizationalStructureAdministrationNode newModel(TreeNodeValue key)
        {
            return obf.newObjectBuilder(OrganizationalStructureAdministrationNode.class).use(OrganizationalStructureAdministrationNode.this, key, orgResource).newInstance();
        }
    };

    OrganizationalUnitAdministrationModel model;

    OrganizationsClientResource orgResource;

    public OrganizationalStructureAdministrationNode(@Uses TreeNode parent, @Uses TreeNodeValue ou, @Uses OrganizationsClientResource orgResource, @Structure ObjectBuilderFactory obf) throws ResourceException
    {
        super(ou);
        this.orgResource = orgResource;

        OrganizationClientResource resource = orgResource.organization(ou.entity().get().identity());
        model = obf.newObjectBuilder(OrganizationalUnitAdministrationModel.class).use(resource).newInstance();

        for (TreeNodeValue treeNodeValue : ou.children().get())
        {
            add(obf.newObjectBuilder(OrganizationalStructureAdministrationNode.class).use(this, treeNodeValue, orgResource).newInstance());
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
        return obf.newObjectBuilder(OrganizationalUnitAdministrationView.class).use(model).newInstance();
    }
}