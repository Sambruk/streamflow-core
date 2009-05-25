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
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

/**
 * JAVADOC
 */
public class AdministrationModel
        extends DefaultTreeModel
{
    @Service
    IndividualRepository individualRepository;

    @Structure
    ObjectBuilderFactory obf;

    public AdministrationModel(@Uses AdministrationNode root)
    {
        super(root);
    }

    public void refresh()
    {
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();

        // Clear old view
        while (root.getChildCount() > 0)
            removeNodeFromParent((MutableTreeNode) root.getChildAt(0));

        individualRepository.individual().visitAccounts(new AccountVisitor()
        {
            public void visitAccount(Account account)
            {
                MutableTreeNode accountNode = obf.newObjectBuilder(AccountAdministrationNode.class).use(account).newInstance();
                insertNodeInto(accountNode, root, root.getChildCount());
            }
        });
    }

}
