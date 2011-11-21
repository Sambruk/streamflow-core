/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * JAVADOC
 */
public abstract class ListDetailView
      extends JSplitPane
   implements TransactionListener
{
   protected JList list;

   public ListDetailView()
   {
      setBorder( Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      setRightComponent( new JPanel() );
      setBorder( BorderFactory.createEmptyBorder() );

      setDividerLocation( 250 );
      setOneTouchExpandable( true );
   }

   protected void initMaster( EventListModel<LinkValue> listModel, Action createAction, Action[] selectionActions, final DetailFactory factory)
   {
      list = new JList(listModel);
      list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      list.setCellRenderer( new LinkListCellRenderer() );

      JScrollPane scrollPane = new JScrollPane( list );

      JPanel master = new JPanel(new BorderLayout());
      master.add( scrollPane, BorderLayout.CENTER );

      // Toolbar
      JPanel toolbar = new JPanel();

      if (createAction != null)
         toolbar.add( new JButton( createAction ) );

      if (selectionActions.length != 0)
      {
         JPopupMenu options = new JPopupMenu();
         for (Action selectionAction : selectionActions)
         {
            options.add( selectionAction );
         }
         list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( selectionActions ) );
         toolbar.add( new JButton( new OptionsAction( options ) ) );
      }

      master.add( toolbar, BorderLayout.SOUTH);

      setLeftComponent( master );

      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               LinkValue detailLink = (LinkValue) list.getSelectedValue();
               if (detailLink != null)
               {
                  int selectedIndex = -1;
                  if (getRightComponent() != null && getRightComponent() instanceof TabbedResourceView)
                  {
                     TabbedResourceView tab = (TabbedResourceView) getRightComponent();
//                     selectedIndex = tab.getSelectedIndex();
                  }

                  Component detailView = factory.createDetail( detailLink );

                  if (detailView instanceof TabbedResourceView)
                  {
//                     ((TabbedResourceView)detailView).setSelectedIndex( selectedIndex );
                  }

                  setRightComponent( detailView );

                  if (detailView instanceof ListDetailView || detailView instanceof TabbedResourceView)
                  {
                     ListDetailView parentListDetailView = (ListDetailView) SwingUtilities.getAncestorOfClass( ListDetailView.class, ListDetailView.this);
                     if (parentListDetailView != null)
                        parentListDetailView.setDividerLocation( 0 );
                  }

               } else
               {
                  setRightComponent( new JPanel() );
               }
            }
         }
      } );
   }

   public interface DetailFactory
   {
      Component createDetail(LinkValue detailLink);
   }

   protected LinkValue getSelectedValue()
   {
      return (LinkValue) list.getSelectedValue();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches(Events.withUsecases( "delete", "move" ), transactions) 
            && !Events.matches( Events.withNames( "removedLabel" ), transactions ))
         list.clearSelection();
   }
}