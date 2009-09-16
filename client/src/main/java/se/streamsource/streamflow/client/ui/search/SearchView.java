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

package se.streamsource.streamflow.client.ui.search;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

import se.streamsource.streamflow.client.infrastructure.ui.i18n;

/**
 * JAVADOC
 */
public class SearchView
        extends JPanel
{
    public JTextField searchField;
    private SearchResultTableModel model;

    public SearchView(@Service ApplicationContext context,
                      @Uses final SearchResultTableView resultView,
                      @Uses SearchResultTableModel model)
    {
        super(new BorderLayout());
        this.model = model;

        ActionMap am = context.getActionMap(this);

        searchField = new JTextField(100);
        final String searchText = i18n.text(SearchResources.enter_search_text);
        searchField.setText(searchText);
        searchField.setForeground(Color.gray);
        setMinimumSize(new Dimension(500, 0));
        add(searchField, BorderLayout.NORTH);
        add(resultView, BorderLayout.CENTER);

        searchField.setAction(am.get("search"));

        searchField.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                if (searchField.getText().equals(searchText))
                {
                    searchField.setText("");
                    searchField.setForeground(Color.black);
                }
            }

            public void focusLost(FocusEvent e)
            {
                if (searchField.getText().equals(""))
                {
                    searchField.setText(searchText);
                    searchField.setForeground(Color.gray);
                }
            }
        });
    }

    @org.jdesktop.application.Action
    public void search() throws ResourceException
    {
        model.search(searchField.getText());
    }

    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);
    }

    @Override
    public void setSize(Dimension d)
    {
        super.setSize(d);
    }


}
