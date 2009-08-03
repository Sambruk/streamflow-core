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

package se.streamsource.streamflow.client.ui.navigator;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.client.ui.menu.MenuView;
import static se.streamsource.streamflow.client.ui.navigator.NavigatorResources.*;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceView;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

/**
 * JAVADOC
 */
public class NavigatorView
        extends JTabbedPane
{
    private WorkspaceView workspace;
    private AdministrationView administration;
    private MenuView menu;

    public NavigatorView(@Service WorkspaceView workspace,
                         @Service AdministrationView administration,
                         @Service MenuView menu,
                         @Service ApplicationContext context)
    {
        this.workspace = workspace;
        this.administration = administration;
        this.menu = menu;
        addTab(text(workspace_label), workspace);
        addTab(text(overview_label), new JLabel("Overview"));
        addTab(text(administration_label), administration);

        setMnemonicAt(0, mnemonic(workspace_label_mnemonic));

        setSelectedComponent(workspace);
    }

    public AdministrationView getAdministration()
    {
        return administration;
    }

    public MenuView getMenu()
    {
        return menu;
    }
}
