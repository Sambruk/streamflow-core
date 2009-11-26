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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * JAVADOC
 */
public class FormsAdminView
        extends JSplitPane
{
    @Structure
    ObjectBuilderFactory obf;

    public FormsAdminView(@Uses final FormsView formsView,
                          @Uses final AdministrationView adminView,
                          @Structure final ObjectBuilderFactory obf)
    {
        super();

        setLeftComponent(formsView);
        setRightComponent(new JPanel());

        setDividerLocation(200);

        final JList list = formsView.getFormList();
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    int idx = list.getSelectedIndex();

                    if (idx < list.getModel().getSize() && idx >= 0)
                    {
                        FormView formView = obf.newObjectBuilder(FormView.class).
                                use(formsView.getModel().getFormsResource().form(idx),
                                        adminView).newInstance();
                        formsView.setFormModel(formView.getModel());
                        setRightComponent(
                                formView);
                    } else
                    {
                        setRightComponent(new JPanel());
                    }
                }
            }
        });
    }

}