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
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.table.SeparatorTable;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.*;

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
         }
         else
            return true;

         return false;
      }
   };

   private Matcher<CaseTableValue> statusMatcher = new Matcher<CaseTableValue>()
   {
      public boolean matches( CaseTableValue caseTableValue )
      {
         if (status.getSelectedIndex() > 0)
         {
            String status = CasesTableView.this.status.getSelectedItem().toString();
            return caseTableValue.status().get().equals( CaseStates.valueOf( status ) );
         }
         else
            return true;
      }
   };

   private Comparator<CaseTableValue> groupingComparator = new Comparator<CaseTableValue>()
   {
      public int compare( CaseTableValue o1, CaseTableValue o2 )
      {
         if (grouping.getSelectedIndex() == 1)
            return o1.caseType().get().compareTo( o2.caseType().get() );
         else
            return o1.caseId().get().compareTo( o2.caseId().get() );
      }
   };

   protected JXTable caseTable;
   protected CasesTableModel model;
   private TableFormat tableFormat;
   private ApplicationContext context;
   private JComboBox grouping;
   private JComboBox status;
   private JComboBox labels;
   private JTextField search;

   private FilterList<CaseTableValue> labelFilterList;
   private EventList<String> labelList = new BasicEventList<String>();

   private FilterList<CaseTableValue> statusList;
   private SeparatorList<CaseTableValue> groupingList;

   public void init( @Service ApplicationContext context,
                     @Uses CasesTableModel casesTableModel,
                     @Uses TableFormat tableFormat )
   {
      this.context = context;
      this.model = casesTableModel;
      this.tableFormat = tableFormat;
      setLayout( new BorderLayout() );

      ActionMap am = context.getActionMap( CasesTableView.class, this );
      setActionMap( am );
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            CasesTableView.class, this ) );

      status = new JComboBox( new String[]{"All", CaseStates.OPEN.name(), CaseStates.ON_HOLD.name(), CaseStates.CLOSED.name()} );
      status.addActionListener( am.get( "status" ) );

      grouping = new JComboBox( new String[]{"None", "Case type"} );
      grouping.addActionListener( am.get( "grouping" ) );

      labels = new JComboBox(new EventComboBoxModel<String>( labelList ));
      labelList.add( "All" );
      labels.setSelectedIndex( 0 );
      labels.addActionListener( am.get("labels") );

      // Table
      // Trigger creation of filters and table model
      caseTable = new SeparatorTable( null );
      labels();
      caseTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      caseTable.getActionMap().getParent().setParent( am );
      caseTable.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS ) );
      caseTable.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS ) );

      JScrollPane caseScrollPane = new JScrollPane( caseTable );

      caseTable.getColumn( 1 ).setPreferredWidth( 70 );
      caseTable.getColumn( 1 ).setMaxWidth( 100 );
      caseTable.getColumn( 2 ).setPreferredWidth( 150 );
      caseTable.getColumn( 2 ).setMaxWidth( 150 );
      caseTable.getColumn( 3 ).setPreferredWidth( 150 );
      caseTable.getColumn( 3 ).setMaxWidth( 150 );
      caseTable.getColumn( caseTable.getColumnCount() - 1 ).setMaxWidth( 50 );
      caseTable.getColumn( caseTable.getColumnCount() - 1 ).setResizable( false );

      caseTable.setAutoCreateColumnsFromModel( false );

      Box filter = Box.createHorizontalBox();
      {
         Box labelBox = Box.createVerticalBox();
         JLabel comp = new JLabel( i18n.text( WorkspaceResources.label ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         labelBox.add( comp );
         labelBox.add( labels );
         filter.add( labelBox );
      }

      {
         Box statusBox = Box.createVerticalBox();
         JLabel comp = new JLabel( i18n.text( WorkspaceResources.status ), JLabel.CENTER );
         comp.setForeground( Color.gray );
         statusBox.add( comp );
         statusBox.add( status );
         filter.add( statusBox );
      }

      {
         Box groupingBox = Box.createVerticalBox();
         JLabel comp = new JLabel( i18n.text( WorkspaceResources.grouping ), JLabel.RIGHT );
         comp.setForeground( Color.gray );
         groupingBox.add( comp );
         groupingBox.add( grouping );
         filter.add( groupingBox );
         filter.setBorder( BorderFactory.createEmptyBorder() );
      }

      Component horizontalGlue = Box.createHorizontalGlue();
      horizontalGlue.setPreferredSize( new Dimension( 1500, 10 ) );
      filter.add( horizontalGlue );
      add( filter, BorderLayout.NORTH );
      add( caseScrollPane, BorderLayout.CENTER );

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
            String value = ((CaseTableValue) ((SeparatorList.Separator) separator).first()).caseType().get();
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
            Set<String> labels = new LinkedHashSet<String>();
            labels.add("All");
            for (CaseTableValue caseTableValue : listChanges.getSourceList())
            {
               for (LinkValue linkValue : caseTableValue.labels().get().links().get())
               {
                  labels.add(linkValue.text().get());
               }
            }

            EventListSynch.synchronize( labels, labelList );
         }
      } );

      new RefreshWhenShowing( this, model );
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
   public void labels()
   {
      labelFilterList = new FilterList<CaseTableValue>( model.getEventList(), labelMatcher);

      status();
   }

   @org.jdesktop.application.Action
   public void status()
   {
      statusList = new FilterList<CaseTableValue>( labelFilterList, statusMatcher );
      grouping();
   }

   @org.jdesktop.application.Action
   public void grouping()
   {
      groupingList = new SeparatorList<CaseTableValue>( statusList, groupingComparator, 2, 10000);

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