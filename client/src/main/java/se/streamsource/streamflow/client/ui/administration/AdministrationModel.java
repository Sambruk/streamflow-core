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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.infrastructure.event.source.*;

import javax.swing.tree.DefaultTreeModel;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class AdministrationModel
        extends DefaultTreeModel
{
    @Structure
    ObjectBuilderFactory obf;
    private EventSourceListener subscriber;

    WeakModelMap<Account, AccountAdministrationNode> nodes = new WeakModelMap<Account, AccountAdministrationNode>()
    {
        @Override
        protected AccountAdministrationNode newModel(Account key)
        {
            return obf.newObjectBuilder(AccountAdministrationNode.class).use(getRoot(), key).newInstance();
        }
    };

    public AdministrationModel(@Uses AdministrationNode root, @Service EventSource source)
    {
        super(root);

        subscriber = new EventSourceListener()
        {

            public void eventsAvailable(EventStore source, EventSpecification specification)
            {
                Logger.getLogger("administration").info("Refresh organizational overview");
                getRoot().refresh();
                reload(getRoot());
            }
        };
        source.registerListener(subscriber, new EventQuery().
                withNames("organizationalUnitMoved", "organizationalUnitMerged"));

    }

    @Override
    public AdministrationNode getRoot()
    {
        return (AdministrationNode) super.getRoot();
    }

    public void refresh()
    {
        getRoot().refresh();
        reload(getRoot());
    }

    public void createOrganizationalUnit(OrganizationalStructureAdministrationNode orgNode, String name)
    {
        orgNode.model().createOrganizationalUnit(name);
    }
}
