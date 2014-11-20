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
package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;

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

   protected void initMaster( EventListModel<LinkValue> listModel, Action createAction, Action[] selectionActions, final DetailFactory factory, ListCellRenderer renderer )
   {
      initMaster( listModel, createAction, new SelectionActionEnabler( selectionActions ), false, factory, renderer );
   }

   protected void initMaster( EventListModel<LinkValue> listModel, Action createAction, Action[] selectionActions, final DetailFactory factory)
   {
      initMaster( listModel, createAction, selectionActions, false, factory);
   }

   protected void initMaster( EventListModel<LinkValue> listModel, Action createAction, SelectionActionEnabler selectionActionEnabler, final DetailFactory factory)
   {
      initMaster( listModel, createAction, selectionActionEnabler, false, factory);
   }

   protected void initMaster( EventListModel<LinkValue> listModel, Action createAction, Action[] selectionActions, boolean actionsWithoutOption, final DetailFactory factory)
   {
      initMaster( listModel, createAction, new SelectionActionEnabler( selectionActions ), actionsWithoutOption, factory, new LinkListCellRenderer() );
   }

   protected void initMaster( EventListModel<LinkValue> listModel, Action createAction, SelectionActionEnabler selectionActionEnabler, boolean actionsWithoutOption, final DetailFactory factory)
   {
      initMaster( listModel, createAction, selectionActionEnabler, actionsWithoutOption, factory, new LinkListCellRenderer() );
   }

   protected void initMaster( EventListModel<LinkValue> listModel, Action createAction, SelectionActionEnabler selectionActionEnabler, final DetailFactory factory, ListCellRenderer renderer )
   {
      initMaster( listModel, createAction,selectionActionEnabler, false, factory, renderer );
   }

   protected void initMaster( EventListModel<TitledLinkValue> listModel, final DetailFactory factory )
   {
       initMaster( listModel, null,new SelectionActionEnabler(new Action[]{}),false,factory, new SeparatorListCellRenderer( new LinkListCellRenderer() ));
   }

   /**
    * This method creates and regulates a master detail.
    * This method contains a workaround for the streamflow client server action concept. ResourceModels are telling what kind of resources and commands or querys are available
    * on the server. Client code should use ResourceActionEnabler, SelectionActionEnabler, RefreshComponents classes to steer what actions become enabled or not.
    * This is right now not honored completely throughout the administration gui - especially for option actions. They are for the most views enabled by default if a selection in
    * a list is made. For the time being we will have to work around occasions where option actions need to be steered from server side, hence the usage of SelectionActionEnabler as
    * method parameter. That makes it possible to send in a custom selection enabler. See @se.streamsource.streamflow.client.ui.administration.users.initMaster()
    * @param listModel
    * @param createAction
    * @param selectionActionEnabler The selection action enabler to use
    * @param actionsWithoutOption
    * @param factory
    * @param renderer
    */
   protected void initMaster( EventListModel<? extends LinkValue> listModel, Action createAction, SelectionActionEnabler selectionActionEnabler, boolean actionsWithoutOption, final DetailFactory factory, ListCellRenderer renderer)
   {
      list = new JList(listModel);
      list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      list.setCellRenderer( renderer );

      JScrollPane scrollPane = new JScrollPane( list );

      JPanel master = new JPanel(new BorderLayout());
      master.add( scrollPane, BorderLayout.CENTER );

      // Toolbar
      JPanel toolbar = new JPanel();

      if (createAction != null)
         toolbar.add( new StreamflowButton( createAction ) );

      list.getSelectionModel().addListSelectionListener( selectionActionEnabler );

      if( actionsWithoutOption)
      {
         for (Action selectionAction : selectionActionEnabler.getActions())
         {
            toolbar.add( new StreamflowButton( selectionAction ) );
         }

      } else
      {
         if (selectionActionEnabler.getActions().length != 0)
         {
            JPopupMenu options = new JPopupMenu();
            for (Action selectionAction : selectionActionEnabler.getActions())
            {
               options.add( selectionAction );
            }
            
            toolbar.add( new StreamflowButton( new OptionsAction( options ) ) );
         }
      }

      master.add( toolbar, BorderLayout.SOUTH);

      setLeftComponent( master );

      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {

            if (!e.getValueIsAdjusting() )
            {
               if(!(list.getSelectedValue() instanceof SeparatorList.Separator))
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
            && !Events.matches( Events.withNames( /*"removedLabel",*/ "removedSelectedForm", "removedParticipant"), transactions ))
         list.clearSelection();
   }
}