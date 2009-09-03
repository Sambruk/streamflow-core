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
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class AccountAdministrationNode
        implements TreeNode, Refreshable, Observer
{
    @Structure
    private ObjectBuilderFactory obf;

    private AccountModel accountModel;

    WeakModelMap<TreeNodeValue, OrganizationalStructureAdministrationNode> models = new WeakModelMap<TreeNodeValue, OrganizationalStructureAdministrationNode>()
    {
        protected OrganizationalStructureAdministrationNode newModel(TreeNodeValue key)
        {
            try
            {
                return obf.newObjectBuilder(OrganizationalStructureAdministrationNode.class).use(AccountAdministrationNode.this, accountModel.userResource(), accountModel.serverResource().organizations(), key).newInstance();
            } catch (ResourceException e)
            {
                throw new OperationException(AdministrationResources.could_not_refresh_list_of_organizations, e);
            }
        }
    };

    private TreeNode parent;
    private TreeValue organizations;

    public AccountAdministrationNode(@Uses TreeNode parent, @Uses AccountModel accountModel) throws ResourceException
    {
        this.parent = parent;
        this.accountModel = accountModel;
        accountModel.addObserver(this);
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

    public AccountModel accountModel()
    {
        return accountModel;
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

    public void refresh() throws Exception
    {
        organizations = accountModel.organizations();
        models.clear();
    }

    public void update(Observable o, Object arg)
    {
        try
        {
            refresh();
        } catch (Exception e)
        {
            new OperationException(AdministrationResources.could_not_refresh_list_of_organizations, e);
        }
    }
}
