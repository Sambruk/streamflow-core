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
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.ui.DetailView;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Vector;

/**
 * JAVADOC
 */
public class AccountAdministrationNode
        extends DefaultMutableTreeNode
        implements DetailView
{
    @Structure
    ObjectBuilderFactory obf;

    @Service
    Restlet client;

    @Service
    AccountModel accountModel;
    @Service
    AccountView accountView;

    public AccountAdministrationNode(@Uses Account account)
    {
        super(account);
    }

    @Override
    public String toString()
    {
        return ((Account) super.getUserObject()).settings().name().get();
    }

    public Account account()
    {
        return (Account) super.getUserObject();
    }

    @Override
    public TreeNode getChildAt(int index)
    {
        if (children == null)
        {
            getChildCount();
        }

        return super.getChildAt(index);
    }

    @Override
    public int getChildCount()
    {

        if (children == null)
        {
            try
            {
                UserClientResource user = account().user(client);
                TreeValue organizations = user.administration().organizations();

                children = new Vector();
                for (TreeNodeValue treeNode : organizations.roots().get())
                {
                    OrganizationsClientResource resource = account().server(client).organizations();
                    children.add(obf.newObjectBuilder(OrganizationalStructureAdministrationNode.class).use(resource, treeNode).newInstance());
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return super.getChildCount();
    }

    @Override
    public boolean getAllowsChildren()
    {
        return true;
    }

    @Override
    public boolean isLeaf()
    {
        return false;
    }

    public JComponent detailView()
    {
        accountModel.setAccount(account());
        return accountView;
    }
}
