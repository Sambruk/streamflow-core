/*
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
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.FormElementItemListCellRenderer;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.util.Strings;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;
import static se.streamsource.streamflow.util.Iterables.*;

/**
 * JAVADOC
 */
public class FormElementsView
      extends JSplitPane
      implements TransactionListener

{
   @Service
   private DialogService dialogs;

   @Uses
   private Iterable<NameDialog> pageCreationDialog;

   @Uses
   private Iterable<FieldCreationDialog> fieldCreationDialog;

   @Uses
   private Iterable<ConfirmationDialog> confirmationDialog;

   private JList list;

   private FormElementsModel model;


   public FormElementsView( @Service ApplicationContext context,
                            @Uses final CommandQueryClient client,
                            @Structure final ObjectBuilderFactory obf)
   {
      this.model = obf.newObjectBuilder( FormElementsModel.class ).use( client).newInstance();

      final ActionMap am = context.getActionMap( this );

      setBorder( Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      setRightComponent( new JPanel() );
      setBorder( BorderFactory.createEmptyBorder() );

      setDividerLocation( 350 );
      setOneTouchExpandable( true );


      initMaster( new EventListModel<LinkValue>( model.getList() ),
            new DetailFactory() {
               public Component createDetail( LinkValue detailLink )
               {
                  if ( detailLink == null ) return new JPanel();
                  LinkValue link = getSelectedValue();
                  if (link.rel().get().equals("page"))
                  {
                     return obf.newObjectBuilder( PageEditView.class ).use( client.getClient( link ) ).newInstance();
                  } else
                     return obf.newObjectBuilder( FieldEditView.class ).use( client.getClient( link )).newInstance();
               }
            },
            am.get( "addPage" ), am.get( "addField" ), am.get( "remove" ), am.get( "up" ), am.get( "down" ));


      list.setCellRenderer( new FormElementItemListCellRenderer() );
      list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get("addField"), am.get("remove")) );
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

      if ( Strings.notEmpty( dialog.name() ) )
      {
         final LinkValue page = findSelectedPage(  getSelectedValue() );
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

      return null;
   }

   @Structure
   ValueBuilderFactory vbf;

   private LinkValue findSelectedPage( LinkValue selected )
   {
      if (selected.rel().get().equals("page"))
      {
         return selected;
      } else
      {
         int i1 = selected.href().get().indexOf( selected.id().get() );
         ValueBuilder<LinkValue> builder = vbf.newValueBuilder( LinkValue.class ).withPrototype( selected );
         builder.prototype().href().set( selected.href().get().substring( 0, i1 ));
         return builder.newInstance();
      }
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
                  model.remove( selected );
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

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( withNames("changedDescription", "removedField", "movedField" ), transactions ))
         model.refresh();

      DomainEvent event = first( filter( withNames("createdField", "createdPage", "movedField", "movedPage"), events(transactions ) ));
      if (event != null)
      {
         String id = EventParameters.getParameter( event, 1 );
         for (LinkValue link : model.getList())
         {
            if (link.href().get().endsWith( id+"/" ))
            {
               list.setSelectedValue( link, true );
               break;
            }
         }
      }
   }

   protected void initMaster( EventListModel<LinkValue> listModel, final DetailFactory factory, Action... actions)
   {
      list = new JList(listModel);
      list.setCellRenderer( new LinkListCellRenderer() );

      JScrollPane scrollPane = new JScrollPane( list );

      JPanel master = new JPanel(new BorderLayout());
      master.add( scrollPane, BorderLayout.CENTER );

      // Toolbar
      JPanel toolbar = new JPanel();
      for (Action action : actions)
      {
         toolbar.add( new JButton( action ) );
      }

      master.add( toolbar, BorderLayout.SOUTH);

      setLeftComponent( master );

      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               setRightComponent( factory.createDetail( getSelectedValue() ));
            }
         }
      } );
   }

   public interface DetailFactory
   {
      Component createDetail(LinkValue detailLink);
   }

   private LinkValue getSelectedValue()
   {
      return (LinkValue) list.getSelectedValue();
   }
}