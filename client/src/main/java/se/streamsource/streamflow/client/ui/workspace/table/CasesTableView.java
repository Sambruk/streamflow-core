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

import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import ca.odell.glazedlists.swing.EventTableModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.table.SeparatorTable;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.util.DateFormats;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import static se.streamsource.streamflow.client.util.i18n.text;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

/**
 * Base class for all views of case lists.
 */
public class CasesTableView
        extends JPanel
        implements TransactionListener
{
   @Structure
   ObjectBuilderFactory obf;

   public static final int MILLIS_IN_DAY = (1000 * 60 * 60 * 24);
   public static final WorkspaceResources[] dueGroups = {WorkspaceResources.overdue, WorkspaceResources.duetoday, WorkspaceResources.duetomorrow, WorkspaceResources.duenextweek, WorkspaceResources.duenextmonth, WorkspaceResources.later, WorkspaceResources.noduedate};

   private Comparator<CaseTableValue> groupingComparator = new Comparator<CaseTableValue>()
   {
      public int compare(CaseTableValue o1, CaseTableValue o2)
      {
         GroupBy groupBy = model.getGroupBy();
         switch (groupBy)
         {
            case caseType:
               return o1.caseType().get().compareTo( o2.caseType().get() );
            case dueOn:
               return dueOnGroup( o1.dueOn().get() ).compareTo( dueOnGroup( o2.dueOn().get() ) );
            case assignee:
               return o1.assignedTo().get().compareTo( o2.assignedTo().get() );
            case project:
               return o1.owner().get().compareTo( o2.owner().get() );
            default:
               return 0;
         }
      }
   };

   protected JXTable caseTable;
   protected CasesTableModel model;
   private TableFormat tableFormat;
   private ApplicationContext context;

   private PerspectiveView filter;


   public void init( @Service ApplicationContext context,
                     @Uses CasesTableModel casesTableModel,
                     final @Uses TableFormat tableFormat,
                     @Optional @Uses JTextField searchField )
   {
      setLayout(new BorderLayout());

      this.context = context;
      this.model = casesTableModel;
      this.tableFormat = tableFormat;

      ActionMap am = context.getActionMap(CasesTableView.class, this);
      setActionMap(am);
      MacOsUIWrapper.convertAccelerators(context.getActionMap(
              CasesTableView.class, this));

      // Filter
      filter = obf.newObjectBuilder(PerspectiveView.class).use(model, searchField).newInstance();
      add(filter, BorderLayout.NORTH);

      // Table
      // Trigger creation of filters and table model
      caseTable = new SeparatorTable(null);
      caseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      caseTable.getActionMap().getParent().setParent(am);
      caseTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
              KeyboardFocusManager.getCurrentKeyboardFocusManager()
                      .getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
      caseTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
              KeyboardFocusManager.getCurrentKeyboardFocusManager()
                      .getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

      caseTable.getActionMap().remove( "column.horizontalScroll" );
      caseTable.getActionMap().remove( "column.packAll" );
      caseTable.getActionMap().remove( "column.packSelected" );
      caseTable.setColumnControlVisible( true );

      caseTable.setModel( new EventJXTableModel<CaseTableValue>( model.getEventList(), tableFormat ) );

      caseTable.setModel(new EventJXTableModel<CaseTableValue>(model.getEventList(), tableFormat));

      model.addObserver(new Observer()
      {
         public void update(Observable o, Object arg)
         {
            if (model.getGroupBy() == GroupBy.none)
            {
               caseTable.setModel(new EventJXTableModel<CaseTableValue>(model.getEventList(), tableFormat));
            } else
            {
               SeparatorList<CaseTableValue> groupingList = new SeparatorList<CaseTableValue>(model.getEventList(),
                       groupingComparator, 1, 10000);
               caseTable.setModel(new EventJXTableModel<CaseTableValue>(groupingList, tableFormat));
            }

            for (Integer invisibleCol : model.getInvisibleColumns())
            {
               TableColumnModelExt tm = (TableColumnModelExt) caseTable.getColumnModel();
               if (tm.getColumnExt( invisibleCol ).isVisible())
                  caseTable.getColumnExt( invisibleCol ).setVisible( false );
            }
         }
      });

      caseTable.getColumn( 0 ).setPreferredWidth( 500 );
      caseTable.getColumn( 1 ).setPreferredWidth( 100 );
      caseTable.getColumn( 1 ).setMaxWidth( 100 );
      caseTable.getColumn( 1 ).setResizable( false );
      caseTable.getColumn( 2 ).setPreferredWidth( 300 );
      caseTable.getColumn( 2 ).setMaxWidth( 300 );
      caseTable.getColumn( 3 ).setPreferredWidth( 150 );
      caseTable.getColumn( 3 ).setMaxWidth( 150 );
      caseTable.getColumn( 4 ).setPreferredWidth( 90 );
      caseTable.getColumn( 4 ).setMaxWidth( 90 );
      caseTable.getColumn( 5 ).setPreferredWidth( 90 );
      caseTable.getColumn( 5 ).setMaxWidth( 90 );
      caseTable.getColumn( 6 ).setPreferredWidth( 150 );
      caseTable.getColumn( 6 ).setMaxWidth( 150 );
      caseTable.getColumn( 6 ).setResizable( false );
      caseTable.getColumn( 7 ).setMaxWidth( 50 );
      caseTable.getColumn( 7 ).setResizable( false );

      caseTable.setAutoCreateColumnsFromModel( false );

      int count = 0;
      for (TableColumn c : caseTable.getColumns())
      {
         c.setIdentifier( (Integer)count );
         count++;
         
         c.addPropertyChangeListener( new PropertyChangeListener()
         {
            public void propertyChange( PropertyChangeEvent evt )
            {
               if ("visible".equals( evt.getPropertyName() ))
               {
                  TableColumnExt columnExt = (TableColumnExt) evt.getSource();

                  if (columnExt.isVisible())
                  {
                     model.removeInvisibleColumn( columnExt.getModelIndex() );
                  } else
                  {
                     model.addInvisibleColumn( columnExt.getModelIndex() );
                  }
               }
            }
         } );
      }

      Component horizontalGlue = Box.createHorizontalGlue();
      horizontalGlue.setPreferredSize( new Dimension( 1500, 10 ) );
      // filters.add( horizontalGlue );

      JScrollPane caseScrollPane = new JScrollPane(caseTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      add(caseScrollPane, BorderLayout.CENTER);

      caseTable.setDefaultRenderer(Date.class, new DefaultTableRenderer(new StringValue()
      {
         private static final long serialVersionUID = 4782416330896582518L;

         public String getString( Object value )
         {
            return value != null ? DateFormats.getProgressiveDateTimeValue( (Date) value, Locale.getDefault() ) : "";
         }
      }));
      caseTable.setDefaultRenderer(ArrayList.class, new DefaultTableCellRenderer()
      {

         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
         {
            if (value instanceof SeparatorList.Separator)
               return caseTable.getDefaultRenderer( SeparatorList.Separator.class )
                     .getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

            JPanel renderer = new JPanel(new FlowLayout(FlowLayout.LEFT));

            ArrayList<String> icons = (ArrayList<String>) value;
            for (String icon : icons)
            {
               ImageIcon image = i18n.icon(Icons.valueOf(icon), 11);
               JLabel iconLabel = image != null ? new JLabel(image, SwingConstants.LEADING) : new JLabel("   ");
               renderer.add(iconLabel);
            }
            if (isSelected)
               renderer.setBackground(table.getSelectionBackground());
            return renderer;
         }
      });
      caseTable.setDefaultRenderer(CaseStates.class, new DefaultTableCellRenderer()
      {
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, int row, int column)
         {
            EventTableModel model = (EventTableModel) table.getModel();
            boolean hasResolution = !Strings.empty( ((CaseTableValue) model.getElementAt( row )).resolution().get() );
            String iconName = hasResolution ? "case_status_withresolution_" + value.toString().toLowerCase() + "_icon"
                    : "case_status_" + value.toString().toLowerCase() + "_icon";

            JLabel renderedComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            renderedComponent.setHorizontalAlignment(SwingConstants.CENTER);
            setText(null);

            setIcon(i18n.icon(CaseResources.valueOf(iconName),
                    i18n.ICON_16));
            setName(i18n.text(CaseResources.valueOf("case_status_" + value.toString().toLowerCase() + "_text")));
            setToolTipText(i18n.text(CaseResources.valueOf("case_status_" + value.toString().toLowerCase() + "_text")));

            return this;
         }
      });
      caseTable.setDefaultRenderer(SeparatorList.Separator.class, new DefaultTableCellRenderer()
      {
         @Override
         public Component getTableCellRendererComponent(JTable table, Object separator, boolean isSelected, boolean hasFocus, int row, int column)
         {
            String value = "";
            boolean emptyDescription = false;
            switch (model.getGroupBy())
            {
               case caseType:
                  emptyDescription = Strings.empty(((CaseTableValue) ((SeparatorList.Separator) separator).first()).caseType().get());
                  value = !emptyDescription ? ((CaseTableValue) ((SeparatorList.Separator) separator).first()).caseType().get() : text(WorkspaceResources.no_casetype);
                  break;
               case assignee:
                  emptyDescription = Strings.empty(((CaseTableValue) ((SeparatorList.Separator) separator).first()).assignedTo().get());
                  value = !emptyDescription ? ((CaseTableValue) ((SeparatorList.Separator) separator).first()).assignedTo().get() : text(WorkspaceResources.no_assignee);
                  break;
               case project:
                  emptyDescription = Strings.empty(((CaseTableValue) ((SeparatorList.Separator) separator).first()).assignedTo().get());
                  value = !emptyDescription ? ((CaseTableValue) ((SeparatorList.Separator) separator).first()).owner().get() : text(WorkspaceResources.no_project);
                  break;
               case dueOn:
                  value = text(dueGroups[dueOnGroup(((CaseTableValue) ((SeparatorList.Separator) separator).first()).dueOn().get())]);
                  break;
            }

            Component component = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
            component.setFont( component.getFont().deriveFont( Font.BOLD + Font.ITALIC ) );
            component.setBackground( Color.lightGray );
            return component;
         }
      });

      caseTable.addHighlighter(HighlighterFactory.createAlternateStriping());

      addFocusListener(new FocusAdapter()
      {
         public void focusGained(FocusEvent e)
         {
            caseTable.requestFocusInWindow();
         }
      });

      model.getEventList().addListEventListener(new ListEventListener<CaseTableValue>()
      {
         public void listChanged(ListEvent<CaseTableValue> listChanges)
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

               assignees.add(caseTableValue.assignedTo().get());
               projects.add(caseTableValue.owner().get());
            }
            List<String> sortedLabels = new ArrayList<String>(labels);
            List<String> sortedAssignees = new ArrayList<String>(assignees);
            List<String> sortedProjects = new ArrayList<String>(projects);
            Collections.sort(sortedLabels);
            Collections.sort(sortedAssignees);
            Collections.sort(sortedProjects);
            sortedLabels.add(0, text(WorkspaceResources.all));
            sortedAssignees.add(0, text(WorkspaceResources.all));
            sortedProjects.add(0, text(WorkspaceResources.all));
         }
      });

      new RefreshWhenShowing(this, model);
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
      Date today = new Date(currentTime);
      Date lateToday = new Date(currentTime + MILLIS_IN_DAY - 1);

      Calendar month = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      month.setTime(today);
      month.add(Calendar.MONTH, 1);

      Calendar week = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      week.setTime(today);
      week.add(Calendar.WEEK_OF_YEAR, 1);

      Calendar tomorrow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      tomorrow.setTime(lateToday);
      tomorrow.add(Calendar.DATE, 1);

      int group;
      if (date == null)
         group = 6;
      else if (date.after(month.getTime()))
         group = 5; // Later
      else if (date.after(week.getTime()))
         group = 4; // Within next month
      else if (date.after(tomorrow.getTime()))
         group = 3; // Within next week
      else if (date.after(lateToday))
         group = 2; // Tomorrow
      else if (date.after(today))
         group = 1;
      else
         group = 0;

      return group;
   }

   public void notifyTransactions( final Iterable<TransactionDomainEvents> transactions )
   {

      if (Events.matches(withNames("createdCase"), transactions))
      {
         context.getTaskService().execute(new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.refresh();
            }

            @Override
            protected void succeeded(Iterable<TransactionDomainEvents> transactionEventsIterable)
            {
               super.succeeded(transactionEventsIterable);

               caseTable.getSelectionModel().setSelectionInterval(caseTable.getRowCount() - 1, caseTable.getRowCount() - 1);
               caseTable.scrollRowToVisible(caseTable.getRowCount() - 1);
            }
         });
      } else if (Events.matches(withNames("addedLabel", "removedLabel",
              "changedDescription", "changedCaseType", "changedStatus",
              "changedOwner", "assignedTo", "unassigned", "deletedEntity",
              "updatedContact", "addedContact", "deletedContact",
              "createdConversation", "changedDueOn", "submittedForm", "createdAttachment",
              "removedAttachment"), transactions))
      {
         context.getTaskService().execute(new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {

               model.refresh();

               if (Events.matches(withNames("changedStatus",
                       "changedOwner", "assignedTo", "unassigned", "deletedEntity"), transactions))
               {
                  caseTable.getSelectionModel().clearSelection();
               }
            }
         });
      }
   }
}