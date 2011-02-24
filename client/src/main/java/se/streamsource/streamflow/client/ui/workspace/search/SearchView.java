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

import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.WrapLayout;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventComboBoxModel;

/**
 * JAVADOC
 */
public class SearchView
      extends JPanel
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private JTextField searchField;

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
      
      ActionMap am;
      setActionMap( am = context.getActionMap( this ) );

      searchField = new JTextField(40);
      searchField.addActionListener( am.get( "search" ) );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "saveSearch" ) );
      options.add( am.get( "handle" ) );

      search = new JPanel(new WrapLayout(FlowLayout.LEFT));
      search.add(searchField);
      search.add( Box.createHorizontalStrut( 10 ) );
      
      {
         Box searchBox = Box.createHorizontalBox();
         JLabel label = new JLabel( i18n.text( WorkspaceResources.status ) );
         status = new JComboBox( new CaseStatesComboBoxModel(
               new String[]{CaseStates.OPEN.name(), CaseStates.ON_HOLD.name(), CaseStates.CLOSED.name()} ) );
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
         createdOn = new JButton(text( WorkspaceResources.choose_date ) );
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
      addSearch( text( WorkspaceResources.status ) );
      addSearch( text( WorkspaceResources.label ) );
      addSearch( text( WorkspaceResources.assignee ) );
      addSearch( text( WorkspaceResources.project ) );
      addSearch( text( WorkspaceResources.created_on ) );

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
      dialogs.showOkCancelHelpDialog( (Component) event.getSource(), createdOnPicker );
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
            searchString+=" status:"+ ((CaseStatesComboBoxModel)status.getModel()).getSelectedStatus();
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

  /*
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
*/


   class CaseStatesComboBoxModel
      extends DefaultComboBoxModel
   {
      Map map = new HashMap();

      public CaseStatesComboBoxModel( String[] items )
      {
         for( String item : items )
         {
            String translation = text( WorkspaceResources.valueOf( item ) );
            map.put( translation, item );
            this.addElement( translation );
         }
      }

      public Object getSelectedStatus() {
        return map.get( super.getSelectedItem() );
    }
   }
}