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

package se.streamsource.streamflow.client.ui.administration.projects;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;

/**
 * Select a name for something.
 */
public class FormsSelectionDialog
        extends JPanel
{

    private JList forms;
    private ListItemValue selected;

    public FormsSelectionDialog(@Service ApplicationContext context, @Uses FormsSelectionModel model)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));

        forms = new JList(model);
        forms.setCellRenderer(new ListItemCellRenderer());

        if (model.getSize() == 0)
        {
            add(new JLabel(i18n.text(AdministrationResources.no_form_definitions_available)), BorderLayout.CENTER);
        } else
        {
            add(forms, BorderLayout.CENTER);
        }
    }

    @Action
    public void execute()
    {
        selected = (ListItemValue) forms.getSelectedValue();

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }


    public ListItemValue getSelectedForm()
    {
        return selected;
    }
}