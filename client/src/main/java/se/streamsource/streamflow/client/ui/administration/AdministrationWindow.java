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

import org.jdesktop.application.FrameView;
import org.jdesktop.application.Application;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.ui.menu.WorkspaceMenuBar;
import se.streamsource.streamflow.client.ui.menu.OverviewMenuBar;
import se.streamsource.streamflow.client.ui.menu.SearchMenuBar;
import se.streamsource.streamflow.client.ui.menu.AdministrationMenuBar;
import se.streamsource.streamflow.client.ui.status.StatusBarView;
import se.streamsource.streamflow.client.ui.overview.OverviewResources;
import se.streamsource.streamflow.client.ui.overview.OverviewView;
import se.streamsource.streamflow.client.ui.search.SearchView;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import java.awt.*;

/**
 * Administration window
 */
public class AdministrationWindow
    extends FrameView
{
    public AdministrationWindow(
            @Service Application application,
            @Uses AdministrationMenuBar menu,
            @Uses AdministrationView view)
    {
        super(application);

        JXFrame frame = new JXFrame(i18n.text(AdministrationResources.window_name));
        frame.setLocationByPlatform(true);
        frame.getContentPane().add(view);

        setFrame(frame);
        setMenuBar(menu);

        frame.setPreferredSize(new Dimension(1000, 600));
        frame.pack();
    }

}