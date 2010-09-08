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

import ca.odell.glazedlists.EventList;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.GroupedList;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.PageListItemValue;
import se.streamsource.streamflow.util.Strings;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListModel;
import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class FieldsView
      extends JPanel
      implements Observer
{
   private GroupedList fieldList;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> pageCreationDialog;

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
      model.addObserver( this );

      setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      final ActionMap am = context.getActionMap( this );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "addPage" ) ) );
      toolbar.add( new JButton( am.get( "addField" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      toolbar.add( new JButton( am.get( "up" ) ) );
      toolbar.add( new JButton( am.get( "down" ) ) );

      model.refresh();
      EventList<ListItemValue> pagesAndFields = model.getPagesAndFieldsList();

      fieldList = new GroupedList();
      fieldList.setEventList( pagesAndFields );

      JPanel titlePanel = new JPanel( new BorderLayout() );
      titlePanel.add( new JSeparator(), BorderLayout.NORTH );
      titlePanel.add( new JLabel( i18n.text( AdministrationResources.fields_label ) ), BorderLayout.CENTER );

      add( titlePanel, BorderLayout.NORTH );
      add( fieldList, BorderLayout.CENTER );
      add( toolbar, BorderLayout.SOUTH );

      fieldList.getList().getSelectionModel().addListSelectionListener(
            new SelectionActionEnabler( am.get( "addField" ), am.get( "remove" ) ) );
      fieldList.getList().getSelectionModel().addListSelectionListener(
            new SelectionActionEnabler( am.get( "up" ), am.get( "down" ) )
            {

               @Override
               public boolean isSelectedValueValid( Action action )
               {
                  boolean result = true;
                  try
                  {
                     JXList list = fieldList.getList();
                     int selectedIndex = list.getSelectedIndex();
                     Object selected = list.getModel().getElementAt( list.convertIndexToModel( selectedIndex ) );


                     if (action.equals( am.get( "up" ) ))
                     {
                        if (selected instanceof PageListItemValue)
                        {
                           if (selectedIndex == 0)
                              result = false;
                        } else
                        {
                           if (list.getModel().getElementAt( selectedIndex - 1 ) instanceof PageListItemValue)
                              result = false;
                        }
                     } else
                     {
                        if (selected instanceof PageListItemValue)
                        {
                           if (selectedIndex == lastPageIndex())
                              result = false;
                        } else
                        {
                           if (selectedIndex == list.getModel().getSize() - 1 ||
                                 list.getModel().getElementAt( selectedIndex + 1 ) instanceof PageListItemValue)
                              result = false;
                        }
                     }
                  } catch (IndexOutOfBoundsException e)
                  {
                     // TODO is there a way to fix the glazedlists outofbounds exception due to concurrent update other than to consume the exception
                     // tried with wrapping the BasicEventList into GlazedLists.threadSafeList( eventlist ) to no avail!!
                     // The problem appears on adding and removing elements causing a server refresh that calls clear and addAll on the event list
                     // resulting in an invalid selection index.
                     result = false;
                  }
                  return result;
               }

               private int lastPageIndex()
               {
                  int lastIndex = -1;
                  ListModel listModel = fieldList.getList().getModel();
                  for (int i = 0; i < listModel.getSize(); i++)
                  {
                     if (listModel.getElementAt( i ) instanceof PageListItemValue)
                        lastIndex = i;
                  }
                  return lastIndex;
               }
            } );

   }

   @org.jdesktop.application.Action
   public void addField()
   {
      FieldCreationDialog dialog = fieldCreationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( AdministrationResources.add_field_to_form ) );

      if (dialog.name() != null && !"".equals( dialog.name() ))
      {
         PageListItemValue page = findSelectedPage( fieldList.getList().getSelectedValue() );
         if (page != null)
         {
            fieldList.getList().clearSelection();
            model.addField( page.entity().get(), dialog.name(), dialog.getFieldType() );
         }
      }
   }

   private PageListItemValue findSelectedPage( Object selected )
   {
      ListModel model = fieldList.getList().getModel();
      if (selected instanceof PageListItemValue)
      {
         return (PageListItemValue) selected;
      } else
      {
         int index = fieldList.getList().getSelectedIndex();
         for (int i = index; i >= 0; i--)
         {
            if (model.getElementAt( i ) instanceof PageListItemValue)
            {
               return (PageListItemValue) model.getElementAt( i );
            }
         }
      }
      return null;
   }

   @org.jdesktop.application.Action
   public void addPage()
   {
      NameDialog dialog = pageCreationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( AdministrationResources.add_page_title ) );

      if (Strings.notEmpty( dialog.name() ))
      {
         fieldList.getList().clearSelection();
         model.addPage( dialog.name() );
      }
   }


   @org.jdesktop.application.Action
   public void remove()
   {
      int index = fieldList.getList().getSelectedIndex();
      if (index != -1)
      {
         ListItemValue selected = (ListItemValue) fieldList.getList().getSelectedValue();

         ConfirmationDialog dialog = confirmationDialog.iterator().next();
         dialog.setRemovalMessage( selected.description().get() );
         dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
         if (dialog.isConfirmed())
         {
            if (selected instanceof PageListItemValue)
            {
               model.removePage( selected.entity().get() );
            } else
            {
               model.removeField( selected.entity().get() );
            }
         }
      }
   }

   @org.jdesktop.application.Action
   public void up()
   {
      ListItemValue selected = (ListItemValue) fieldList.getList().getSelectedValue();
      if (selected instanceof PageListItemValue)
      {
         model.movePage( selected.entity().get(), "up" );
      } else
      {
         model.moveField( selected.entity().get(), "up" );
      }
   }

   @org.jdesktop.application.Action
   public void down()
   {
      int index = fieldList.getList().getSelectedIndex();
      if (index != -1)
      {
         ListItemValue selected = (ListItemValue) fieldList.getList().getSelectedValue();
         if (selected instanceof PageListItemValue)
         {
            model.movePage( selected.entity().get(), "down" );
         } else
         {
            model.moveField( selected.entity().get(), "down" );
         }
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

   public void update( Observable o, Object arg )
   {
      fieldList.getList().clearSelection();
      fieldList.getList().setSelectedValue( arg, true );
   }
}