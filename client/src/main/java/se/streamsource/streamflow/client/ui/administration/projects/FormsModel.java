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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.FormDefinitionsClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.util.List;

/**
 * JAVADOC
 */
public class FormsModel
    extends AbstractListModel
        implements Refreshable

{
    @Uses
    FormDefinitionsClientResource forms;

    private List<ListItemValue> formsList;

    public int getSize()
    {
        return formsList == null ? 0 : formsList.size();
    }

    public Object getElementAt( int index )
    {
        return formsList.get( index );
    }

    public void refresh()
    {
        try
        {
            formsList = forms.forms().items().get();
            fireContentsChanged( this, 0, getSize() );
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_members, e);
        }
    }
}