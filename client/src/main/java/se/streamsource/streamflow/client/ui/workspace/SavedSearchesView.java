/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.workspace;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * JAVADOC
 */
public class SavedSearchesView
   extends JPanel
   implements ListSelectionListener
{
   private JTextField searchField;
   private SavedSearchesModel model;

   private JList searches;
   private JButton saveSearch;
   private JButton removeSearch;

   private RefreshWhenVisible refresher;

   public SavedSearchesView( @Service ApplicationContext context, @Uses JTextField searchField)
   {
      super(new BorderLayout());
      ActionMap am;
      setActionMap( am = context.getActionMap(this ));
      this.searchField = searchField;

      saveSearch = new JButton(am.get( "saveSearch" ));
      removeSearch = new JButton(am.get( "removeSearch" ));
      searches = new JList();
      searches.setCellRenderer( new LinkListCellRenderer() );
      searches.addListSelectionListener( this );

      add(saveSearch, BorderLayout.NORTH);
      add(searches, BorderLayout.CENTER);
      add(removeSearch, BorderLayout.SOUTH);

   }

   public void valueChanged( ListSelectionEvent e )
   {
      if (!searches.isSelectionEmpty())
      {
         LinkValue search = (LinkValue) searches.getSelectedValue();
         searchField.setText( search.id().get() );
         SwingUtilities.getAncestorOfClass( Window.class, this ).setVisible( false );
      }
   }

   @Action
   public void saveSearch()
   {

   }

   @Action
   public void removeSearch()
   {

   }

   public void setModel( SavedSearchesModel savedSearches )
   {
      if (refresher != null)
         removeAncestorListener( refresher );

      this.model = savedSearches;

      searches.setModel( new EventListModel<LinkValue>(model.getEventList()) );

      addAncestorListener( refresher = new RefreshWhenVisible( model, this ));
   }
}
