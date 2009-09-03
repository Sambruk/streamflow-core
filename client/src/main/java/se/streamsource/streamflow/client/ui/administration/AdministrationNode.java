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
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.menu.AccountsModel;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * JAVADOC
 */
public class AdministrationNode
        implements TreeNode, Refreshable
{
    @Structure
    ObjectBuilderFactory obf;

    @Uses
    AccountsModel accountsModel;

    WeakModelMap<AccountModel, AccountAdministrationNode> models = new WeakModelMap<AccountModel, AccountAdministrationNode>()
    {
        @Override
        protected AccountAdministrationNode newModel(AccountModel key)
        {
            return obf.newObjectBuilder(AccountAdministrationNode.class).use(AdministrationNode.this, key).newInstance();
        }
    };

    public TreeNode getChildAt(final int childIndex)
    {
        return models.get(accountsModel.accountModel(childIndex));
    }

    public int getChildCount()
    {
        return accountsModel.getSize();
    }

    public TreeNode getParent()
    {
        return null;
    }

    public int getIndex(TreeNode node)
    {
        AccountAdministrationNode accNode = (AccountAdministrationNode) node;
        for (int i = 0; i < accountsModel.getSize(); i++)
        {
            AccountModel accountModel = accountsModel.accountModel(i);
            if (accountModel == accNode.accountModel())
                return i;
        }
        return -1;
    }

    public boolean getAllowsChildren()
    {
        return true;
    }

    public boolean isLeaf()
    {
        return false;
    }

    public Enumeration children()
    {
        final Iterator<AccountAdministrationNode> administrationNodeIterator = models.iterator();
        return new Enumeration()
        {
            public boolean hasMoreElements()
            {
                return administrationNodeIterator.hasNext();
            }

            public Object nextElement()
            {
                return administrationNodeIterator.next();
            }
        };
    }

    public void refresh() throws Exception
    {
        for (AccountAdministrationNode model : models)
        {
            model.refresh();
        }
    }
}
