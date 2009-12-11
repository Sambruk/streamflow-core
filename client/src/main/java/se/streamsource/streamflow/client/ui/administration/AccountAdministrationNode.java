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
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationsModel;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationModel;
import se.streamsource.streamflow.domain.organization.AdministrationType;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class AccountAdministrationNode
        implements TreeNode, Refreshable, Observer, EventListener
{
    @Structure
    private ObjectBuilderFactory obf;

    private AccountModel accountModel;

    private UsersAdministrationModel usersModel;
    private OrganizationsModel organizationsModel;

    WeakModelMap<TreeNodeValue, TreeNode> models = new WeakModelMap<TreeNodeValue, TreeNode>()
    {
        protected TreeNode newModel( TreeNodeValue key )
        {
            if (key.nodeType().get().equals( AdministrationType.organization.name()))
                return obf.newObjectBuilder( OrganizationAdministrationNode.class).use(AccountAdministrationNode.this, accountModel.serverResource().organizations(), key).newInstance();
            else
                return obf.newObjectBuilder( OrganizationalUnitAdministrationNode.class).use(AccountAdministrationNode.this, accountModel.serverResource().organizations(), key).newInstance();
        }
    };

    private TreeNode parent;
    private TreeValue organizations;

    public AccountAdministrationNode( @Uses TreeNode parent, @Uses AccountModel accountModel ) throws ResourceException
    {
        this.parent = parent;
        this.accountModel = accountModel;
        accountModel.addObserver( this );
    }

    public TreeNode getParent()
    {
        return parent;
    }

    public int getIndex( TreeNode node )
    {
        if (organizations == null)
            return -1;

        for (int idx = 0; idx < organizations.roots().get().size(); idx++)
        {
            TreeNodeValue treeNodeValue = organizations.roots().get().get( idx );
            TreeNode child = models.get( treeNodeValue );
            if (child.equals( node ))
                return idx;
        }

        return -1;
    }

    public AccountModel accountModel()
    {
        return accountModel;
    }

    public UsersAdministrationModel usersModel()
    {
        if (usersModel == null)
        {
            usersModel = obf.newObjectBuilder( UsersAdministrationModel.class ).use( accountModel.serverResource().organizations() ).newInstance();
        }

        return usersModel;
    }

    public OrganizationsModel organizationsModel()
    {
        if (organizationsModel == null)
        {
            organizationsModel = obf.newObjectBuilder( OrganizationsModel.class ).use( accountModel.serverResource().organizations() ).newInstance();
        }

        return organizationsModel;
    }

    public Enumeration children()
    {
        return Collections.enumeration( Collections.emptyList() );
    }

    public TreeNode getChildAt( int index )
    {
        if (organizations == null)
            return null;

        TreeNodeValue treeNode = organizations.roots().get().get( index );
        return models.get( treeNode );
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

    public void refresh()
    {
        try
        {
            models.clear();
            organizations = accountModel.organizations();
        } catch (ResourceException e)
        {
            throw new OperationException( AdministrationResources.could_not_refresh, e );
        }
    }

    public void update( Observable o, Object arg )
    {
        try
        {
            refresh();
        } catch (Exception e)
        {
            throw new OperationException( AdministrationResources.could_not_refresh, e );
        }
    }

    public void notifyEvent( DomainEvent event )
    {
        if (usersModel != null)
            usersModel.notifyEvent( event );

        if (organizationsModel != null)
            organizationsModel.notifyEvent( event );

        for (TreeNode model : models)
        {
            ((EventListener)model).notifyEvent( event );
        }
    }
}
