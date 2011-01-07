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

package se.streamsource.streamflow.client.ui.workspace.search;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */
public class SearchView
      extends JPanel
   implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   @Uses
   Iterable<SaveSearchDialog> saveSearchDialogs;

   @Uses
   protected ObjectBuilder<HandleSearchesDialog> handleSearchesDialogs;

//   private JComboBox searches;
   private JTextField searchField;
   private SavedSearchesModel model;

   // Search term helpers
   private JComboBox status;

   private JComboBox labels;
   private EventList<LinkValue> labelList = new BasicEventList<LinkValue>(  );

   private JComboBox assignee;
   private EventList<LinkValue> assigneeList = new BasicEventList<LinkValue>(  );

   private JComboBox project;
   private EventList<LinkValue> projectList = new BasicEventList<LinkValue>(  );

   private JButton createdOn;
   private JXMonthView createdOnPicker = new JXMonthView();

   private final SearchResultTableModel searchResultTableModel;

   private JPopupMenu searchAddmenu;

   private JPanel search;

   public SearchView( @Service ApplicationContext context, @Uses final CommandQueryClient client, @Uses SearchResultTableModel searchResultTableModel, @Structure ObjectBuilderFactory obf )
   {
      setLayout( new BoxLayout(this, BoxLayout.X_AXIS) );
      this.searchResultTableModel = searchResultTableModel;
      setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );
      setBorder( BorderFactory.createEtchedBorder());

      ActionMap am;
      setActionMap( am = context.getActionMap( this ) );

      model = obf.newObjectBuilder( SavedSearchesModel.class ).use(client.getSubClient( "savedsearches" )).newInstance();

