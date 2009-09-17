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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.PopupMenuTrigger;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * JAVADOC
 */
public class AdministrationOutlineView
        extends JPanel
{
    private JXTree tree;

    @Service
    DialogService dialogs;
    @Uses
    Iterable<NameDialog> nameDialogs;

    private AdministrationModel model;

    @Structure
    ObjectBuilderFactory obf;

    public AdministrationOutlineView(@Service ApplicationContext context, 
                                     @Uses final AdministrationModel model) throws Exception
    {
        super(new BorderLayout());
        this.model = model;
        tree = new JXTree(model);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);

        DefaultTreeRenderer renderer = new DefaultTreeRenderer(new WrappingProvider(
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
                            return ((AccountAdministrationNode) o).accountModel().settings().name().get();
                        else if (o instanceof OrganizationalStructureAdministrationNode)
                            return ((OrganizationalStructureAdministrationNode) o).toString();
                        else
                            return "Unknown";
                    }
                },
                false
        ));
        tree.setCellRenderer(renderer);

        JPanel toolbar = new JPanel();
        toolbar.setBorder(BorderFactory.createEtchedBorder());

        add(BorderLayout.CENTER, tree);

        ActionMap am = context.getActionMap(this);
        JPopupMenu popup = new JPopupMenu();
        popup.add(am.get("createOrganizationalUnit"));
        popup.add(am.get("removeOrganizationalUnit"));
        popup.add(new JSeparator());
        popup.add(am.get("moveOrganizationalUnit"));
        popup.add(am.get("mergeOrganizationalUnit"));

        tree.addMouseListener(new PopupMenuTrigger(popup));


        addAncestorListener(new AncestorListener()
        {
            public void ancestorAdded(AncestorEvent event)
            {
                model.refresh();
            }

            public void ancestorRemoved(AncestorEvent event)
            {
            }

            public void ancestorMoved(AncestorEvent event)
            {
            }
        });
    }


    public JTree getTree()
    {
        return tree;
    }

    @Action
    public void createOrganizationalUnit()
    {
        Object node = tree.getSelectionPath().getLastPathComponent();
        if (node instanceof OrganizationalStructureAdministrationNode)
        {
            OrganizationalStructureAdministrationNode orgNode = (OrganizationalStructureAdministrationNode) node;

            NameDialog dialog = nameDialogs.iterator().next();
            dialogs.showOkCancelHelpDialog(this, dialog);
            if (dialog.name() != null)
            {
                ArrayList<Integer> expandedRows = new ArrayList<Integer>();
                for (int i = 0; i < tree.getRowCount(); i++)
                {
                    if (tree.isExpanded(i))
                        expandedRows.add(i);
                }
                int[] selected = tree.getSelectionRows();

                model.createOrganizationalUnit(orgNode, dialog.name());

                model.refresh();
                for (Integer expandedRow : expandedRows)
                {
                    tree.expandRow(expandedRow);
                }
                tree.setSelectionRows(selected);
            }
        }
    }

    @Action
    public void removeOrganizationalUnit()
    {
        Object node = tree.getSelectionPath().getLastPathComponent();
        if (node instanceof OrganizationalStructureAdministrationNode)
        {
            OrganizationalStructureAdministrationNode orgNode = (OrganizationalStructureAdministrationNode) node;

            Object parent = orgNode.getParent();
            if (parent instanceof OrganizationalStructureAdministrationNode)
            {
                OrganizationalStructureAdministrationNode orgParent = (OrganizationalStructureAdministrationNode) parent;
                orgParent.model().removeOrganizationalUnit(orgNode.ou().entity().get());
                model.refresh();

            }
        }
    }

    @Action
    public void moveOrganizationalUnit()
    {
        OrganizationalStructureAdministrationNode parent =
                        (OrganizationalStructureAdministrationNode)tree.getSelectionPath().getParentPath().getLastPathComponent();

        OrganizationalStructureAdministrationNode moved =
                        (OrganizationalStructureAdministrationNode)tree.getSelectionPath().getLastPathComponent();

        SelectOrganizationalUnitDialog moveAndMergeDialog = obf.newObjectBuilder(SelectOrganizationalUnitDialog.class).use(model).newInstance();

        dialogs.showOkCancelHelpDialog(WindowUtils.findWindow(this), moveAndMergeDialog, i18n.text(AdministrationResources.move_to));
        
        if( moveAndMergeDialog.target() != null
                && !moved.ou().entity().get().equals(moveAndMergeDialog.target()))
        {
            moved.model().moveOrganizationalUnit(parent.ou().entity().get(), moveAndMergeDialog.target());
        } else
        {
            dialogs.showOkDialog(WindowUtils.findWindow(this), new JLabel(i18n.text(AdministrationResources.could_not_move_organization)));
        }

    }

    @Action
    public void mergeOrganizationalUnit()
    {
        OrganizationalStructureAdministrationNode parent =
                        (OrganizationalStructureAdministrationNode)tree.getSelectionPath().getParentPath().getLastPathComponent();

        OrganizationalStructureAdministrationNode moved =
                        (OrganizationalStructureAdministrationNode)tree.getSelectionPath().getLastPathComponent();

        SelectOrganizationalUnitDialog moveAndMergeDialog = obf.newObjectBuilder(SelectOrganizationalUnitDialog.class).use(model).newInstance();

        dialogs.showOkCancelHelpDialog(WindowUtils.findWindow(this), moveAndMergeDialog, i18n.text(AdministrationResources.merge_to));

        if(moveAndMergeDialog.target() != null
                && !moved.ou().entity().get().equals(moveAndMergeDialog.target()))
        {
            moved.model().mergeOrganizationalUnit(parent.ou().entity().get(), moveAndMergeDialog.target()); 
        } else
        {
            dialogs.showOkDialog(WindowUtils.findWindow(this), new JLabel(i18n.text(AdministrationResources.could_not_merge_organization)));
        }

    }

}
