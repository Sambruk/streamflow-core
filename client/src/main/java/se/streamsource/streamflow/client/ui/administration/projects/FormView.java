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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;

import javax.swing.*;
import java.awt.*;
import java.util.Observer;
import java.util.Observable;

/**
 * JAVADOC
 */
public class FormView
    extends JPanel
    implements Observer
{
    private AdministrationView adminView;
    private FormModel model;

    @Structure
    ObjectBuilderFactory obf;
    private JTextArea textArea;

    public FormView(@Service ApplicationContext context,
                    @Uses FormModel model,
                    @Uses AdministrationView adminView)
    {
        super(new BorderLayout());
        this.model = model;
        ActionMap am = context.getActionMap(this);

        this.adminView = adminView;


        model.addObserver(this);
        textArea = new JTextArea(model.getNote());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        add(textArea, BorderLayout.CENTER);
        add(new JButton(am.get("edit")), BorderLayout.SOUTH);

    }

    @org.jdesktop.application.Action
    public void edit()
    {
        FormEditAdminView formEditAdminView = obf.newObjectBuilder(FormEditAdminView.class).
                use(model.getResource()).newInstance();

        adminView.show( formEditAdminView );
    }

    public FormModel getModel()
    {
        return model;
    }

    public void update(Observable observable, Object o)
    {
        textArea.setText(model.getNote());
    }
}
