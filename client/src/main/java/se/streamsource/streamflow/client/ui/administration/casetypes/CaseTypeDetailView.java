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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.administration.CaseTypeEntityDTO;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class CaseTypeDetailView
    extends JPanel implements Refreshable

{

    @Structure
    Module module;

    private CaseTypeDetailModel model;

    private ValueBinder valueBinder;

    private StreamflowSelectableLabel ownerName;
    private StreamflowSelectableLabel ownerId;

    private StreamflowSelectableLabel caseTypeId;

    public CaseTypeDetailView(@Service ApplicationContext context,
                              @Uses final CaseTypeDetailModel model,
                              @Structure Module module)
    {
       this.model = model;
        valueBinder = module.objectBuilderFactory().newObject( ValueBinder.class );

        FormLayout layout = new FormLayout( "150dlu, 2dlu, 350", "pref, pref, pref, pref, pref, pref" );
        setLayout( layout );
        setMaximumSize( new Dimension( Short.MAX_VALUE, 50 ) );
        DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );

        ownerName = new StreamflowSelectableLabel();

        builder.append( new JLabel( " " ) );
        builder.nextLine();

        builder.appendSeparator( i18n.text( AdministrationResources.casetype_separator) );

        builder.nextLine();

        builder.append( i18n.text(AdministrationResources.owner_name_label), valueBinder.bind( "ownerName", ownerName ) );
        builder.nextLine();

        ownerId = new StreamflowSelectableLabel();

        builder.append( i18n.text(AdministrationResources.owner_id_label), valueBinder.bind( "ownerId", ownerId ) );

        builder.nextLine();
        builder.append( new JLabel( " " ) );
        builder.nextLine();

        builder.appendSeparator( i18n.text( AdministrationResources.casetype_id_separator) );

        builder.nextLine();

        caseTypeId = new StreamflowSelectableLabel();

        builder.append( i18n.text(AdministrationResources.id_label), valueBinder.bind( "id", caseTypeId ) );

        new RefreshWhenShowing(this,this);
    }

    @Override
    public void refresh()
    {
        model.refresh();

        valueBinder.update(model.getResourceValue().index().get());
    }
}
