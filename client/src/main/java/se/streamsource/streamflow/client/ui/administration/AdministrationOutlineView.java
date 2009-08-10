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

import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class AdministrationOutlineView
        extends JPanel
{
    private JXTree tree;

    public AdministrationOutlineView(@Uses AdministrationModel model) throws Exception
    {
        super(new BorderLayout());

        tree = new JXTree(model);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        tree.setCellRenderer(new DefaultTreeRenderer(new WrappingProvider(
                new IconValue()
                {
                    public Icon getIcon(Object o)
                    {
                        if (o instanceof AccountAdministrationNode)
                            return i18n.icon(Icons.account, i18n.ICON_24);
                        else if (o instanceof OrganizationalStructureAdministrationNode)
                            return i18n.icon(Icons.organization, i18n.ICON_24);
                        else
                            return NULL_ICON;
                    }
                },
                new StringValue()
                {
                    public String getString(Object o)
                    {
                        if (o instanceof AdministrationNode)
                            return "                            ";
                        else if (o instanceof AccountAdministrationNode)
                            return ((AccountAdministrationNode)o).accountModel().settings().name().get();
                        else if (o instanceof OrganizationalStructureAdministrationNode)
                            return ((OrganizationalStructureAdministrationNode)o).toString();
                        else
                            return "Unknown";
                    }
                },
                false
        )));

        JPanel toolbar = new JPanel();
        toolbar.setBorder(BorderFactory.createEtchedBorder());

        add(BorderLayout.CENTER, tree);
/*
        javax.swing.Action addAction = getActionMap().get("addOrganizationalUnit");
        toolbar.add(new JButton(addAction));
        final javax.swing.Action removeAction = getActionMap().get("removeOrganizationalUnit");
        toolbar.add(new JButton(removeAction));
        addAction.setEnabled(true);
        removeAction.setEnabled(false);

        add(toolbar, BorderLayout.SOUTH);


        tree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                TreePath leadSelectionPath = e.getNewLeadSelectionPath();
                if (leadSelectionPath != null && OrganizationalStructureAdministrationNode.class.isInstance(leadSelectionPath.getLastPathComponent()))
                {
                    removeAction.setEnabled(true);
                } else
                {
                    removeAction.setEnabled(false);
                }
            }
        });

*/
    }


    public JTree getTree()
    {
        return tree;
    }

/*
    @Action
    public void add()
    {
        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) outline.getSelectionPath().getPath()[1];
        final Account account = (Account) mutableTreeNode.getUserObject();

        OrganizationalStructureAdministrationNode node = (OrganizationalStructureAdministrationNode) outline.getSelectionPath().getLastPathComponent();
        OrganizationalStructureValue ou = (OrganizationalStructureValue) node.getUserObject();
        ValueBuilder<NewOrganizationalUnitContext> newOuBuilder = vbf.newValueBuilder(NewOrganizationalUnitContext.class);
        newOuBuilder.prototype().context().set(ou.organizationalUnit().get());
        uowf.nestedUnitOfWork();

        NewOrganizationalUnitDialog dialog = obf.newObjectBuilder(NewOrganizationalUnitDialog.class).use(controller, newOuBuilder).newInstance();
        cca.register(dialog, account);

        dialogs.showOkCancelHelpDialog(this, dialog);
    }

    @Action
    public void remove()
    {
        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) outline.getSelectionPath().getPath()[1];
        final Account account = (Account) mutableTreeNode.getUserObject();

        CallAuthenticationContext.runWith(account, new Runnable()
        {
            public void run()
            {
                TreePath selectionPath = outline.getSelectionPath();
                OrganizationalStructureAdministrationNode node = (OrganizationalStructureAdministrationNode) selectionPath.getLastPathComponent();
                OrganizationalStructureValue ou = (OrganizationalStructureValue) node.getUserObject();
                EntityReference entityReference = ou.organizationalUnit().get();
                OrganizationalStructureAdministrationNode parentNode = (OrganizationalStructureAdministrationNode)selectionPath.getPathComponent(selectionPath.getPathCount()-2);
                OrganizationalStructureValue parentOu = (OrganizationalStructureValue) parentNode.getUserObject();

                controller.removeOrganization(entityReference, parentOu.organizationalUnit().get());
            }
        });
    }
*/
}
