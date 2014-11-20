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
package se.streamsource.streamflow.client.ui.administration.casetypes;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.TabbedResourceView;
import se.streamsource.streamflow.client.util.TitledLinkGroupingComparator;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class RemovedCaseTypesView
        extends ListDetailView
{
    RemovedCaseTypesModel model;

    @Structure
    Module module;

    public RemovedCaseTypesView( @Service ApplicationContext context,
                          @Uses final RemovedCaseTypesModel model)
    {
        this.model = model;

        final ActionMap am = context.getActionMap( this );
        setActionMap( am );

        initMaster(new EventListModel<TitledLinkValue>( model.getList()), new DetailFactory()
        {
            public Component createDetail( LinkValue detailLink )
            {
                final RemovedCaseTypeModel caseTypeModel = (RemovedCaseTypeModel) model.newResourceModel(detailLink);

                TabbedResourceView view = module.objectBuilderFactory().newObjectBuilder(TabbedResourceView.class).use( caseTypeModel ).newInstance();
                return view;
            }
        });

        new RefreshWhenShowing(this, model);
    }

    public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
    {
        model.notifyTransactions( transactions );

        super.notifyTransactions( transactions );
    }
}