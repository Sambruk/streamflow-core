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

package se.streamsource.streamflow.client.ui.administration.labels;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */
public class SelectedLabelsView
      extends JPanel
      implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private JList labelList;

   private SelectedLabelsModel modelSelected;

   public SelectedLabelsView( @Service ApplicationContext context,
                              @Uses SelectedLabelsModel model )
   {
      super( new BorderLayout() );
      this.modelSelected = model;
      setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      labelList = new JList( new EventListModel<LinkValue>( modelSelected.getList() ) );

      labelList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane( labelList ), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
      labelList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      new RefreshWhenShowing( this, modelSelected );
   }

   @Action
   public Task add()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use( modelSelected.getPossible() ).newInstance();
      dialog.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_label_title ) );

      if (dialog.getSelectedLinks() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               modelSelected.add( dialog.getSelectedLinks() );
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final Iterable<LinkValue> selected = (Iterable) Iterables.iterable(labelList.getSelectedValues());
      
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            for (LinkValue linkValue : selected)
            {
               modelSelected.remove( linkValue );
            }
         }
      };
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      modelSelected.notifyTransactions( transactions );
   }
}