/*
      searches = new JComboBox(new EventComboBoxModel<LinkValue>( model.getList() ) );
      searches.setMaximumSize( new Dimension( 200, (int) searches.getPreferredSize().getHeight() ) );
      searches.setEditable( true );
      searches.setMaximumRowCount( 10 );
      searches.setRenderer( new SavedSearchListCellRenderer() );
      searches.setEditor( new SearchComboEditor() );
*/
      searchField = new JTextField(20);
      searchField.addActionListener( am.get( "search" ) );


      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "saveSearch" ) );
      options.add( am.get( "handle" ) );

      search = new JPanel(new WrapLayout(FlowLayout.LEFT));
      search.add( new JLabel( "Search:" ) );
      search.add(searchField);
      search.add( Box.createHorizontalStrut( 10 ) );
      search.add( new JButton( am.get( "add" ) ) );

      {
         Box searchBox = Box.createHorizontalBox();
         JLabel label = new JLabel( i18n.text( WorkspaceResources.status ) );
         status = new JComboBox(new String[]{CaseStates.OPEN.name(), CaseStates.ON_HOLD.name(), CaseStates.CLOSED.name()});
         label.setForeground( Color.gray );
         searchBox.add( label );
         searchBox.add( status );
         searchBox.setVisible( false );
         search.add(searchBox);
      }

      {
         Box searchBox = Box.createHorizontalBox();
         JLabel label = new JLabel( i18n.text( WorkspaceResources.label ) );
         labels = new JComboBox(new EventComboBoxModel<LinkValue>(labelList));
         labels.setPreferredSize( new Dimension( 150, (int) labels.getPreferredSize().getHeight() ) );
         labels.addPopupMenuListener( new PopupMenuListener()
         {
            public void popupMenuWillBecomeVisible( PopupMenuEvent e )
            {
               if (assignee.getModel().getSize() == 0)
               {

                  List<LinkValue> possibleLabels = client.query( "possiblelabels", LinksValue.class ).links().get();
                  EventListSynch.synchronize( possibleLabels, labelList );
               }
            }

            public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
            {
            }

            public void popupMenuCanceled( PopupMenuEvent e )
            {
            }
         } );
         labels.setRenderer( new LinkListCellRenderer() );
         label.setForeground( Color.gray );
         searchBox.add( label );
         searchBox.add( labels );
         searchBox.setVisible( false );
         search.add(searchBox);
      }

      {
         Box searchBox = Box.createHorizontalBox();
         JLabel label = new JLabel( i18n.text( WorkspaceResources.assignee ) );
         assignee = new JComboBox(new EventComboBoxModel<LinkValue>(assigneeList));
         assignee.setPreferredSize( new Dimension( 150, (int) assignee.getPreferredSize().getHeight() ) );
         assignee.addPopupMenuListener( new PopupMenuListener()
         {
            public void popupMenuWillBecomeVisible( PopupMenuEvent e )
            {
               if (assignee.getModel().getSize() == 0)
               {

                  List<LinkValue> possibleassignees = client.query( "possibleassignees", LinksValue.class ).links().get();
                  EventListSynch.synchronize( possibleassignees, assigneeList );
               }
            }

            public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
            {
            }

            public void popupMenuCanceled( PopupMenuEvent e )
            {
            }
         } );
         assignee.setRenderer( new LinkListCellRenderer() );
         label.setForeground( Color.gray );
         searchBox.add( label );
         searchBox.add( assignee );
         searchBox.setVisible( false );
         search.add(searchBox);
      }

      {
         Box searchBox = Box.createHorizontalBox();
         JLabel label = new JLabel( i18n.text( WorkspaceResources.project ) );
         project = new JComboBox(new EventComboBoxModel<LinkValue>(projectList));
         project.setPreferredSize( new Dimension( 150, (int) project.getPreferredSize().getHeight() ) );
         project.addPopupMenuListener( new PopupMenuListener()
         {
            public void popupMenuWillBecomeVisible( PopupMenuEvent e )
            {
               if (project.getModel().getSize() == 0)
               {
                  EventListSynch.synchronize( client.query( "possibleprojects", LinksValue.class ).links().get(), projectList );
               }
            }

            public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
            {
            }

            public void popupMenuCanceled( PopupMenuEvent e )
            {
            }
         } );
         project.setRenderer( new LinkListCellRenderer() );
         label.setForeground( Color.gray );
         searchBox.add( label );
         searchBox.add( project );
         searchBox.setVisible( false );
         search.add(searchBox);
      }

      {
         Box searchBox = Box.createHorizontalBox();
         JLabel label = new JLabel( i18n.text( WorkspaceResources.created_on ) );
         createdOn = new JButton("Choose date");
         createdOn.addActionListener( am.get( "createdOn" ) );
         createdOnPicker.setFirstDayOfWeek( Calendar.MONDAY );
         createdOnPicker.setTraversable( true );
         createdOnPicker.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
         createdOnPicker.setSelectionMode( DateSelectionModel.SelectionMode.SINGLE_INTERVAL_SELECTION );
         createdOnPicker.setPreferredColumnCount( 2 );
         createdOnPicker.setPreferredRowCount( 3 );
         Calendar firstMonth = Calendar.getInstance();
         firstMonth.add( Calendar.MONTH, -5 );
         createdOnPicker.setFirstDisplayedDay( firstMonth.getTime() );
         createdOnPicker.setTodayBackground( Color.gray );
         label.setForeground( Color.gray );
         searchBox.add( label );
         searchBox.add( createdOn );
         searchBox.setVisible( false );
         search.add(searchBox);
      }

      add(search);

//      add(Box.createHorizontalGlue());

