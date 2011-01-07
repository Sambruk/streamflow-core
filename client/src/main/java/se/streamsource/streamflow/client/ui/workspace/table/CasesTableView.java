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

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventJXTableModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.PinstripePainter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.table.SeparatorTable;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

/**
 * Base class for all views of case lists.
 */
public class CasesTableView
      extends JPanel
      implements TransactionListener
{
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
         }
         else
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
            return caseTableValue.assignedTo().get().equals(assignee);
         }
         else
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
            return caseTableValue.owner().get().equals(project);
         }
         else
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
               return (int) Math.signum(o1.dueOn().get().compareTo( o2.dueOn().get() ));
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

   private JButton columnSettings;
   private JPopupMenu filterAddmenu;
   private JPanel filters;

   public void init( @Service ApplicationContext context,
                     @Uses CasesTableModel casesTableModel,
                     @Uses TableFormat tableFormat )
   {
//      setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );
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

      columnSettings = new JButton( i18n.icon( Icons.options, 16 ));
      columnSettings.addActionListener( am.get( "columns" ) );

      filters = new JPanel(new WrapLayout(FlowLayout.LEFT));
      filters.add(new JLabel("Filter:"));
      filters.add( new JButton( am.get( "add" ) ) );
      filters.setBorder( BorderFactory.createEtchedBorder());
      {
         labels = new JComboBox(new EventComboBoxModel<String>( labelList ));
         labels.setPreferredSize( new Dimension( 150, (int) labels.getPreferredSize().getHeight() ) );
         labelList.add( i18n.text( WorkspaceResources.all ) );
         labels.setSelectedIndex( 0 );
         labels.addActionListener( am.get("labels") );

         Box labelBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( i18n.text( WorkspaceResources.label ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         labelBox.add( comp );
         labelBox.add( labels );
         labelBox.setVisible( false );
         filters.add( labelBox );
      }

      {
         assignees = new JComboBox(new EventComboBoxModel<String>( assigneeList ));
         assignees.setPreferredSize( new Dimension( 150, (int) assignees.getPreferredSize().getHeight() ) );
         assigneeList.add( i18n.text( WorkspaceResources.all ) );
         assignees.setSelectedIndex( 0 );
         assignees.addActionListener( am.get("assignee") );

         Box assigneeBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( i18n.text( WorkspaceResources.assignee ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         assigneeBox.add( comp );
         assigneeBox.add( assignees );
         assigneeBox.setVisible( false );
         filters.add( assigneeBox );
      }

      {
         projects = new JComboBox(new EventComboBoxModel<String>( projectList ));
         projects.setPreferredSize( new Dimension( 150, (int) projects.getPreferredSize().getHeight() ) );
         projectList.add( i18n.text( WorkspaceResources.all ) );
         projects.setSelectedIndex( 0 );
         projects.addActionListener( am.get("project") );

         Box projectBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( i18n.text( WorkspaceResources.project ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         projectBox.add( comp );
         projectBox.add( projects );
         projectBox.setVisible( false );
         filters.add( projectBox );
      }

      {
         sorting = new JComboBox( new String[]{"None", "Created on", "Description", "Due date"} );
         sorting.addActionListener( am.get( "sorting" ) );

         Box sortingBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( i18n.text( WorkspaceResources.sorting ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         sortingBox.add( comp );
         sortingBox.add( sorting );
         sortingBox.setVisible( false );
         filters.add( sortingBox );
      }

      {
         grouping = new JComboBox( new String[]{"None", "Case type", "Assignee", "Project"} );
         grouping.addActionListener( am.get( "grouping" ) );

         Box groupingBox = Box.createHorizontalBox();
         JLabel comp = new JLabel( i18n.text( WorkspaceResources.grouping ), JLabel.RIGHT );
         comp.setForeground( Color.gray );
         groupingBox.add( comp );
         groupingBox.add( grouping );
         groupingBox.setVisible( false );
         filters.add( groupingBox );
      }

      filterAddmenu = new JPopupMenu();
      addFilter( "Labels" );
      addFilter( "Assignees" );
      addFilter( "Projects" );
      addFilter( "Sorting" );
      addFilter( "Grouping" );

      labels();

      caseTable.getColumn( 1 ).setPreferredWidth( 70 );
      caseTable.getColumn( 1 ).setMaxWidth( 100 );
      caseTable.getColumn( 2 ).setPreferredWidth( 150 );
      caseTable.getColumn( 2 ).setMaxWidth( 150 );
      caseTable.getColumn( 3 ).setPreferredWidth( 150 );
      caseTable.getColumn( 3 ).setMaxWidth( 150 );
      caseTable.getColumn( caseTable.getColumnCount() - 1 ).setMaxWidth( 50 );
      caseTable.getColumn( caseTable.getColumnCount() - 1 ).setResizable( false );

      caseTable.setAutoCreateColumnsFromModel( false );

      Component horizontalGlue = Box.createHorizontalGlue();
      horizontalGlue.setPreferredSize( new Dimension( 1500, 10 ) );
      filters.add( horizontalGlue );

      JScrollPane caseScrollPane = new JScrollPane( caseTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
      caseScrollPane.setCorner( JScrollPane.UPPER_RIGHT_CORNER, columnSettings);
      add( filters, BorderLayout.NORTH);
      add( caseScrollPane, BorderLayout.CENTER);

      JXTable.BooleanEditor completableEditor = new JXTable.BooleanEditor();
      caseTable.setDefaultEditor( Boolean.class, completableEditor );
      caseTable.setDefaultRenderer( Date.class, new DefaultTableRenderer( new StringValue()
      {
         private SimpleDateFormat format = new SimpleDateFormat();

         public String getString( Object value )
         {
            if (value == null) return "";
            Date time = (Date) value;
            return format.format( time );
         }
      } ) );
      caseTable.setDefaultRenderer( ArrayList.class, new DefaultTableRenderer()
      {

         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
         {
            JPanel renderer = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            //BoxLayout box = new BoxLayout( renderer, BoxLayout.X_AXIS );

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
      caseTable.setDefaultRenderer( CaseStates.class, new CaseStatusTableCellRenderer() );
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
            }
            Component component = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
            component.setFont( component.getFont().deriveFont( Font.BOLD + Font.ITALIC ) );
            component.setBackground( Color.lightGray );
            return component;
         }
      } );

      caseTable.addHighlighter( HighlighterFactory.createAlternateStriping() );

      PinstripePainter p = new PinstripePainter();
      p.setAngle( 90 );
      p.setPaint( Color.LIGHT_GRAY );

      caseTable.addHighlighter( new PainterHighlighter( new HighlightPredicate()
      {
         public boolean isHighlighted( Component component, ComponentAdapter componentAdapter )
         {
            if (componentAdapter != null)
            {
               Object value = componentAdapter.getValue( componentAdapter.getColumnCount() - 1 );
               return value.equals( value.equals( CaseStates.CLOSED ) );
            } else
               return false;
         }
      }, p ) );

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
                  labels.add(linkValue.text().get());
               }

               assignees.add( caseTableValue.assignedTo().get() );
               projects.add( caseTableValue.owner().get() );
            }
            List<String> sortedLabels = new ArrayList<String>(labels);
            List<String> sortedAssignees = new ArrayList<String>(assignees);
            List<String> sortedProjects = new ArrayList<String>(projects);
            Collections.sort( sortedLabels );
            Collections.sort(sortedAssignees);
            Collections.sort(sortedProjects);
            sortedLabels.add( 0, i18n.text( WorkspaceResources.all ) );
            sortedAssignees.add( 0, i18n.text( WorkspaceResources.all ) );
            sortedProjects.add( 0, i18n.text( WorkspaceResources.all ) );

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

   @org.jdesktop.application.Action
   public void add(ActionEvent event)
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
         filters.getComponent( idx+2 ).setVisible( checkBox.isSelected() );
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

      optionsPopup.show( columnSettings, 0, columnSettings.getHeight() );
   }

   @org.jdesktop.application.Action
   public void labels()
   {
      labelFilterList = new FilterList<CaseTableValue>( model.getEventList(), labelMatcher);

      assignee();
   }

   @org.jdesktop.application.Action
   public void assignee()
   {
      assigneeFilterList = new FilterList<CaseTableValue>( labelFilterList, assigneeMatcher);

      project();
   }

   @org.jdesktop.application.Action
   public void project()
   {
      projectFilterList = new FilterList<CaseTableValue>( assigneeFilterList, projectMatcher);

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
      groupingList = new SeparatorList<CaseTableValue>( sortingList, groupingComparator, grouping.getSelectedIndex() == 0 ? 10000 : 2, 10000);

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
            "createdConversation", "submittedForm", "createdAttachment",
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