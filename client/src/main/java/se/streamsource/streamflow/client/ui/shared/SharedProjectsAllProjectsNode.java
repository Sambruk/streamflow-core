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

package se.streamsource.streamflow.client.ui.shared;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.SharedUserClientResource;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

/**
 * JAVADOC
 */
public class SharedProjectsAllProjectsNode
        extends DefaultMutableTreeTableNode
{
    ObjectBuilderFactory obf;

    private Restlet client;

    private Account account;

    public SharedProjectsAllProjectsNode(@Service IndividualRepository repository,
                                         @Service Restlet client,
                                         @Structure ObjectBuilderFactory obf,
                                         @Uses Account account)
    {
        super(repository.individual());
        this.client = client;
        this.obf = obf;
        this.account = account;
        refresh();
    }

    public void refresh()
    {
        try
        {
            UserClientResource user = account.user(client);
            ListValue projects = user.shared().projects().listProjects();

            for (ListItemValue project : projects.items().get())
            {
                SharedUserClientResource projectResource =  user.shared().projects().project(project.entity().get().identity());
                add(obf.newObjectBuilder(SharedProjectNode.class).use(projectResource, project.description().get(), account.settings()).newInstance());
            }
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public Object getValueAt(int column)
    {
        return account.settings().name().get();
    }


}