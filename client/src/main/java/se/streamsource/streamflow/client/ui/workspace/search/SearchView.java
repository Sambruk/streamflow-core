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

package se.streamsource.streamflow.client.ui.workspace.search;

import ca.odell.glazedlists.swing.EventComboBoxModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.SavedSearchListCellRenderer;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */
public class SearchView
      extends JPanel
   implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   Iterable<SaveSearchDialog> saveSearchDialogs;

   @Uses
   protected ObjectBuilder<HandleSearchesDialog> handleSearchesDialogs;

   private JComboBox searches;
   private SavedSearchesModel model;

   public SearchView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      setBorder( Borders.createEmptyBorder( "3dlu,2dlu,4dlu,4dlu" ) );

      ActionMap am;
      setActionMap( am = context.getActionMap( this ) );

      model = obf.newObjectBuilder( SavedSearchesModel.class ).use(client).newInstance();

      searches = new JComboBox(new EventComboBoxModel<LinkValue>( model.getList() ) );
      searches.setEditable( true );
      searches.setMaximumRowCount( 10 );
      searches.setRenderer( new SavedSearchListCellRenderer() );
      searches.setEditor( new SearchComboEditor() );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "add" ) );
      options.add( am.get( "handle" ) );

      add( searches, BorderLayout.CENTER );
      add( new JButton( new OptionsAction( options ) ), BorderLayout.EAST );

      new RefreshWhenShowing( this, model);
   }

   public JTextField getTextField()
   {
      return (JTextField) searches.getEditor();
   }

   @Action
   public Task add()
   {
      final SaveSearchDialog dialog = saveSearchDialogs.iterator().next();
      dialog.presetQuery( searches.getEditor().getItem().toString() );
      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), dialog, text( WorkspaceResources.save_search ) );

      if (dialog.search() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.saveSearch( dialog.search() );
            }
         };
      } else
         return null;
   }

   @Action
   public void handle()
   {
      HandleSearchesDialog handleSearchesDialog = handleSearchesDialogs.use( vbf, this.model ).newInstance();
      dialogs.showOkDialog( WindowUtils.findWindow( this ), handleSearchesDialog, text( WorkspaceResources.handle_searches ) );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Specifications.or(Events.onEntities( model.getList() ), Events.withNames( "createdSavedSearch", "removedSavedSearch" )), transactions ))
         model.refresh();
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