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

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.FormElementItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.ListDetailView;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.util.Strings;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ListModel;
import java.awt.Component;
import java.util.Observable;

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;
import static se.streamsource.streamflow.util.Iterables.*;

/**
 * JAVADOC
 */
public class FormElementsView
      extends ListDetailView
{
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
                            @Uses final CommandQueryClient client,
                            @Structure final ObjectBuilderFactory obf)
   {
      this.model = obf.newObjectBuilder( FormElementsModel.class ).use( client).newInstance();

      final ActionMap am = context.getActionMap( this );

      initMaster( new EventListModel<LinkValue>( model.getFormElementsList()), am.get( "addPage" ),
            new Action[]{ am.get( "addField" ), am.get( "remove" ), am.get( "up" ), am.get( "down" ) },
            new DetailFactory() {
               public Component createDetail( LinkValue detailLink )
               {
                  LinkValue link = getSelectedValue();
                  if (link.rel().get().equals("page"))
                  {
                     return obf.newObjectBuilder( PageEditView.class ).use( client.getClient( link ) ).newInstance();
                  } else
                     return obf.newObjectBuilder( FieldEditView.class ).use( client.getClient( link )).newInstance();
                  }
            });


      list.setCellRenderer( new FormElementItemListCellRenderer() );
      list.getSelectionModel().addListSelectionListener(
            new SelectionActionEnabler( am.get( "up" ), am.get( "down" ) )
            {

               @Override
               public boolean isSelectedValueValid( Action action )
               {
                  boolean result = true;
                  try
                  {
                     int selectedIndex = list.getSelectedIndex();
                     LinkValue link = (LinkValue) list.getSelectedValue();

                     if (action.equals( am.get( "up" ) ))
                     {
                        if (link.rel().get().equals("page"))
                        {
                           if (selectedIndex == 0)
                              result = false;
                        } else if (link.rel().get().equals("field"))
                        {
                           LinkValue previous = (LinkValue) list.getModel().getElementAt( selectedIndex - 1 );
                           if (previous.rel().get().equals("page"))
                              result = false;
                        }
                     } else if (action.equals( am.get( "down" ) ))
                     {
                        if (link.rel().get().equals("page"))
                        {
                           if (selectedIndex == lastPageIndex())
                              result = false;
                        } else
                        {
                           if (selectedIndex == list.getModel().getSize() - 1 ||
                                 ((LinkValue)list.getModel().getElementAt( selectedIndex + 1 )).rel().get().equals("page"))
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
                  ListModel listModel = list.getModel();
                  for (int i = 0; i < listModel.getSize(); i++)
                  {
                     LinkValue link = (LinkValue) listModel.getElementAt( i );
                     if ( link.rel().get().equals( "page" ) )
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
         final LinkValue page = findSelectedPage( (LinkValue) list.getSelectedValue() );
         if (page != null)
         {
            list.clearSelection();
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

   private LinkValue findSelectedPage( LinkValue selected )
   {
      ListModel model = list.getModel();
      if (selected.rel().get().equals("page"))
      {
         return selected;
      } else
      {
         int index = list.getSelectedIndex();
         for (int i = index; i >= 0; i--)
         {
            if (((LinkValue)model.getElementAt( i )).rel().get().equals("page"))
            {
               return (LinkValue) model.getElementAt( i );
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
         list.clearSelection();
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
      final LinkValue selected = getSelectedValue();
      if (selected != null)
      {
         ConfirmationDialog dialog = confirmationDialog.iterator().next();
         dialog.setRemovalMessage( selected.text().get() );
         dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
         if (dialog.isConfirmed())
         {
            return new CommandTask()
            {
               @Override
               public void command()
                  throws Exception
               {
                  model.removeFormElement( selected );
               }
            };
         }
      }

      return null;
   }

   @org.jdesktop.application.Action
   public Task up()
   {
      final LinkValue selected = getSelectedValue();
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
      final LinkValue selected = getSelectedValue();
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

   public void update( Observable o, Object arg )
   {
      list.clearSelection();
      list.setSelectedValue( arg, true );
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( transactions, withNames("changedDescription", "removedField", "movedField" )))
         model.refresh();

      DomainEvent event = first( filter( events(transactions ), withNames("createdField", "createdPage", "movedField")));
      if (event != null)
      {
         String id = EventParameters.getParameter( event, 1 );
         for (LinkValue link : model.getFormElementsList())
         {
            if (link.href().get().endsWith( id+"/" ))
            {
               list.setSelectedValue( link, true );
               break;
            }
         }
      }
   }
}