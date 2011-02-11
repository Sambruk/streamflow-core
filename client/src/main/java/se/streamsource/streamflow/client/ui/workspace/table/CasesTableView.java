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

package se.streamsource.streamflow.client.ui.workspace.table;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventJXTableModel;
import ca.odell.glazedlists.swing.EventTableModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.PinstripePainter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.WrapLayout;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.table.SeparatorTable;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.util.DateFormats;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * Base class for all views of case lists.
 */
public class CasesTableView
      extends JPanel
      implements TransactionListener
{
   public static final int MILLIS_IN_DAY = (1000 * 60 * 60 * 24);
   public static final WorkspaceResources[] dueGroups = {WorkspaceResources.overdue, WorkspaceResources.duetoday, WorkspaceResources.duetomorrow, WorkspaceResources.duenextweek, WorkspaceResources.duenextmonth, WorkspaceResources.later, WorkspaceResources.noduedate};

   private Matcher<CaseTableValue> labelMatcher = new Matcher<CaseTableValue>()
   {
      public boolean matches( CaseTableValue caseTableValue )
      {
         if (labels.getSelectedIndex() > 0)
         {
            String label = CasesTableView.this.labels.getSelectedItem().toString();
            for (LinkValue linkValue : caseTableValue.labels().get().links().get())
            {
               if (linkValue.text().get().equals( label ))
                  return true;
            }

            return false;
         } else
            return true;
      }
   };

   private Matcher<CaseTableValue> assigneeMatcher = new Matcher<CaseTableValue>()
   {
      public boolean matches( CaseTableValue caseTableValue )
      {
         if (assignees.getSelectedIndex() > 0)
         {
            String assignee = CasesTableView.this.assignees.getSelectedItem().toString();
            return caseTableValue.assignedTo().get().equals( assignee );
         } else
            return true;
      }
   };

   private Matcher<CaseTableValue> projectMatcher = new Matcher<CaseTableValue>()
   {
      public boolean matches( CaseTableValue caseTableValue )
      {
         if (projects.getSelectedIndex() > 0)
         {
            String project = CasesTableView.this.projects.getSelectedItem().toString();
            return caseTableValue.owner().get().equals( project );
         } else
            return true;
      }
   };

   private Comparator<CaseTableValue> sortingComparator = new Comparator<CaseTableValue>()
   {
      public int compare( CaseTableValue o1, CaseTableValue o2 )
      {
         int selectedSorting = sorting.getSelectedIndex();
         if (selectedSorting == 1)
         {
            return o1.creationDate().get().compareTo( o2.creationDate().get() );
         } else if (selectedSorting == 2)
         {
            return o1.description().get().compareToIgnoreCase( o2.description().get() );
         } else if (selectedSorting == 3)
         {
            if (o1.dueOn().get() == null && o2.dueOn().get() == null)
               return 0;
            else if (o1.dueOn().get() == null)
               return 1;
            else if (o2.dueOn().get() == null)
               return -1;
            else
               return (int) Math.signum( o1.dueOn().get().compareTo( o2.dueOn().get() ) );
         }

         return 0;
      }
   };

   private Comparator<CaseTableValue> groupingComparator = new Comparator<CaseTableValue>()
   {
      public int compare( CaseTableValue o1, CaseTableValue o2 )
      {
         int selectedIndex = grouping.getSelectedIndex();
         if (selectedIndex == 1)
            return o1.caseType().get().compareTo( o2.caseType().get() );
         else if (selectedIndex == 2)
            return o1.assignedTo().get().compareTo( o2.assignedTo().get() );
         else if (selectedIndex == 3)
            return o1.owner().get().compareTo( o2.owner().get() );
         else if (selectedIndex == 4)
            return dueOnGroup( o1.dueOn().get() ).compareTo( dueOnGroup( o2.dueOn().get() ) );
         else
            return 0;
      }
   };

   protected JXTable caseTable;
   protected CasesTableModel model;
   private TableFormat tableFormat;
   private ApplicationContext context;

   private JComboBox labels;
   private FilterList<CaseTableValue> labelFilterList;
   private EventList<String> labelList = new BasicEventList<String>();

   private JComboBox assignees;
   private FilterList<CaseTableValue> assigneeFilterList;
   private EventList<String> assigneeList = new BasicEventList<String>();

   private JComboBox projects;
   private FilterList<CaseTableValue> projectFilterList;
   private EventList<String> projectList = new BasicEventList<String>();

   private JComboBox sorting;
   private SortedList<CaseTableValue> sortingList;

   private JComboBox grouping;
   private SeparatorList<CaseTableValue> groupingList;

   private JButton cxolumnSettings;
   private JPopupMenu filterAddmenu;
   private JPanel filters;

   public void init( @Service ApplicationContext context,
                     @Uses CasesTableModel casesTableModel,
                     @Uses TableFormat tableFormat )
   {
      setLayout( new BorderLayout() );

      this.context = context;
      this.model = casesTableModel;
      this.tableFormat = tableFormat;

      ActionMap am = context.getActionMap( CasesTableView.class, this );
      setActionMap( am );
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            CasesTableView.class, this ) );

      // Table
      // Trigger creation of filters and table model
      caseTable = new SeparatorTable( null );
      caseTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      caseTable.getActionMap().getParent().setParent( am );
      caseTable.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS ) );
      caseTable.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS ) );

      caseTable.setColumnControlVisible( true );
