/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import org.qi4j.api.injection.scope.Uses;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

import javax.swing.*;

/**
 * JAVADOC
 */
public class WorkspaceView
        extends JTabbedPane
{
    public WorkspaceView(@Uses WorkToolbarView filter,
                         @Uses InboxView inboxView,
                         @Uses WorkView workView,
                         @Uses ContextsView contextsView)
    {
        addTab(text(inbox_label), inboxView);
        addTab(text(work_label), workView);
        addTab(text(contexts_label), contextsView);
    }
}