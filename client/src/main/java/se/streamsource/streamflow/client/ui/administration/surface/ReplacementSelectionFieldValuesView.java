/**
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.administration.forms.FormView;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.SelectionElementsView;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import java.awt.*;

/**
 *
 */
public class ReplacementSelectionFieldValuesView
        extends ListDetailView
        implements TransactionListener
{
    @Structure
    Module module;

    public ReplacementSelectionFieldValuesView(@Service ApplicationContext context, @Uses final ReplacementSelectionFieldValuesModel model)
    {

        initMaster( new EventListModel<LinkValue>( model.getList()),null, new javax.swing.Action[]{}, new DetailFactory()
        {
            public Component createDetail( LinkValue detailLink )
            {
                return module.objectBuilderFactory().newObjectBuilder( SelectionElementsView.class).use( model.newResourceModel(detailLink)).newInstance();
            }
        });

        list.setPreferredSize(new Dimension(250,300));

        new RefreshWhenShowing(this, model);
    }
}
