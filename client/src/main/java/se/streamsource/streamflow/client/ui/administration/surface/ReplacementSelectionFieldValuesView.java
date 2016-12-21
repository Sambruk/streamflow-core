/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
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
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.TabbedResourceView;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

    DetailFactory factory;

    public ReplacementSelectionFieldValuesView(@Service ApplicationContext context, @Uses final ReplacementSelectionFieldValuesModel model)
    {
        factory = new DetailFactory()
        {
            public Component createDetail( LinkValue detailLink )
            {
                return module.objectBuilderFactory().newObjectBuilder( SelectionElementsView.class).use( model.newResourceModel(detailLink)).newInstance();
            }
        };

        initMaster( new EventListModel<LinkValue>( model.getUnsortedList()),null, new javax.swing.Action[]{}, factory , new LinkListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
                    {
                        if ( value instanceof LinkValue )
                        {
                            LinkValue link = (LinkValue) value;
                            String val = link.text().get();

                            if (link.rel().get().equals("page"))
                            {
                                Component component = super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
                                setFont( getFont().deriveFont( Font.ITALIC ));
                                component.setEnabled( false );
                                return component;
                            }else
                            {
                                Component component = super.getListCellRendererComponent(list, "   " + val, index, isSelected, cellHasFocus);
                                if (link.rel().get().equals("none"))
                                {
                                    component.setEnabled( false );
                                }
                                return component;
                            }

                        }
                        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    }
                });

        list.setPreferredSize(new Dimension(250, 300));

        for( ListSelectionListener listener : list.getListSelectionListeners() )
        {
            list.removeListSelectionListener( listener );
        }

        list.addListSelectionListener( new ListSelectionListener()
        {
            public void valueChanged( ListSelectionEvent e )
            {
                if (!e.getValueIsAdjusting())
                {
                    LinkValue detailLink = (LinkValue) list.getSelectedValue();
                    if (detailLink != null)
                    {
                        if( "selectionfieldvalue".equals( detailLink.rel().get() ) )
                        {
                            setRightComponent( factory.createDetail( detailLink ) );
                        } else {
                            setRightComponent( new JPanel() );
                        }
                    } else
                    {
                        setRightComponent( new JPanel() );
                    }
                }
            }
        } );

        new RefreshWhenShowing(this, model);
    }
}
