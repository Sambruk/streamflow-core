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
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import static se.streamsource.streamflow.infrastructure.configuration.FileConfiguration.OS.mac;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * Base class for menus
 */
public abstract class AbstractMenu
        extends JMenu
{
    private ApplicationContext context;

    public void init(@Service ApplicationContext context)
    {
        setActionMap(context.getActionMap());
        this.context = context;
        init();
    }

    abstract protected void init();

    protected void menu(String menuName, String... menuItems)
    {
        ActionMap am = getActionMap();

        ResourceMap resourceMap = context.getResourceMap(getClass(), AbstractMenu.class);
        String menuTitle = resourceMap.getString(menuName);
        setText(menuTitle);
        setMnemonic(menuTitle.charAt(0));
        for (String menuItem : menuItems)
        {
            if (menuItem.equals("---"))
            {
                add(new JSeparator());
            } else
            {
                String actionName = menuItem.startsWith("*") ? menuItem.substring(1) : menuItem;
                Action menuItemAction = am.get(actionName);

                if (menuItemAction == null)
                {
                    Logger.getLogger("menu").warning("Could not find menu action:"+actionName);
                    continue;
                }
                JMenuItem item = menuItem.startsWith("*") ? new JCheckBoxMenuItem() : new JMenuItem();
                item.setAction(menuItemAction);
                item.setIcon(null);
                add(item);
            }
        }
    }
}