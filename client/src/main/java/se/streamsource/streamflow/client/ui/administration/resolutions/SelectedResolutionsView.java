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

package se.streamsource.streamflow.client.ui.administration.resolutions;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */
public class SelectedResolutionsView
      extends JPanel
      implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   ObjectBuilder<SelectLinkDialog> labelsDialogs;

   public JList list;

   private SelectedResolutionsModel model;

   public SelectedResolutionsView( @Service ApplicationContext context,
                                   @Uses final CommandQueryClient client,
                                   @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.model = obf.newObjectBuilder( SelectedResolutionsModel.class ).use( client ).newInstance();
      setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      list = new JList( new EventListModel<LinkValue>( model.getList() ) );

      list.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane( list ), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
      list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      new RefreshWhenVisible( this, model );
   }

   @Action
   public Task add()
   {
      final SelectLinkDialog dialog = labelsDialogs.use( model.getPossible() ).newInstance();
      dialog.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_resolution_title ) );

      if (dialog.getSelectedLinks() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.add( dialog.getSelectedLinks() );
            }
         };
      } else
         return null;
   }

   @Action
   public void remove()
   {
      LinkValue selected = (LinkValue) list.getSelectedValue();
      model.remove( selected );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );
   }
}