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

package se.streamsource.streamflow.client.ui.search;

import org.jdesktop.application.FrameView;
import org.jdesktop.application.Application;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.menu.WorkspaceMenuBar;
import se.streamsource.streamflow.client.ui.menu.OverviewMenuBar;
import se.streamsource.streamflow.client.ui.menu.SearchMenuBar;
import se.streamsource.streamflow.client.ui.status.StatusBarView;
import se.streamsource.streamflow.client.ui.overview.OverviewResources;
import se.streamsource.streamflow.client.ui.overview.OverviewView;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.OperationException;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

/**
 * Search window
 */
public class SearchWindow
    extends FrameView
{
    public SearchWindow(@Service Application application,
                        @Uses SearchMenuBar menu,
                        @Uses final AccountSelector accountSelector,
                        @Structure final ObjectBuilderFactory obf)
    {
        super(application);

        final JXFrame frame = new JXFrame(i18n.text(SearchResources.window_name));
        frame.setLocationByPlatform(true);

        accountSelector.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (accountSelector.isSelectionEmpty())
                    {
                        frame.getContentPane().removeAll();
                    } else
                    {
                        frame.getContentPane().removeAll();

                        try
                        {
                            AccountModel selectedAccount = accountSelector.getSelectedAccount();
                            String organization = selectedAccount.userResource().administration().organizations().roots().get().get(0).entity().get().identity();
                            SearchResultTableModel model = obf.newObjectBuilder(SearchResultTableModel.class).use(selectedAccount.serverResource().organizations().organization(organization).search()).newInstance();
                            SearchView searchView = obf.newObjectBuilder(SearchView.class).use(model).newInstance();

                            frame.getContentPane().add(searchView);
                        } catch (ResourceException e1)
                        {
                            throw new OperationException(SearchResources.could_not_switch_account, e1);
                        }
                    }
                }
            }
        });

        setFrame(frame);
        setMenuBar(menu);

        frame.setPreferredSize(new Dimension(1000, 600));
        frame.pack();
    }

}