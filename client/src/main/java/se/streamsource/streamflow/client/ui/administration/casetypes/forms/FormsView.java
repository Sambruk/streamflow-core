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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

import java.awt.BorderLayout;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.label.SelectionDialog;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;

/**
 * JAVADOC
 */
public class FormsView
      extends JPanel
{
   public JList formList;
   private FormsModel model;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   ObjectBuilder<SelectionDialog> possibleMoveToDialogs;

   @Structure
   ObjectBuilderFactory obf;

   public FormsView( @Service ApplicationContext context,
                     @Uses FormsModel model )
   {
      super( new BorderLayout() );
      this.model = model;
      model.refresh();
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      formList = new JList( new EventListModel<LinkValue>(model.getForms()) );

      formList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane(formList), BorderLayout.CENTER );

      JPopupMenu optionsPopup = new JPopupMenu();
      optionsPopup.add( am.get( "move" ) );
      optionsPopup.add( am.get( "remove" ) );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( new OptionsAction(optionsPopup) ));
      add( toolbar, BorderLayout.SOUTH );
      formList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ), am.get( "move" ) ) );

      addAncestorListener( new RefreshWhenVisible( model, this ) );

   }

   @Action
   public void add()
   {
      NameDialog formDialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, formDialog, i18n.text( AdministrationResources.create_new_form ) );

      String name = formDialog.name();
      if (name != null && !"".equals( name ))
      {
         model.createForm( name );
         model.refresh();
      }
   }

   @Action
   public void remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamFlowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         LinkValue selected = (LinkValue) formList.getSelectedValue();
         if (selected != null)
         {
            model.removeForm( selected );
            model.formModels.remove( selected.id().get() );
            formList.clearSelection();
         }
      }
   }

   @Action
   public void move()
   {
      LinkValue selected = (LinkValue) formList.getSelectedValue();
      FormModel formModel = model.getFormModel( selected.id().get() );
      SelectionDialog dialog = possibleMoveToDialogs.use(formModel.getPossibleMoveTo()).newInstance();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_move_to ) );

      if (dialog.getSelectedLinks() != null)
      {
         for (LinkValue linkValue : dialog.getSelectedLinks())
         {
            formModel.moveForm( linkValue );
            model.refresh();
         }
      }
   }

   public JList getFormList()
   {
      return formList;
   }

   public FormsModel getModel()
   {
      return model;
   }
}