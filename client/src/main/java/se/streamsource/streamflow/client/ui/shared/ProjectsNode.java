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
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import org.restlet.Restlet;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.SharedUserClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

/**
 * JAVADOC
 */
public class ProjectsNode
        extends DefaultMutableTreeTableNode
{
    public ProjectsNode(@Service IndividualRepository repository,
                              @Structure final ObjectBuilderFactory obf,
                              @Service final Restlet client)
    {
        super(repository.individual());

        // add nodes for all accounts
        repository.individual().visitAccounts(new AccountVisitor()
        {

            public void visitAccount(Account account)
            {
                try
                {
                    UserClientResource user = account.user(client);
                    ListValue projects = user.shared().projects().listProjects();

                    for (ListItemValue project : projects.items().get())
                    {
                        SharedUserClientResource projectResource =  user.shared().projects().project(project.entity().get().identity());
                        add(obf.newObjectBuilder(ProjectNode.class).use(projectResource, project.description().get(), account.settings()).newInstance());
                    }
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public Object getValueAt(int column)
    {
        return i18n.text(WorkspaceResources.projects_node);
    }
}