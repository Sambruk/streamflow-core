/**
 *
 * Copyright 2009-2011 Streamsource AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.caselog;

import ca.odell.glazedlists.swing.EventJXTableModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.ButtonTableCellEditor;
import se.streamsource.streamflow.client.ui.DateFormats;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.popup.PopupHandler;
import se.streamsource.streamflow.client.util.popup.PopupHandler.Position;
import se.streamsource.streamflow.client.util.popup.RefreshHandler;
import se.streamsource.streamflow.client.util.popup.SelectionList;
import se.streamsource.streamflow.client.util.popup.StandardPopupHandler;
import se.streamsource.streamflow.client.util.popup.ValueToLabelConverter;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Locale;

import static se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes.*;
import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class CaseLogView extends JPanel implements TransactionListener, Refreshable
{

   private final CaseLogModel model;

   private JList filtersList;
   private JXTable table;
   private JScrollPane newMessagePane;
   private JTextArea newMessageArea;
   private PopupHandler filterPopupHandler;

   public CaseLogView(@Service ApplicationContext context, @Uses CaseLogModel logmodel)
   {
      this.model = logmodel;

      setActionMap( context.getActionMap( this ) );

      // Layout and form for the left panel
      FormLayout rightLayout = new FormLayout( "30dlu, 300:grow, 50dlu", "pref, fill:pref:grow, 60dlu" );
      setLayout( rightLayout );
      setFocusable( false );
      DefaultFormBuilder rightBuilder = new DefaultFormBuilder( rightLayout, this );
      rightBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY2, Sizes.DLUX2, Sizes.DLUY2, Sizes.DLUX2 ) );

      JLabel caseLogLabel = new JLabel( i18n.text( WorkspaceResources.case_log ) );
      rightBuilder.add( caseLogLabel, new CellConstraints( 1, 1, 2, 1, CellConstraints.LEFT, CellConstraints.TOP,
            new Insets( 0, 0, 0, 0 ) ) );
      rightBuilder.nextColumn();

      filtersList = new SelectionList( Arrays.asList( system.name(), custom.name(), contact.name(), form.name(),
            conversation.name(), attachment.name() ), model.getSelectedFilters(), new ValueToLabelConverter()
      {

         public String convert(String value)
         {
            return text( valueOf( value.toString() ) );
         }
      }, new ListSelectionListener()
      {


         public void valueChanged(ListSelectionEvent event)
         {
            if (!event.getValueIsAdjusting())
            {
               String selectedValue = (String) ((JList) event.getSource()).getSelectedValue();
               if (selectedValue != null)
               {
                  if (model.getSelectedFilters().contains( selectedValue ))
                  {
                     model.getSelectedFilters().remove( selectedValue );
                  } else
                  {
                     model.getSelectedFilters().add( selectedValue );
                  }
                  CaseLogView.this.refresh();
                  ((JList) event.getSource()).clearSelection();
               }
            }
         }
      } );

      filterPopupHandler = new StandardPopupHandler( CaseLogView.this, getActionMap().get( "filter" ), Position.right,
            false, new RefreshHandler()
            {
               public void refresh()
               {
                  CaseLogView.this.refresh();
               }
            } );
      rightBuilder.add( filterPopupHandler.getButton(), new CellConstraints( 3, 1, 1, 1, CellConstraints.RIGHT,
            CellConstraints.TOP, new Insets( 0, 0, 0, 0 ) ) );

      filterPopupHandler.getButton().setMargin( new Insets( 1, 1, 1, 1 ) );

      filtersList.addFocusListener( new FocusAdapter()
      {
         @Override
         public void focusLost(FocusEvent e)
         {
            if (e.getOppositeComponent() != null && (e.getOppositeComponent().equals( filterPopupHandler.getButton() )))
               filterPopupHandler.kill();
         }
      } );

      // Caselog
      rightBuilder.nextLine();

      table = new JXTable(  new EventJXTableModel<CaseLogEntryDTO>( model.caselogs(), new CaseLogTableFormatter()  ) );
      table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      table.addHighlighter( HighlighterFactory.createAlternateStriping() );

      table.getColumn( 0 ).setPreferredWidth( 60 );
      table.getColumn( 0 ).setMaxWidth( 60 );
      table.getColumn( 0 ).setResizable( false );

      table.setAutoCreateColumnsFromModel( false );

      AbstractAction setPublishAction = new AbstractAction()
      {
          public void actionPerformed(ActionEvent e)
          {
              int modelRow = Integer.valueOf( e.getActionCommand() );
              setPublish( modelRow );
          }
      };
      ButtonTableCellEditor buttonCellEditor = new ButtonTableCellEditor( table, setPublishAction );

      table.setDefaultRenderer( Icon.class, buttonCellEditor );
      table.setDefaultEditor( Icon.class, buttonCellEditor );
      table.setDefaultRenderer( CaseLogEntryDTO.class, new TableCellRenderer()
      {
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
         {
            CaseLogEntryDTO entry = (CaseLogEntryDTO) value;

            JPanel renderer = new JPanel();

            FormLayout rowLayout = new FormLayout( "pref:grow, pref", "pref, fill:pref:grow" );
            renderer.setLayout( rowLayout );
            DefaultFormBuilder formBuilder = new DefaultFormBuilder( rowLayout, renderer );
            renderer.setBorder( new EmptyBorder( 3, 3, 6, 3 ) );

            // User
            JLabel user = new JLabel( entry.creator().get() );
            user.setForeground( Color.GRAY );
            formBuilder.add( user, new CellConstraints( 1, 1, 1, 1, CellConstraints.LEFT, CellConstraints.TOP ) );

            // Date
            JLabel date = new JLabel( DateFormats.getProgressiveDateTimeValue( entry.creationDate().get(),
                  Locale.getDefault() ) );
            date.setForeground( Color.GRAY );
            formBuilder.add( date, new CellConstraints( 2, 1, 1, 1, CellConstraints.RIGHT, CellConstraints.TOP ) );

            // Message
            String entryMessage = entry.message().get();
            if (!Strings.empty( entryMessage ))
            {
               entryMessage = String.format("<html><div WIDTH=%d>%s</div><html>", 500, entryMessage.trim().replace( "\n", "<br>" ) );
            }
            JLabel message = new JLabel( entryMessage );
            message.setForeground( Color.BLACK );
            formBuilder.add( message, new CellConstraints( 1, 2, 2, 1, CellConstraints.LEFT, CellConstraints.TOP ) );

            if( isSelected )
            {
               renderer.setBackground( table.getSelectionBackground() );
               renderer.setForeground( table.getSelectionForeground() );
               user.setForeground( table.getSelectionForeground() );
               date.setForeground( table.getSelectionForeground() );
               message.setForeground( table.getSelectionForeground() );
            }

            renderer.setOpaque( true );
            
            table.setRowHeight( row, renderer.getPreferredSize().height );
            return renderer;
         }
      } );

      JScrollPane scroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
      scroll.setMinimumSize( new Dimension( 250, 100 ) );
      scroll.setPreferredSize( new Dimension( 400, 300 ) );
      
      rightBuilder.setExtent( 3, 1 );
      rightBuilder.add( scroll, new CellConstraints( 1, 2, 3, 1, CellConstraints.FILL, CellConstraints.FILL,
            new Insets( 0, 0, 0, 0 ) ) );

      // Add caselog message
      ImageIcon icon = i18n.icon( Icons.message_add, 24 );
      rightBuilder.add( new JLabel( icon ), new CellConstraints( 1, 3, 1, 1, CellConstraints.LEFT, CellConstraints.TOP,
            new Insets( 10, 10, 0, 0 ) ) );
      newMessageArea = new JTextArea( 10, 30 );
      newMessageArea.setLineWrap( true );
      newMessageArea.setWrapStyleWord( true );
      newMessagePane = new JScrollPane( newMessageArea );
      newMessagePane.setMinimumSize( new Dimension( 10, 10 ) );
      newMessagePane.setPreferredSize( new Dimension( 10, 70 ) );
      rightBuilder.add( newMessagePane, new CellConstraints( 2, 3, 2, 1, CellConstraints.FILL, CellConstraints.TOP,
            new Insets( 10, 0, 0, 0 ) ) );

      newMessageArea.addKeyListener( new KeyListener()
      {

         public void keyTyped(KeyEvent e)
         {
         }

         public void keyReleased(KeyEvent e)
         {
            if (e.getKeyCode() == 10)
            {
               if (!e.isControlDown())
               {
                  newMessageArea.setText( "" );
               }
            }
         }

         public void keyPressed(KeyEvent e)
         {
            if (e.getKeyCode() == 10)
            {
               if (e.isControlDown())
               {
                  newMessageArea.append( "\n" );
               } else
               {
                  addMessage();
               }
            }
         }
      } );

      RefreshComponents refreshComponents = new RefreshComponents();
      refreshComponents.enabledOn( "addMessage", newMessagePane.getViewport().getView() );

      new RefreshWhenShowing( this, this );
   }

   @Action
   public void filter()
   {
      JPanel filterPanel = new JPanel( new BorderLayout() );
      filterPanel.setPreferredSize( new Dimension( 130, 120 ) );
      filterPanel.setMaximumSize( new Dimension( 130, 120 ) );
      filterPanel.setMaximumSize( new Dimension( 130, 120 ) );
      filterPanel.add( filtersList, BorderLayout.CENTER );
      filterPopupHandler.setPanelContent( filterPanel );
   }

   public void refresh()
   {
      model.refresh();

      SwingUtilities.invokeLater( new Runnable(){

         public void run()
         {
            table.packAll();
            table.getColumn( 0 ).setPreferredWidth( 60 );
            table.getColumn( 0 ).setMaxWidth( 60 );
            table.scrollRowToVisible( table.getRowCount() - 1 );
         }
      });

   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (matches( withNames( "satMyPagesVisibility" ), transactions ))
      {
         refresh();
         table.clearSelection();
      }
   }

   public void addMessage()
   {
      if (!Strings.empty( newMessageArea.getText() ))
      {
         model.addMessage( newMessageArea.getText() );
         newMessageArea.setText( "" );
         newMessageArea.setCaretPosition( 0 );
         newMessageArea.requestFocusInWindow();
         refresh();
      }
   }

   public void setPublish( final int index)
   {
      new CommandTask(){

         @Override
         protected void command() throws Exception
         {
            model.togglepublish( ((LinkValue)table.getValueAt( index, 1 )));
         }
      }.execute();
   }
}
