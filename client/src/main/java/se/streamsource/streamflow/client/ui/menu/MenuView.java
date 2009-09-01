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

package se.streamsource.streamflow.client.ui.menu;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.qi4j.api.injection.scope.Service;

import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 * JAVADOC
 */
public class MenuView
        extends JMenuBar
{
    private ApplicationContext context;

    public MenuView(@Service ApplicationContext context)
    {
        setActionMap(context.getActionMap());
        this.context = context;
        menu("fileMenu",
                "---",
                "quit");

        menu("settingsMenu",
                "manageAccounts");
        menu("window",
                "showWorkspaceWindow",
                "showOverviewWindow",
                "showAdministrationWindow",
                "showSearchWindow");

    }

    private void menu(String menuName, String... actionNames)
    {
        ActionMap am = getActionMap();

        ResourceMap resourceMap = context.getResourceMap(getClass());
        String menuTitle = resourceMap.getString(menuName);
        JMenu menu = new JMenu(menuTitle);
        for (String actionName : actionNames)
        {
            if (actionName.equals("---"))
            {
                menu.add(new JSeparator());
            } else
            {
                JMenuItem menuItem = new JMenuItem();
                menuItem.setAction(am.get(actionName));
                menuItem.setIcon(null);
                menu.add(menuItem);
            }
        }
        add(menu);
    }
}