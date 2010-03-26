/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration.form;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.label.GroupedSelectionDialog;
import se.streamsource.streamflow.client.ui.administration.label.SelectionDialog;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.*;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

/**
 * JAVADOC
 */
public class SelectedFormsView
      extends JPanel
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   ObjectBuilder<GroupedSelectionDialog> selectDialogs;

   public JList labelList;

   private SelectedFormsModel modelSelected;

   public SelectedFormsView( @Service ApplicationContext context, @Uses SelectedFormsModel modelSelected )
   {
      super( new BorderLayout() );
      this.modelSelected = modelSelected;

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      labelList = new JList( new EventListModel<LinkValue>( new SortedList<LinkValue>( modelSelected.getEventList(), new LinkComparator() ) ) );

      labelList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane( labelList ), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
      labelList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      addAncestorListener( new RefreshWhenVisible( modelSelected, this ) );
   }

   @Action
   public void add()
   {
      GroupedSelectionDialog dialog = selectDialogs.use( modelSelected.getPossible() ).newInstance();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_form_title ) );

      if (dialog.getSelectedLinks() != null)
      {
         for (LinkValue linkValue : dialog.getSelectedLinks())
         {
            modelSelected.add( linkValue);
         }
         modelSelected.refresh();
      }

   }

   @Action
   public void remove()
   {
      LinkValue selected = (LinkValue) labelList.getSelectedValue();
      modelSelected.remove( selected );
      modelSelected.refresh();
   }
}