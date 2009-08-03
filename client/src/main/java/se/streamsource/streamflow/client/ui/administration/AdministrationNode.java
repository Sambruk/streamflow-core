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
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.Accounts;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.DetailView;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;

/**
 * JAVADOC
 */
public class AdministrationNode
        implements DetailView, TreeNode
{
    @Structure ObjectBuilderFactory obf;

    @Uses
    Accounts.AccountsState accounts;

    WeakModelMap<Account, AccountAdministrationNode> models = new WeakModelMap<Account, AccountAdministrationNode>()
    {
        @Override
        protected AccountAdministrationNode newModel(Account key)
        {
            return obf.newObjectBuilder(AccountAdministrationNode.class).use(AdministrationNode.this, key).newInstance();
        }
    };

    public TreeNode getChildAt(final int childIndex)
    {
        return models.get(accounts.accounts().get(childIndex));
    }

    public int getChildCount()
    {
        return accounts.accounts().count();
    }

    public TreeNode getParent()
    {
        return null;
    }

    public int getIndex(TreeNode node)
    {
        int idx = 0;
        for (Account account : accounts.accounts())
        {
            TreeNode child = models.get(account);
            if (node.equals(child))
                return idx;
            idx++;
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
        return Collections.enumeration(Collections.emptyList());
    }

    public JComponent detailView()
    {
        return new JLabel("Administration");
    }
}
