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

import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.JavaHelp;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.menu.WorkspaceMenuBar;
import se.streamsource.streamflow.client.ui.status.StatusBarView;

import java.awt.*;

/**
 * Workspace window
 */
public class WorkspaceWindow
    extends FrameView
{
    public WorkspaceWindow(@Service Application application,
                           @Service JavaHelp javaHelp,
                           @Uses WorkspaceMenuBar menu,
                           @Uses WorkspaceView view)
    {
        super(application);

        JXFrame frame = new JXFrame(i18n.text(WorkspaceResources.window_name));
        frame.setLocationByPlatform(true);
        frame.getContentPane().add(view);
        frame.getRootPane().setOpaque(true);
        setFrame(frame);
        setMenuBar(menu);

        JXStatusBar bar = new StatusBarView(getContext());
        setStatusBar(bar);

        frame.setPreferredSize(new Dimension(1000, 600));
        frame.pack();
        javaHelp.enableHelp(this.getRootPane(),"workspace");
    }

    
}
