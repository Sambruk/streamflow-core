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

package se.streamsource.streamflow.client.ui.administration.projects;

import org.jdesktop.swingx.tree.TreeModelSupport;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MemberClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MembersClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Set;

/**
 * JAVADOC
 */
public class ProjectMembersModel
        implements TreeModel, Refreshable
{
    @Uses private MembersClientResource members;
    @Uses OrganizationalUnitAdministrationModel ouAdminModel;

    @Structure
    ObjectBuilderFactory obf;

    private TreeValue root;
    private TreeModelSupport modelSupport = new TreeModelSupport(this);

    public Object getRoot()
    {
        return root;
    }

    public Object getChild(Object parent, int index)
    {
        if (parent instanceof TreeValue)
        {
            return ((TreeValue) parent).roots().get().get(index);
        } else
        {
            return ((TreeNodeValue) parent).children().get().get(index);
        }
    }

    public int getChildCount(Object parent)
    {
        if (parent instanceof TreeValue)
        {
            return ((TreeValue) parent).roots().get().size();
        } else
        {
            return ((TreeNodeValue) parent).children().get().size();
        }
    }

    public boolean isLeaf(Object node)
    {
        if (node instanceof TreeValue)
        {
            return ((TreeValue) node).roots().get().isEmpty();
        } else
        {
            return ((TreeNodeValue) node).children().get().isEmpty();
        }
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        if (parent instanceof TreeValue)
        {
            return ((TreeValue) parent).roots().get().indexOf(child);
        } else
        {
            return ((TreeNodeValue) parent).children().get().indexOf(child);
        }
    }

    public void addTreeModelListener(TreeModelListener l)
    {
        modelSupport.addTreeModelListener(l);
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
        modelSupport.removeTreeModelListener(l);
    }

    public void refresh()
    {
        try
        {
            root = members.memberRoles();
            modelSupport.fireNewRoot();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_members, e);
        }
    }

    public void addMembers(Set<String> newMembers)
    {
        try
        {
            for (String value: newMembers)
            {
                MemberClientResource member = this.members.member(value);
                member.put(null);
            }
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_add_members, e);
        }
    }

    public void removeMember(EntityReference entityReference)
    {
        try
        {
            members.member(entityReference.identity()).delete();
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_member, e);
        }
    }

    public MemberRolesModel memberRolesModel(String id)
    {
        return obf.newObjectBuilder(MemberRolesModel.class).use(members.member(id),
                ouAdminModel).newInstance();
    }
}