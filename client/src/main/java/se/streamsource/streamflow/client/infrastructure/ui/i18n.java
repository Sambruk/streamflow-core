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

package se.streamsource.streamflow.client.infrastructure.ui;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class i18n
{
    public static final int ICON_16 = 16;
    public static final int ICON_24 = 24;

    public static String text(Enum resource)
    {
        String string = getResourceMap(resource).getString(resource.name());
        if (string == null)
            string = "#" + resource.name();
        return string;
    }

    public static int mnemonic(Enum resource)
    {
        ResourceMap resourceMap = getResourceMap(resource);
        Integer keycode = resourceMap.getKeyCode(resource.name());
        if (keycode == null)
            return KeyEvent.VK_UNDEFINED;
        return keycode;
    }

    public static ImageIcon icon(Enum resource)
    {
        return icon(resource, ICON_24);
    }

    public static ImageIcon icon(Enum resource, int size)
    {
        ResourceMap resourceMap = getResourceMap(resource);
        ImageIcon icon = resourceMap.getImageIcon(resource.name());
        Image image = icon.getImage();
        if (icon.getIconWidth() != size)
        {
            image = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            icon.setImage(image);
        }
        return icon;
    }

    private static ResourceMap getResourceMap(Enum resource)
    {
        ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(resource.getClass());
        if (resourceMap == null)
            throw new IllegalArgumentException("No resource map found for resource:" + resource);
        return resourceMap;
    }
}
