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

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.resource.users.shared.user.SharedUserClientResource;

/**
 * JAVADOC
 */
public class ProjectNode
        extends DefaultMutableTreeTableNode
{
    ObjectBuilderFactory obf;

    private SharedUserClientResource projectClientResource;

    @Uses String projectName;

    @Uses
    private AccountSettingsValue settings;
    

    public ProjectNode(@Service IndividualRepository repository,
                             @Uses SharedUserClientResource projectClientResource,
                             @Structure ObjectBuilderFactory obf)
    {
        super(repository.individual());
        this.projectClientResource = projectClientResource;
        this.obf = obf;
        refresh();
    }

    private void refresh()
    {
        add(obf.newObjectBuilder(ProjectInboxNode.class).use(projectClientResource.inbox()).newInstance());
        add(obf.newObjectBuilder(ProjectAssignmentsNode.class).use(projectClientResource.assignments()).newInstance());
        add(obf.newObjectBuilder(ProjectDelegationsNode.class).use(projectClientResource.delegations()).newInstance());
        add(obf.newObjectBuilder(ProjectWaitingForNode.class).use(projectClientResource.waitingFor()).newInstance());
    }

    @Override
    public Object getValueAt(int column)
    {
        return projectName;
    }

    public AccountSettingsValue getSettings()
    {
        return settings;
    }
}