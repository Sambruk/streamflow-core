/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.resource.user.UserEntityDTO;

import javax.swing.*;
import java.util.List;

public class SelectUsersDialogModel
        extends AbstractListModel
{
    private List<UserEntityDTO> users;

    public SelectUsersDialogModel(@Uses OrganizationsClientResource resource)
    {
        try
        {
            users = resource.users().users().get();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_get_users, e);
        }
    }

    public int getSize()
    {
        return users == null ? 0 : users.size();
    }

    public Object getElementAt(int index)
    {
        return users == null ? null : users.get(index);
    }
}
