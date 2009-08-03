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
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.ui.DetailView;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;

import javax.swing.JComponent;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;

/**
 * JAVADOC
 */
public class AccountAdministrationNode
        implements DetailView, TreeNode, Refreshable
{
    @Structure
    ObjectBuilderFactory obf;

    @Service
    Restlet client;

    @Uses
    AccountModel accountModel;

    WeakModelMap<TreeNodeValue, OrganizationalStructureAdministrationNode> models = new WeakModelMap<TreeNodeValue, OrganizationalStructureAdministrationNode>()
    {
        protected OrganizationalStructureAdministrationNode newModel(TreeNodeValue key)
        {
            return obf.newObjectBuilder(OrganizationalStructureAdministrationNode.class).use(AccountAdministrationNode.this, resource, key).newInstance();
        }
    };

    public OrganizationsClientResource resource;
    private TreeNode parent;
    private Account account;
    public TreeValue organizations;

    public AccountAdministrationNode(@Uses TreeNode parent, @Uses Account account,
                                     @Service Restlet client) throws ResourceException
    {
        this.parent = parent;
        this.account = account;

        resource = account.server(client).organizations();
    }

    public TreeNode getParent()
    {
        return parent;
    }

    public int getIndex(TreeNode node)
    {
        if (organizations == null)
            return -1;

        for (int idx = 0; idx < organizations.roots().get().size(); idx++)
        {
            TreeNodeValue treeNodeValue = organizations.roots().get().get(idx);
            OrganizationalStructureAdministrationNode child = models.get(treeNodeValue);
            if (child.equals(node))
                return idx;
        }

        return -1;
    }

    public Enumeration children()
    {
        return Collections.enumeration(Collections.emptyList());
    }

    public TreeNode getChildAt(int index)
    {
        if (organizations == null)
            return null;

        TreeNodeValue treeNode = organizations.roots().get().get(index);
        return models.get(treeNode);
    }

    public int getChildCount()
    {
        return organizations == null ? 0 : organizations.roots().get().size();
    }

    public boolean getAllowsChildren()
    {
        return true;
    }

    public boolean isLeaf()
    {
        return false;
    }

    public JComponent detailView()
    {
        return obf.newObjectBuilder(AccountView.class).use(accountModel).newInstance();
    }

    public void refresh() throws Exception
    {
        UserClientResource user = account.user(client);
        organizations = user.administration().organizations();
    }
}
