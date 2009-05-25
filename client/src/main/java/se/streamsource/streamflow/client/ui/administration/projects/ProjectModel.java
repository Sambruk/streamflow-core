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
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.application.administration.query.NoSuchUserException;
import se.streamsource.streamflow.application.administration.query.RegistrationException;
import se.streamsource.streamflow.client.ConnectionException;
import se.streamsource.streamflow.client.resource.organizations.projects.ProjectClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MemberClientResource;
import se.streamsource.streamflow.domain.DuplicateException;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceValue;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * JAVADOC
 */
public class ProjectModel
        implements TreeModel
{
    private ProjectClientResource project;
    private TreeValue root;
    private TreeModelSupport modelSupport = new TreeModelSupport(this);

    public ProjectModel()
    {
    }

    public void setProject(ProjectClientResource project)
    {
        this.project = project;

        refresh();
    }

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

    private void refresh()
    {
        try
        {
            root = project.members().memberRoles();
            modelSupport.fireNewRoot();
        } catch (ResourceException e)
        {
            throw new ConnectionException("Could not refresh list of members", e);
        }
    }

    public void addMember(String participant) throws DuplicateException, RegistrationException, NoSuchUserException, ResourceException
    {
        EntityReferenceValue value = project.findParticipant(participant);
        if (value.entity().get() == null)
            throw new NoSuchUserException(participant);

        MemberClientResource member = project.members().member(value.entity().get().identity());
        member.put(null);
        refresh();
    }

    public void removeMember(EntityReference entityReference) throws ResourceException
    {
        MemberClientResource member = project.members().member(entityReference.identity());
        member.delete();

        refresh();
    }

    public void addRole(String roleName, String member) throws ResourceException
    {
        EntityReferenceValue role = project.findRole(roleName);
        if (role.entity().get() == null)
        {
            throw new IllegalArgumentException("No role named: " + roleName + " found");
        }

        project.members().member(member).roles().role(role.entity().get().identity()).put(null);

        refresh();
    }


}