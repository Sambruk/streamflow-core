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
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.OrganizationClientResource;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * JAVADOC
 */
public class OrganizationalStructureAdministrationNode
        extends DefaultMutableTreeNode implements Transferable, EventListener
{
    ObjectBuilderFactory obf;

    WeakModelMap<TreeNodeValue, OrganizationalStructureAdministrationNode> models = new WeakModelMap<TreeNodeValue, OrganizationalStructureAdministrationNode>()
    {
        @Override
        protected OrganizationalStructureAdministrationNode newModel(TreeNodeValue key)
        {
            return obf.newObjectBuilder(OrganizationalStructureAdministrationNode.class).use(OrganizationalStructureAdministrationNode.this, key, orgResource).newInstance();
        }
    };

    WeakModelMap<String, OrganizationalUnitAdministrationModel> orgUnitModels = new WeakModelMap<String, OrganizationalUnitAdministrationModel>()
    {

        protected OrganizationalUnitAdministrationModel newModel(String key)
        {
            try
            {
                OrganizationClientResource resource = orgResource.organization( key );
                return obf.newObjectBuilder(OrganizationalUnitAdministrationModel.class).use(resource).newInstance();
            } catch (ResourceException e)
            {
                throw new OperationException(AdministrationResources.could_not_get_organization, e);
            }
        }
    };

    OrganizationalUnitAdministrationModel model;

    OrganizationsClientResource orgResource;
    private TreeNode parentNode;

    public OrganizationalStructureAdministrationNode(@Uses TreeNode parent, @Uses TreeNodeValue ou, @Uses OrganizationsClientResource orgResource, @Structure ObjectBuilderFactory obf) throws ResourceException
    {
        super(ou.buildWith().prototype());
        this.obf = obf;
        this.orgResource = orgResource;
        this.parentNode = parent;
        model = getOrgUnitModel(ou.entity().get().identity());

        for (TreeNodeValue treeNodeValue : ou.children().get())
        {
            add(obf.newObjectBuilder(OrganizationalStructureAdministrationNode.class).use(this, treeNodeValue, orgResource).newInstance());
        }
    }

    public OrganizationalUnitAdministrationModel getOrgUnitModel(String key)
    {
        if (parentNode instanceof AccountAdministrationNode)
        {
            return orgUnitModels.get( key );
        } else if (parentNode instanceof OrganizationalStructureAdministrationNode)
        {
            return ((OrganizationalStructureAdministrationNode)parentNode).getOrgUnitModel( key );
        }
        return null;
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

    @Override
    public void setUserObject(Object userObject)
    {
        model.describe(userObject.toString());
        ou().description().set(userObject.toString());
    }

    public OrganizationalUnitAdministrationModel model()
    {
        return model;
    }

    public DataFlavor[] getTransferDataFlavors() {

        DataFlavor[] result = {new DataFlavor(OrganizationalStructureAdministrationNode.class,"OrganizationalStructureNode")};
        return result;
    }

    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return "OrganizationalStructureNode".equals(dataFlavor.getHumanPresentableName());
    }

    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {

        return ((OrganizationalStructureAdministrationNode)parent).ou().entity().get();
    }

    public void notifyEvent( DomainEvent event )
    {
        for (OrganizationalUnitAdministrationModel model : orgUnitModels)
        {
            model.notifyEvent( event );
        }

        for (OrganizationalStructureAdministrationNode organizationalStructureAdministrationNode : models)
        {
            organizationalStructureAdministrationNode.notifyEvent( event );
        }
    }
}