/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
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
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.FormElementsList;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.form.FormElementItem;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.PageListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.util.Iterables;
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

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;
import static se.streamsource.streamflow.util.Iterables.*;

/**
 * JAVADOC
 */
public class FormElementsView
      extends JPanel
      implements Observer, TransactionListener
{
   private FormElementsList fieldList;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> pageCreationDialog;

   @Uses
   Iterable<FieldCreationDialog> fieldCreationDialog;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   private FormElementsModel model;

   public FormElementsView( @Service ApplicationContext context,
                      @Uses FormElementsModel model )
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

      EventList<FormElementItem> formElementsList = model.getFormElementsList();

      fieldList = new FormElementsList();
      fieldList.setEventList( formElementsList );

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
                     FormElementItem formElementItem = (FormElementItem) list.getSelectedValue();

                     if (action.equals( am.get( "up" ) ))
                     {
                        if (formElementItem.getRelation().equals("page"))
                        {
                           if (selectedIndex == 0)
                              result = false;
                        } else if (formElementItem.getRelation().equals("field"))
                        {
                           FormElementItem previousItem = (FormElementItem) list.getModel().getElementAt( selectedIndex - 1 );
                           if (previousItem.getRelation().equals("page"))
                              result = false;
                        }
                     } else if (action.equals( am.get( "down" ) ))
                     {
                        if (formElementItem.getRelation().equals("page"))
                        {
                           if (selectedIndex == lastPageIndex())
                              result = false;
                        } else
                        {
                           if (selectedIndex == list.getModel().getSize() - 1 ||
                                 ((FormElementItem)list.getModel().getElementAt( selectedIndex + 1 )).getRelation().equals("page"))
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

      new RefreshWhenVisible(this, model);

   }

   @org.jdesktop.application.Action
   public Task addField()
   {
      final FieldCreationDialog dialog = fieldCreationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( AdministrationResources.add_field_to_form ) );

      if (dialog.name() != null && !"".equals( dialog.name() ))
      {
         final FormElementItem page = findSelectedPage( (FormElementItem) fieldList.getList().getSelectedValue() );
         if (page != null)
         {
            fieldList.getList().clearSelection();
            return new CommandTask()
            {
               @Override
               public void command()
                  throws Exception
               {
                  model.addField( page, dialog.name(), dialog.getFieldType() );
               }
            };
         }
      }

      return null;
   }

   private FormElementItem findSelectedPage( FormElementItem selected )
   {
      ListModel model = fieldList.getList().getModel();
      if (selected.getRelation().equals("page"))
      {
         return selected;
      } else
      {
         int index = fieldList.getList().getSelectedIndex();
         for (int i = index; i >= 0; i--)
         {
            if (((FormElementItem)model.getElementAt( i )).getRelation().equals("page"))
            {
               return (FormElementItem) model.getElementAt( i );
            }
         }
      }
      return null;
   }

   @org.jdesktop.application.Action
   public Task addPage()
   {
      final NameDialog dialog = pageCreationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( AdministrationResources.add_page_title ) );

      if (Strings.notEmpty( dialog.name() ))
      {
         fieldList.getList().clearSelection();
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.addPage( dialog.name() );
            }
         };
      } else
         return null;
   }


   @org.jdesktop.application.Action
   public Task remove()
   {
      final FormElementItem selected = (FormElementItem) fieldList.getList().getSelectedValue();
      if (selected != null)
      {
         ConfirmationDialog dialog = confirmationDialog.iterator().next();
         dialog.setRemovalMessage( selected.getName());
         dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
         if (dialog.isConfirmed())
         {
            return new CommandTask()
            {
               @Override
               public void command()
                  throws Exception
               {
                  model.removeFormElement(selected );
               }
            };
         }
      }

      return null;
   }

   @org.jdesktop.application.Action
   public Task up()
   {
      final FormElementItem selected = (FormElementItem) fieldList.getList().getSelectedValue();
      return new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            model.move( selected, "up" );
         }
      };
   }

   @org.jdesktop.application.Action
   public Task down()
   {
      final FormElementItem selected = (FormElementItem) fieldList.getList().getSelectedValue();
      if (selected != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.move( selected, "down" );
            }
         };
      } else
         return null;
   }

   public FormElementsList getFieldList()
   {
      return fieldList;
   }

   public FormElementsModel getModel()
   {
      return model;
   }

   public void update( Observable o, Object arg )
   {
      fieldList.getList().clearSelection();
      fieldList.getList().setSelectedValue( arg, true );
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( transactions, withNames("changedDescription", "removedField", "movedField" )))
         model.refresh();

      DomainEvent event = first( filter( events(transactions ), withNames("createdField", "createdPage", "movedField")));
      if (event != null)
      {
         String id = EventParameters.getParameter( event, 1 );
         for (FormElementItem formElementItem : model.getFormElementsList())
         {
            if (formElementItem.getClient().getReference().getPath().endsWith(id+"/"))
            {
               fieldList.getList().setSelectedValue( formElementItem, true );
               break;
            }
         }
      }
   }
}