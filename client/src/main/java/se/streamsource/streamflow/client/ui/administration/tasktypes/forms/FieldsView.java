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

package se.streamsource.streamflow.client.ui.administration.tasktypes.forms;

import java.awt.BorderLayout;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.GroupedList;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.PageListItemValue;
import se.streamsource.streamflow.domain.form.FieldTypes;
import ca.odell.glazedlists.EventList;

/**
 * JAVADOC
 */
public class FieldsView
      extends JPanel
{
   private GroupedList fieldList;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<FieldCreationDialog> fieldCreationDialog;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   private FieldsModel model;

   public FieldsView( @Service ApplicationContext context,
                      @Uses FieldsModel model )
   {
      super( new BorderLayout() );
      this.model = model;
      JScrollPane scrollPanel = new JScrollPane();
      ActionMap am = context.getActionMap( this );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      toolbar.add( new JButton( am.get( "up" ) ) );
      toolbar.add( new JButton( am.get( "down" ) ) );

      model.refresh();
      EventList<ListItemValue> pagesAndFields = model.getPagesAndFieldsList();

      fieldList = new GroupedList();
      fieldList.setEventList( pagesAndFields );

      scrollPanel.setViewportView( fieldList );

      JPanel titlePanel = new JPanel( new BorderLayout() );
      titlePanel.add( new JSeparator(), BorderLayout.NORTH );
      titlePanel.add( new JLabel( i18n.text( AdministrationResources.fields_label ) ), BorderLayout.CENTER );

      add( titlePanel, BorderLayout.NORTH );
      add( scrollPanel, BorderLayout.CENTER );
      add( toolbar, BorderLayout.SOUTH );


      fieldList.getList().getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );
      fieldList.getList().getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "up" ) ) );
      fieldList.getList().getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "down" ) ) );
   }

   @org.jdesktop.application.Action
   public void add()
   {
      FieldCreationDialog dialog = fieldCreationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text(AdministrationResources.add_field_to_form));

      if (dialog.name() != null && !"".equals( dialog.name() ))
      {
         if ( FieldTypes.page_break.equals( dialog.getFieldType() ))
         {
            model.addPage( dialog.name() );
            fieldList.getList().clearSelection();
         } else
         {
            ListItemValue selected = (ListItemValue) fieldList.getList().getSelectedValue();
            if ( selected != null)
            {
               if ( selected instanceof PageListItemValue)
               {
                  model.addField( selected.entity().get(), dialog.name(), dialog.getFieldType() );
                  fieldList.getList().clearSelection();
               } else
               {
                  // show help message
               }
            }
         }
      }
   }


   @org.jdesktop.application.Action
   public void remove()
   {
      int index = fieldList.getList().getSelectedIndex();
      if (index != -1)
      {
         ConfirmationDialog dialog = confirmationDialog.iterator().next();
         dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamFlowResources.confirmation ) );
         if (dialog.isConfirmed())
         {

            ListItemValue selected = (ListItemValue) fieldList.getList().getSelectedValue();
            if ( selected instanceof PageListItemValue)
            {
               model.removePage( selected.entity().get() );
            } else
            {
               model.removeField( selected.entity().get() );
            }
            fieldList.getList().clearSelection();
         }
      }
   }

   @org.jdesktop.application.Action
   public void up()
   {
      int index = fieldList.getList().getSelectedIndex();
      if (index != -1)
      {
         ListItemValue selected = (ListItemValue) fieldList.getList().getSelectedValue();
         if ( selected instanceof PageListItemValue)
         {
            model.movePage( selected.entity().get(), "up" );
         } else
         {
            model.moveField( selected.entity().get(), "up" );
         }
         fieldList.getList().setSelectedIndex( index );
      }
   }

   @org.jdesktop.application.Action
   public void down()
   {
      int index = fieldList.getList().getSelectedIndex();
      if (index != -1)
      {
         ListItemValue selected = (ListItemValue) fieldList.getList().getSelectedValue();
         if ( selected instanceof PageListItemValue)
         {
            model.movePage( selected.entity().get(), "down" );
         } else
         {
            model.moveField( selected.entity().get(), "down" );
         }
         fieldList.getList().setSelectedIndex( index );
      }
   }

   public GroupedList getFieldList()
   {
      return fieldList;
   }

   public FieldsModel getModel()
   {
      return model;
   }
}