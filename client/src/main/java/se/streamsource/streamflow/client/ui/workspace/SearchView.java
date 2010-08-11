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

import ca.odell.glazedlists.swing.EventComboBoxModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SavedSearchListCellRenderer;
import se.streamsource.streamflow.client.ui.OptionsAction;

import javax.swing.ActionMap;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

/**
 * JAVADOC
 */
public class SearchView
      extends JPanel
{
   @Service
   DialogService dialogs;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   Iterable<SaveSearchDialog> saveSearchDialogs;

   @Uses
   protected ObjectBuilder<HandleSearchesDialog> handleSearchesDialog;

   private JComboBox searches;
   private SavedSearchesModel model;

   private RefreshWhenVisible refresher;

   public SearchView( @Service ApplicationContext context )
   {
      super( new BorderLayout() );
      setBorder( Borders.createEmptyBorder( "3dlu,2dlu,4dlu,4dlu" ) );

      ActionMap am;
      setActionMap( am = context.getActionMap( this ) );

      searches = new JComboBox();
      searches.setEditable( true );
      searches.setMaximumRowCount( 10 );
      searches.setRenderer( new SavedSearchListCellRenderer() );
      searches.setEditor( new SearchComboEditor() );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "add" ) );
      options.add( am.get( "handle" ) );

      add( searches, BorderLayout.CENTER );
      add( new JButton( new OptionsAction( options ) ), BorderLayout.EAST );

   }

   public void setModel( SavedSearchesModel savedSearches )
   {
      savedSearches.refresh();
      this.model = savedSearches;
      searches.setModel( new EventComboBoxModel<LinkValue>( model.getEventList() ) );
   }

   public JTextField getTextField()
   {
      return (JTextField) searches.getEditor();
   }

   @Action
   public void add()
   {
      SaveSearchDialog dialog = saveSearchDialogs.iterator().next();
      dialog.presetQuery( searches.getEditor().getItem().toString() );
      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), dialog, text( WorkspaceResources.save_search ) );

      if (dialog.search() != null)
      {
         model.saveSearch( dialog.search() );
      }
   }

   @Action
   public void handle()
   {
      HandleSearchesDialog dialog = handleSearchesDialog.use( vbf, this.model ).newInstance();
      dialogs.showOkDialog( WindowUtils.findWindow( this ), dialog, text( WorkspaceResources.handle_searches ) );
   }

   class SearchComboEditor extends JTextField
         implements ComboBoxEditor
   {
      private LinkValue link;

      public Component getEditorComponent()
      {
         return this;
      }

      public void setItem( Object anObject )
      {
         if (anObject instanceof TitledLinkValue)
         {
            this.link = (LinkValue) anObject;
            this.setText( ((TitledLinkValue) anObject).title().get() );
         }
      }

      public Object getItem()
      {
         return this.getText();
      }

      public LinkValue getLink()
      {
         return link;
      }

      public void clear()
      {
         link = null;
         this.setText( "" );
      }
   }
}