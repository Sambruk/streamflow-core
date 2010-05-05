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

package se.streamsource.streamflow.client.ui.administration.casetypes;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;

import com.jgoodies.forms.factories.Borders;

import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

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
public class SelectedCaseTypesView
      extends JPanel
{
   @Service
   DialogService dialogs;

   @Uses
   ObjectBuilder<SelectCaseTypesDialog> caseTypesDialogs;

   public JList caseTypeList;

   private SelectedCaseTypesModel modelSelected;

   public SelectedCaseTypesView( @Service ApplicationContext context, @Uses SelectedCaseTypesModel modelSelected )
   {
      super( new BorderLayout() );
      this.modelSelected = modelSelected;
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      caseTypeList = new JList( new EventListModel<LinkValue>( new SortedList<LinkValue>( modelSelected.getCaseTypeList(), new LinkComparator() ) ) );

      caseTypeList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane( caseTypeList ), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
      caseTypeList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      addAncestorListener( new RefreshWhenVisible( modelSelected, this ) );
   }

   @Action
   public void add()
   {
      SelectCaseTypesDialog dialog = caseTypesDialogs.use( modelSelected.getPossibleCaseTypes() ).newInstance();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_casetypes_title ) );

      if (dialog.getSelectedCaseTypes() != null)
      {
         for (LinkValue linkValue : dialog.getSelectedCaseTypes())
         {
            modelSelected.addCaseType( linkValue );
         }
         modelSelected.refresh();
      }

   }

   @Action
   public void remove()
   {
      LinkValue selected = (LinkValue) caseTypeList.getSelectedValue();
      modelSelected.removeCaseType( EntityReference.parseEntityReference( selected.id().get()) );
      modelSelected.refresh();
   }
}