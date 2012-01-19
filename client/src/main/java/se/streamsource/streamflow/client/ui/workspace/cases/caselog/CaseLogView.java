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

import static se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes.attachment;
import static se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes.contact;
import static se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes.conversation;
import static se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes.custom;
import static se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes.form;
import static se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes.system;
import static se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes.valueOf;
import static se.streamsource.streamflow.client.util.i18n.ICON_16;
import static se.streamsource.streamflow.client.util.i18n.icon;
import static se.streamsource.streamflow.client.util.i18n.text;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowToggleButton;
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
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

public class CaseLogView extends JPanel implements TransactionListener, Refreshable
{

   private final CaseLogModel model;

   private JList filtersList;
   private JList list = new JXList();
   private JScrollPane newMessagePane;
   private JTextArea newMessageArea;
   private PopupHandler filterPopupHandler;

   private boolean editMode = false;

   private ListSelectionListener listSelectionListener;

   private JScrollPane scroll;

   private StreamflowToggleButton editButton;
   private ImageIcon notPublishedIcon;
   private ImageIcon publishedIcon;

   public CaseLogView(@Service ApplicationContext context, @Uses CaseLogModel logmodel)
   {
      this.model = logmodel;

      setActionMap( context.getActionMap( this ) );

      // Load icons
      notPublishedIcon = icon( Icons.not_published, ICON_16 );
      publishedIcon = icon( Icons.published, ICON_16 );
      
      // Layout and form for the left panel
      FormLayout rightLayout = new FormLayout( "30dlu, 300:grow, 50dlu, 15dlu", "pref, fill:pref:grow, 60dlu" );
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
         @Override
         public String convert(String value)
         {
            return text( valueOf( value.toString() ) );
         }
      }, new ListSelectionListener()
      {

         @Override
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
                  model.refresh();
                  ((JList) event.getSource()).clearSelection();
               }
            }
         }
      } );

      filterPopupHandler = new StandardPopupHandler( CaseLogView.this, getActionMap().get( "filter" ), Position.right,
            false, new RefreshHandler()
            {
               @Override
               public void refresh()
               {
                  model.refresh();
                  list.ensureIndexIsVisible( list.getModel().getSize() - 1 );
               }
            } );
      rightBuilder.add( filterPopupHandler.getButton(), new CellConstraints( 3, 1, 1, 1, CellConstraints.RIGHT,
            CellConstraints.TOP, new Insets( 0, 0, 0, 0 ) ) );

      filtersList.addFocusListener( new FocusAdapter()
      {
         @Override
         public void focusLost(FocusEvent e)
         {
            if (e.getOppositeComponent() != null && (e.getOppositeComponent().equals( filterPopupHandler.getButton() )))
               filterPopupHandler.kill();
         }
      } );

      editButton = new StreamflowToggleButton( getActionMap().get( "edit"));
      rightBuilder.add(editButton, new CellConstraints( 4, 1, 1, 1, CellConstraints.RIGHT,
            CellConstraints.TOP, new Insets( 0, 0, 0, 0 ) ) );
      
      // Caselog
      rightBuilder.nextLine();
      ((JXList) list).addHighlighter( HighlighterFactory.createAlternateStriping() );
      list.setModel( new EventListModel<CaseLogEntryDTO>( model.caselogs() ) );
      list.setCellRenderer( new CaseLogListCellRenderer() );
      list.setFixedCellHeight( -1 );
      list.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      scroll = new JScrollPane( list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
      scroll.setMinimumSize( new Dimension( 250, 100 ) );
      scroll.setPreferredSize( new Dimension( 400, 300 ) );
      rightBuilder.setExtent( 3, 1 );
      rightBuilder.add( scroll, new CellConstraints( 1, 2, 4, 1, CellConstraints.FILL, CellConstraints.FILL,
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
      rightBuilder.add( newMessagePane, new CellConstraints( 2, 3, 3, 1, CellConstraints.FILL, CellConstraints.TOP,
            new Insets( 10, 0, 0, 0 ) ) );

      newMessageArea.addKeyListener( new KeyListener()
      {

         @Override
         public void keyTyped(KeyEvent e)
         {
         }

         @Override
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

         @Override
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

      listSelectionListener = new ListSelectionListener()
      {
         
         @Override
         public void valueChanged(ListSelectionEvent event)
         {
            if (! event.getValueIsAdjusting()){
               final CaseLogEntryDTO selectedValue = (CaseLogEntryDTO) ((JList) event.getSource()).getSelectedValue();
               if (selectedValue != null)
               {
                  new CommandTask(){

                     @Override
                     protected void command() throws Exception
                     {
                        model.togglepublish( selectedValue);
                     }
                  }.execute();
                  ((JList) event.getSource()).clearSelection();
               }
            }
         }
      };
      
      addHierarchyListener( new HierarchyListener()
      {
         @Override
         public void hierarchyChanged(HierarchyEvent e)
         {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing() && editMode){
               editButton.setSelected( false );
               configureComponentsForEdit( false );
            }  
         }
      });
      
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

   @Action
   public void edit()
   {
      // Toggle edit mode
      configureComponentsForEdit( !editMode );

   }
   
   private void configureComponentsForEdit(boolean edit){
      editMode = edit;
      if (editMode) {
         editButton.setIcon( publishedIcon );
         scroll.setBorder( BorderFactory.createLineBorder( list.getSelectionBackground(), 2) );
         list.addListSelectionListener( listSelectionListener );
      } else {
         editButton.setIcon( notPublishedIcon );
         scroll.setBorder( BorderFactory.createLineBorder( Color.GRAY, 1));
         list.removeListSelectionListener( listSelectionListener );
      }
   }
   
   public void refresh()
   {
      model.refresh();

      if (!editMode) {
         list.ensureIndexIsVisible( list.getModel().getSize() - 1 );
      }
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (matches( withNames( "addedEntry","modifiedMyPagesVisibility" ), transactions ))
      {
         refresh();
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
}
