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

package se.streamsource.streamflow.client;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.swingx.util.WindowUtils;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class AboutDialog
    extends JPanel
{
    public AboutDialog()
    {
        super(new BorderLayout());

        setActionMap(Application.getInstance().getContext().getActionMap(this));

        add(new JLabel("About StreamFlow"), BorderLayout.CENTER);
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}
