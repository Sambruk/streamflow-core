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
package se.streamsource.streamflow.client.ui.administration.organisationsettings;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.Action;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 *
 */
public class MailRestrictionsView
    extends JPanel
        implements TransactionListener
{
    MailRestrictionsModel model;

    @Service
    DialogService dialogs;

    @Structure
    Module module;

    public JList list;

    private DefaultFormBuilder builder;

    public MailRestrictionsView( @Service ApplicationContext context,
                            @Uses final MailRestrictionsModel model )
    {
        this.model = model;

        FormLayout layout = new FormLayout( "150dlu, 2dlu, 50, 200", "pref" );
        setLayout(layout);
        setMaximumSize( new Dimension( Short.MAX_VALUE, 50 ) );
        builder = new DefaultFormBuilder( layout, this);

        builder.add(new JLabel(i18n.text(AdministrationResources.mailrestrictions_addresses)),
                new CellConstraints(1,1,1,1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 4, 0, 0, 0 )));

        JPanel addressPanel = new JPanel( new BorderLayout() );
        addressPanel.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

        ActionMap am = context.getActionMap( this );
        setActionMap( am );

        JPopupMenu options = new JPopupMenu();
        options.add( am.get( "rename" ) );
        options.add( am.get( "remove" ) );

        JScrollPane scrollPane = new JScrollPane();
        EventList<LinkValue> itemValueEventList = model.getList();
        list = new JList( new EventListModel<LinkValue>( itemValueEventList ) );
        list.setCellRenderer( new LinkListCellRenderer() );
        scrollPane.setViewportView( list );
        addressPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add( new StreamflowButton( am.get( "add" ) ) );
        toolbar.add( new StreamflowButton( new OptionsAction( options ) ) );
        addressPanel.add(toolbar, BorderLayout.SOUTH);

        list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ), am.get( "rename" ) ) );

        builder.add( addressPanel, new CellConstraints( 3, 1, 2, 1) );

        new RefreshWhenShowing( this, model );

    }

    @Action
    public Task add()
    {
        final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);

        dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_mailrestriction_title ) );

        if (!Strings.empty(dialog.name()))
        {
            return new CommandTask()
            {
                @Override
                public void command()
                        throws Exception
                {
                    model.create( dialog.name() );
                }
            };
        } else
            return null;
    }

    @Action
    public Task remove()
    {
        final LinkValue selected = (LinkValue) list.getSelectedValue();

        ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
        dialog.setRemovalMessage( selected.text().get() );
        dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
        if (dialog.isConfirmed())
        {
            return new CommandTask()
            {
                @Override
                public void command()
                        throws Exception
                {
                    model.remove( selected );
                }
            };
        } else
            return null;
    }

    @Action
    public Task rename()
    {
        final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);
        dialogs.showOkCancelHelpDialog( this, dialog );

        if (!Strings.empty( dialog.name() ))
        {
            return new CommandTask()
            {
                @Override
                public void command()
                        throws Exception
                {
                    model.changeDescription( (LinkValue) list.getSelectedValue(), dialog.name() );
                }
            };
        } else
            return null;
    }

    public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
    {
        model.notifyTransactions( transactions );
    }

}