//      add( new JButton( new OptionsAction( options ) ));

      searchAddmenu = new JPopupMenu();
      addSearch( "Status" );
      addSearch( "Labels" );
      addSearch( "Assignees" );
      addSearch( "Projects" );
      addSearch( "Created on" );

      new RefreshWhenShowing( this, model);
      new RefreshWhenShowing( this, new Refreshable()
      {
         public void refresh()
         {
            getTextField().requestFocusInWindow();
         }
      });

   }

   public JTextField getTextField()
   {
      return (JTextField) searchField;
   }

   private void addSearch( String name )
   {
      JCheckBoxMenuItem status = new JCheckBoxMenuItem( name );
      status.addActionListener( getActionMap().get( "showSearch" ) );
      searchAddmenu.add( status );
   }

   @org.jdesktop.application.Action
   public void createdOn(ActionEvent event)
   {
      dialogs.showOkDialog( (Component) event.getSource(), createdOnPicker);
      if (!createdOnPicker.getSelection().isEmpty())
      {
         DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
         format.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

         if (createdOnPicker.getSelection().size() == 1)
         {
            createdOn.setText( format.format(createdOnPicker.getSelection().first()) );
         } else
         {
            createdOn.setText( format.format(createdOnPicker.getSelection().first())+" - "+ format.format(createdOnPicker.getSelection().last()));
         }
      }

   }

   @org.jdesktop.application.Action
   public void showSearch()
   {
      for (int idx = 0; idx < searchAddmenu.getComponents().length; idx++)
      {
         Component component = searchAddmenu.getComponents()[idx];
         JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) component;
         search.getComponent( idx+4 ).setVisible( checkBox.isSelected() );
      }
   }

   @org.jdesktop.application.Action
   public void add(ActionEvent event)
   {
      Component source = (Component) event.getSource();
      searchAddmenu.show( source, 0, source.getHeight() );
   }

   @Action
   public void search()
   {
      String searchString = getTextField().getText();

      if (searchString.length() > 500)
      {
         dialogs.showMessageDialog( this, i18n.text( WorkspaceResources.too_long_query), "" );
      } else
      {
         if (status.isShowing() && status.getSelectedIndex() != -1)
         {
            searchString+=" status:"+status.getSelectedItem().toString();
         }

         if (labels.isShowing() && labels.getSelectedIndex() != -1)
         {
            searchString+=" label:\""+((LinkValue)labels.getSelectedItem()).text().get()+"\"";
         }

         if (assignee.isShowing() && assignee.getSelectedIndex() != -1)
         {
            searchString+=" assignedTo:"+((LinkValue)assignee.getSelectedItem()).id().get();
         }

         if (project.isShowing() && project.getSelectedIndex() != -1)
         {
            searchString+=" project:\""+((LinkValue)project.getSelectedItem()).text().get()+"\"";
         }

         if (createdOn.isShowing() && !createdOnPicker.getSelection().isEmpty())
         {
            SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd" );
            if (createdOnPicker.getSelection().size() == 1)
               searchString+=" createdOn:"+ dateFormat.format( createdOnPicker.getSelection().first() );
            else
               searchString+=" createdOn:"+ dateFormat.format( createdOnPicker.getSelection().first() )+"-"+dateFormat.format( createdOnPicker.getSelection().last() );
         }

         searchResultTableModel.search( searchString );
      }
   }

   @Action
   public Task saveSearch()
   {
      final SaveSearchDialog dialog = saveSearchDialogs.iterator().next();
      dialog.presetQuery( searchField.getText() );
      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), dialog, text( WorkspaceResources.save_search ) );

      if (dialog.search() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.saveSearch( dialog.search() );
            }
         };
      } else
         return null;
   }

   @Action
   public void handle()
   {
      HandleSearchesDialog handleSearchesDialog = handleSearchesDialogs.use( module.valueBuilderFactory(), this.model ).newInstance();
      dialogs.showOkDialog( WindowUtils.findWindow( this ), handleSearchesDialog, text( WorkspaceResources.handle_searches ) );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Specifications.or(Events.onEntities( model.getList() ), Events.withNames( "createdSavedSearch", "removedSavedSearch" )), transactions ))
         model.refresh();
   }

   class SearchComboEditor extends JTextField
         implements ComboBoxEditor
   {
      private LinkValue link;

      public Component getEditorComponent()
      {
         return this;
      }

      public void setItem( Object anObject )
      {
         if (anObject instanceof TitledLinkValue)
         {
            this.link = (LinkValue) anObject;
            this.setText( ((TitledLinkValue) anObject).title().get() );
         }
      }

      public Object getItem()
      {
         return this.getText();
      }

      public LinkValue getLink()
      {
         return link;
      }

      public void clear()
      {
         link = null;
         this.setText( "" );
      }
   }
}