/*
      columnSettings = new JButton( i18n.icon( Icons.options, 16 ) );
      columnSettings.addActionListener( am.get( "columns" ) );
*/

      filters = new JPanel( new WrapLayout( FlowLayout.LEFT ) );
      filters.add( new JLabel( text( WorkspaceResources.filter ) ) );
      filters.add( new JButton( am.get( "add" ) ) );
      filters.setBorder( BorderFactory.createEtchedBorder() );
      {
         labels = new JComboBox( new EventComboBoxModel<String>( labelList ) );
         labels.setPreferredSize( new Dimension( 150, (int) labels.getPreferredSize().getHeight() ) );
         labelList.add( text( WorkspaceResources.all ) );
         labels.setSelectedIndex( 0 );
         labels.addActionListener( am.get( "labels" ) );

         Box labelBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( text( WorkspaceResources.label ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         labelBox.add( comp );
         labelBox.add( labels );
         labelBox.setVisible( false );
         filters.add( labelBox );
      }

      {
         assignees = new JComboBox( new EventComboBoxModel<String>( assigneeList ) );
         assignees.setPreferredSize( new Dimension( 150, (int) assignees.getPreferredSize().getHeight() ) );
         assigneeList.add( text( WorkspaceResources.all ) );
         assignees.setSelectedIndex( 0 );
         assignees.addActionListener( am.get( "assignee" ) );

         Box assigneeBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( text( WorkspaceResources.assignee ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         assigneeBox.add( comp );
         assigneeBox.add( assignees );
         assigneeBox.setVisible( false );
         filters.add( assigneeBox );
      }

      {
         projects = new JComboBox( new EventComboBoxModel<String>( projectList ) );
         projects.setPreferredSize( new Dimension( 150, (int) projects.getPreferredSize().getHeight() ) );
         projectList.add( text( WorkspaceResources.all ) );
         projects.setSelectedIndex( 0 );
         projects.addActionListener( am.get( "project" ) );

         Box projectBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( text( WorkspaceResources.project ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         projectBox.add( comp );
         projectBox.add( projects );
         projectBox.setVisible( false );
         filters.add( projectBox );
      }

      {
         sorting = new JComboBox( new String[]{
               text( WorkspaceResources.none ),
               text( WorkspaceResources.created_on ),
               text( WorkspaceResources.description_label ),
               text( WorkspaceResources.duedate_column_header )} );
         sorting.addActionListener( am.get( "sorting" ) );

         Box sortingBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( text( WorkspaceResources.sorting ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         sortingBox.add( comp );
         sortingBox.add( sorting );
         sortingBox.setVisible( false );
         filters.add( sortingBox );
      }

      {
         grouping = new JComboBox( new String[]{
               text( WorkspaceResources.none ),
               text( WorkspaceResources.case_type ),
               text( WorkspaceResources.assignee ),
               text( WorkspaceResources.project ),
               text( WorkspaceResources.due_on_label )} );
         grouping.addActionListener( am.get( "grouping" ) );

         Box groupingBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( text( WorkspaceResources.grouping ), JLabel.RIGHT );
         comp.setForeground( Color.gray );
         groupingBox.add( comp );
         groupingBox.add( grouping );
         groupingBox.setVisible( false );
         filters.add( groupingBox );
      }

      filterAddmenu = new JPopupMenu();
      addFilter( text( WorkspaceResources.label ) );
      addFilter( text( WorkspaceResources.assignee ) );
      addFilter( text( WorkspaceResources.project ) );
      addFilter( text( WorkspaceResources.sorting ) );
      addFilter( text( WorkspaceResources.grouping ) );

      labels();

      caseTable.getColumn( 1 ).setPreferredWidth( 70 );
      caseTable.getColumn( 1 ).setMaxWidth( 100 );
      caseTable.getColumn( 2 ).setPreferredWidth( 300 );
      caseTable.getColumn( 2 ).setMaxWidth( 300 );
      caseTable.getColumn( 3 ).setPreferredWidth( 150 );
      caseTable.getColumn( 3 ).setMaxWidth( 150 );
      caseTable.getColumn( 4 ).setPreferredWidth( 90 );
      caseTable.getColumn( 4 ).setMaxWidth( 90 );
      caseTable.getColumn( caseTable.getColumnCount() - 1 ).setMaxWidth( 50 );
      caseTable.getColumn( caseTable.getColumnCount() - 1 ).setResizable( false );

      // Do this in reverse because ordering is changed by invisibility
      caseTable.getColumnExt( 6 ).setVisible( false );
      caseTable.getColumnExt( 5 ).setVisible( false );
      caseTable.getColumnExt( 3 ).setVisible( false );

      caseTable.setAutoCreateColumnsFromModel( false );

      Component horizontalGlue = Box.createHorizontalGlue();
      horizontalGlue.setPreferredSize( new Dimension( 1500, 10 ) );
      filters.add( horizontalGlue );

      JScrollPane caseScrollPane = new JScrollPane( caseTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
//      caseScrollPane.setCorner( JScrollPane.UPPER_RIGHT_CORNER, columnSettings );
      add( filters, BorderLayout.NORTH );
      add( caseScrollPane, BorderLayout.CENTER );

      caseTable.setDefaultRenderer( Date.class, new DefaultTableRenderer( new StringValue()
      {
         private static final long serialVersionUID = 4782416330896582518L;

         public String getString(Object value)
         {
            return DateFormats.getProgressiveDateTimeValue((Date) value, Locale.getDefault());
         }
      } ) );
      caseTable.setDefaultRenderer( ArrayList.class, new DefaultTableCellRenderer()
      {

         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
         {
            JPanel renderer = new JPanel( new FlowLayout( FlowLayout.LEFT ) );

            ArrayList<String> icons = (ArrayList<String>) value;
            for (String icon : icons)
            {
               ImageIcon image = i18n.icon( Icons.valueOf( icon ), 11 );
               JLabel iconLabel = image != null ? new JLabel( image, SwingConstants.LEADING ) : new JLabel( "   " );
               renderer.add( iconLabel );
            }
            if (isSelected)
               renderer.setBackground( table.getSelectionBackground() );
            return renderer;
         }
      } );
      caseTable.setDefaultRenderer( CaseStates.class, new DefaultTableCellRenderer()
      {
         @Override
         public Component getTableCellRendererComponent( JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus, int row, int column )
         {
            EventTableModel model = (EventTableModel)table.getModel();
            boolean hasResolution = !Strings.empty( ((CaseTableValue)model.getElementAt( row )).resolution().get() );
            String iconName = hasResolution ? "case_status_withresolution_" + value.toString().toLowerCase() + "_icon"
                  : "case_status_" + value.toString().toLowerCase() + "_icon";

            JLabel renderedComponent = (JLabel) super.getTableCellRendererComponent( table, value, isSelected, hasFocus,
                  row, column );
            renderedComponent.setHorizontalAlignment( SwingConstants.CENTER );
            setText( null );

            setIcon( i18n.icon( CaseResources.valueOf( iconName ),
                  i18n.ICON_16 ) );
            setName( i18n.text( CaseResources.valueOf( "case_status_" + value.toString().toLowerCase() + "_text" ) ) );
            setToolTipText( i18n.text( CaseResources.valueOf( "case_status_" + value.toString().toLowerCase() + "_text" ) ) );

            return this;
         }
      } );
      caseTable.setDefaultRenderer( SeparatorList.Separator.class, new DefaultTableCellRenderer()
      {
         @Override
         public Component getTableCellRendererComponent( JTable table, Object separator, boolean isSelected, boolean hasFocus, int row, int column )
         {
            String value = "";
            switch (grouping.getSelectedIndex())
            {
               case 1:
                  value = ((CaseTableValue) ((SeparatorList.Separator) separator).first()).caseType().get();
                  break;
               case 2:
                  value = ((CaseTableValue) ((SeparatorList.Separator) separator).first()).assignedTo().get();
                  break;
               case 3:
                  value = ((CaseTableValue) ((SeparatorList.Separator) separator).first()).owner().get();
                  break;
               case 4:
                  value = text( dueGroups[dueOnGroup( ((CaseTableValue) ((SeparatorList.Separator) separator).first()).dueOn().get() )] );
                  break;
            }
            Component component = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
            component.setFont( component.getFont().deriveFont( Font.BOLD + Font.ITALIC ) );
            component.setBackground( Color.lightGray );
            return component;
         }
      } );

      caseTable.addHighlighter( HighlighterFactory.createAlternateStriping() );

      addFocusListener( new FocusAdapter()
      {
         public void focusGained( FocusEvent e )
         {
            caseTable.requestFocusInWindow();
         }
      } );

      model.getEventList().addListEventListener( new ListEventListener<CaseTableValue>()
      {
         public void listChanged( ListEvent<CaseTableValue> listChanges )
         {
            // Synchronize lists
            Set<String> labels = new HashSet<String>();
            Set<String> assignees = new HashSet<String>();
            Set<String> projects = new HashSet<String>();
            for (CaseTableValue caseTableValue : listChanges.getSourceList())
            {
               for (LinkValue linkValue : caseTableValue.labels().get().links().get())
               {
                  labels.add( linkValue.text().get() );
               }

               assignees.add( caseTableValue.assignedTo().get() );
               projects.add( caseTableValue.owner().get() );
            }
            List<String> sortedLabels = new ArrayList<String>( labels );
            List<String> sortedAssignees = new ArrayList<String>( assignees );
            List<String> sortedProjects = new ArrayList<String>( projects );
            Collections.sort( sortedLabels );
            Collections.sort( sortedAssignees );
            Collections.sort( sortedProjects );
            sortedLabels.add( 0, text( WorkspaceResources.all ) );
            sortedAssignees.add( 0, text( WorkspaceResources.all ) );
            sortedProjects.add( 0, text( WorkspaceResources.all ) );

            EventListSynch.synchronize( sortedLabels, labelList );
            EventListSynch.synchronize( sortedAssignees, assigneeList );
            EventListSynch.synchronize( sortedProjects, projectList );
         }
      } );

      new RefreshWhenShowing( this, model );
   }

   private void addFilter( String name )
   {
      JCheckBoxMenuItem status = new JCheckBoxMenuItem( name );
      status.addActionListener( getActionMap().get( "showFilter" ) );
      filterAddmenu.add( status );
   }

   public JXTable getCaseTable()
   {
      return caseTable;
   }

   public CasesTableModel getModel()
   {
      return model;
   }


   private Integer dueOnGroup( Date date )
   {
      /**
       * 0 = Overdue
       * 1 = Today
       * 2 = Tomorrow
       * 3 = Within next week
       * 4 = Within next month
       * 5 = Later
       * 6 = No due date
       */

      long currentTime = System.currentTimeMillis();
      currentTime = currentTime / MILLIS_IN_DAY;
      currentTime *= MILLIS_IN_DAY;
      Date today = new Date( currentTime );
      Date lateToday = new Date( currentTime + MILLIS_IN_DAY - 1 );

      Calendar month = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
      month.setTime( today );
      month.add( Calendar.MONTH, 1 );

      Calendar week = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
      week.setTime( today );
      week.add( Calendar.WEEK_OF_YEAR, 1 );

      Calendar tomorrow = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
      tomorrow.setTime( lateToday );
      tomorrow.add( Calendar.DATE, 1 );

      int group;
      if (date == null)
         group = 6;
      else if (date.after( month.getTime() ))
         group = 5; // Later
      else if (date.after( week.getTime() ))
         group = 4; // Within next month
      else if (date.after( tomorrow.getTime() ))
         group = 3; // Within next week
      else if (date.after( lateToday ))
         group = 2; // Tomorrow
      else if (date.after( today ))
         group = 1;
      else
         group = 0;

      System.out.println( date + "=" + group );
      return group;
   }

   @org.jdesktop.application.Action
   public void add( ActionEvent event )
   {
      Component source = (Component) event.getSource();
      filterAddmenu.show( source, 0, source.getHeight() );
   }

   @org.jdesktop.application.Action
   public void showFilter()
   {
      for (int idx = 0; idx < filterAddmenu.getComponents().length; idx++)
      {
         Component component = filterAddmenu.getComponents()[idx];
         JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) component;
         filters.getComponent( idx + 2 ).setVisible( checkBox.isSelected() );
      }
      filters.revalidate();
   }

   @org.jdesktop.application.Action
   public void columns()
   {
      JPopupMenu optionsPopup = new JPopupMenu();
      optionsPopup.add( new JCheckBoxMenuItem( "Case id" ) );
      optionsPopup.add( new JCheckBoxMenuItem( "Case type" ) );
      optionsPopup.add( new JCheckBoxMenuItem( "Status" ) );
      optionsPopup.add( new JCheckBoxMenuItem( "Created date" ) );

//      optionsPopup.show( columnSettings, 0, columnSettings.getHeight() );
   }

   @org.jdesktop.application.Action
   public void labels()
   {
      labelFilterList = new FilterList<CaseTableValue>( model.getEventList(), labelMatcher );

      assignee();
   }

   @org.jdesktop.application.Action
   public void assignee()
   {
      assigneeFilterList = new FilterList<CaseTableValue>( labelFilterList, assigneeMatcher );

      project();
   }

   @org.jdesktop.application.Action
   public void project()
   {
      projectFilterList = new FilterList<CaseTableValue>( assigneeFilterList, projectMatcher );

      sorting();
   }

   @org.jdesktop.application.Action
   public void sorting()
   {
      sortingList = new SortedList<CaseTableValue>( projectFilterList, sortingComparator );
      grouping();
   }

   @org.jdesktop.application.Action
   public void grouping()
   {
      groupingList = new SeparatorList<CaseTableValue>( sortingList, groupingComparator, grouping.getSelectedIndex() == 0 ? 10000 : 1, 10000 );

      caseTable.setModel( new EventJXTableModel<CaseTableValue>( groupingList, tableFormat ) );
   }

   public void notifyTransactions( final Iterable<TransactionDomainEvents> transactions )
   {

      if (Events.matches( withNames( "createdCase" ), transactions ))
      {
         context.getTaskService().execute( new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.refresh();
            }

            @Override
            protected void succeeded( Iterable<TransactionDomainEvents> transactionEventsIterable )
            {
               super.succeeded( transactionEventsIterable );

               caseTable.getSelectionModel().setSelectionInterval( caseTable.getRowCount() - 1, caseTable.getRowCount() - 1 );
               caseTable.scrollRowToVisible( caseTable.getRowCount() - 1 );
            }
         } );
      } else if (Events.matches( withNames( "addedLabel", "removedLabel",
            "changedDescription", "changedCaseType", "changedStatus",
            "changedOwner", "assignedTo", "unassigned", "deletedEntity",
            "updatedContact", "addedContact", "deletedContact",
            "createdConversation", "changedDueOn", "submittedForm", "createdAttachment",
            "removedAttachment" ), transactions ))
      {
         context.getTaskService().execute( new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.refresh();

               if (Events.matches( withNames( "changedStatus",
                     "changedOwner", "assignedTo", "unassigned", "deletedEntity" ), transactions ))
               {
                  caseTable.getSelectionModel().clearSelection();
               }
            }
         } );
      }
   }
}