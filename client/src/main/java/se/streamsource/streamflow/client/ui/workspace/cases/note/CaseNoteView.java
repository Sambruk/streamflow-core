/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.note;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXPanel;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.workspace.cases.general.NoteDTO;
import se.streamsource.streamflow.client.ui.DateFormats;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowToggleButton;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Locale;

import static org.qi4j.api.specification.Specifications.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * A view representing all general notes present on a case
 */
public class CaseNoteView
   extends JPanel implements TransactionListener, Refreshable
{
   private ValueBinder valueBinder;
   private ActionBinder actionBinder;

   private CaseNoteModel model;
   private JTextArea note;

   private JDialog popup;

   StreamflowToggleButton allNotesBtn;

   public CaseNoteView( @Service ApplicationContext appContext,
                        @Uses CaseNoteModel model,
                        @Structure Module module )
   {
      this.model = model;


      setActionMap( appContext.getActionMap( this ) );
      ActionMap am = getActionMap();

      ObjectBuilderFactory obf = module.objectBuilderFactory();

      FormLayout formLayout = new FormLayout( "70dlu, 3dlu, pref, 3dlu, right:70dlu:grow","pref,120:grow" );
      this.setLayout( formLayout );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, this );

      valueBinder = obf.newObject( ValueBinder.class );
      actionBinder = obf.newObjectBuilder( ActionBinder.class ).use( am ).newInstance();
      actionBinder.setResourceMap( appContext.getResourceMap( this.getClass() ) );

      formBuilder.append( i18n.text( WorkspaceResources.note_label ));


      allNotesBtn = new StreamflowToggleButton( am.get( "allNotes" ) );
      allNotesBtn.addItemListener( new ItemListener()
      {
         public void itemStateChanged(ItemEvent itemEvent)
         {
            int state = itemEvent.getStateChange();
            if (state == ItemEvent.SELECTED)
            {
               showPopup( allNotesBtn );
           } else if (state == ItemEvent.DESELECTED)
           {
              killPopup();
           }
         }
       });
      formBuilder.nextColumn(2);
      formBuilder.append( allNotesBtn );

      JScrollPane textScroll = null;
      formBuilder.add( textScroll = new JScrollPane( valueBinder.bind( "note", actionBinder.bind( "addNote",  note = new JTextArea() ) ) ),
            new CellConstraints( 1, 2, 5, 1, CellConstraints.FILL, CellConstraints.FILL ) );
      textScroll.setPreferredSize( new Dimension( 210, 120 ) );
      note.setLineWrap( true );
      note.setWrapStyleWord( true );

      textScroll.getViewport().getView().setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null );
      textScroll.getViewport().getView().setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null );

      RefreshComponents refreshComponents = new RefreshComponents();
      model.addObserver( refreshComponents );
      refreshComponents.enabledOn( "addnote", note );
      
      new RefreshWhenShowing( this, this );

   }


   @Action(block = Task.BlockingScope.COMPONENT)
   public Task addNote( final ActionEvent event )
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.addNote( ((JTextArea) event.getSource()).getText() );
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public void allNotes( final ActionEvent event )
   {

   }


   public void refresh()
   {
      model.refresh();
      valueBinder.update( model.getNote() );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( or( withNames( "addedNote" ), withUsecases( "reopen", "close" ) ), transactions ))
      {
         refresh();
      }
   }

   private void showPopup( JComponent component)
   {

      JXPanel panel = new JXPanel();
      panel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
      JScrollPane scroll = new JScrollPane(  );
      panel.add( scroll );

      JList notes = new JList( );
      scroll.setViewportView( notes );
      
      notes.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {

            if(!e.getValueIsAdjusting())
            {
               NoteDTO selectedNote = (NoteDTO)((JList)e.getSource()).getSelectedValue();
               if( !selectedNote.createdOn().get().equals( model.getNote().createdOn().get() ) )
               {
                  note.setEnabled( false );
               } else
               {
                  note.setEnabled( model.checkNoteEnabled() );
               }

               note.setText( selectedNote.note().get() );
               model.setSelectedNoteIndex(e.getLastIndex());
            }

         }
      } );

      notes.addFocusListener( new FocusAdapter()
      {
         @Override
         public void focusLost( FocusEvent e )
         {
            if( !(e.getOppositeComponent() instanceof StreamflowToggleButton) )
               allNotesBtn.doClick();
            else if( (e.getOppositeComponent() instanceof StreamflowToggleButton)  )
               killPopup();

         }
      } );
      
      notes.setModel( model.getNotes() );
      notes.setSelectedIndex( model.getSelectedNoteIndex() );
      
      notes.setCellRenderer( new DefaultListCellRenderer()
      {
         public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
         {
            NoteDTO note = (NoteDTO) value;

            JPanel itemPanel = new JPanel();
            itemPanel.setLayout( new BoxLayout( itemPanel, BoxLayout.X_AXIS ) );
            
            JLabel date = new JLabel(DateFormats.getProgressiveDateTimeValue( note.createdOn().get(),
                  Locale.getDefault() ) + "\t", JLabel.LEFT );
            JLabel name = new JLabel( note.creator().get(), JLabel.RIGHT);
            
            itemPanel.add( date, Box.LEFT_ALIGNMENT);
            itemPanel.add( Box.createHorizontalGlue() );
            itemPanel.add( name, Box.RIGHT_ALIGNMENT );

            itemPanel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));

            if(isSelected)
            {

               itemPanel.setBackground( list.getSelectionBackground() );
               date.setForeground( list.getSelectionForeground() );
               name.setForeground( list.getSelectionForeground() );
            } else
            {
               itemPanel.setBackground( list.getBackground() );
               date.setForeground( list.getForeground() );
               name.setForeground( list.getForeground() );
            }
            
            return itemPanel;
         }
      } );


      final JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass( JFrame.class, CaseNoteView.this );
      popup = new JDialog( frame );
      popup.getRootPane().registerKeyboardAction( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            killPopup();
         }
      }, KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), JComponent.WHEN_IN_FOCUSED_WINDOW);
      popup.setUndecorated( true );
      popup.setModal( false );
      popup.setLayout( new BorderLayout() );

      popup.add( panel, BorderLayout.CENTER );
      Point location = component.getLocationOnScreen();

      int diff = (int) panel.getPreferredSize().getWidth() - component.getWidth();
      popup.setBounds( (int) location.getX() - diff , (int) location.getY() + component.getHeight(),
            (int) panel.getPreferredSize().getWidth(), (int) panel.getPreferredSize().getHeight() );

      popup.pack();
      popup.setVisible( true );
      frame.addComponentListener( new ComponentAdapter()
      {
         @Override
         public void componentMoved( ComponentEvent e )
         {
            if (popup != null)
            {
               killPopup();
               frame.removeComponentListener( this );
            }
         }
      } );
   }

   private void killPopup()
   {
      if( popup != null )
      {
         popup.setVisible(false);
         popup.dispose();
         popup = null;
      }
   }
}
