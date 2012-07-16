/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.client.util.popup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.qi4j.api.util.Iterables;

import se.streamsource.streamflow.client.util.StreamflowToggleButton;

public class StandardPopupHandler implements PopupHandler
{

   private StreamflowToggleButton button;
   private JPanel optionsPanel;
   private JDialog popup;
   private final JPanel parentPanel;
   private Position position;
   private boolean refreshOnClose;
   private RefreshHandler refreshHandler;
   
   public StandardPopupHandler(JPanel parent, Action action, RefreshHandler refreshHandler)
   {
      this( parent, action, Position.left, true, refreshHandler );
   }
   
   public StandardPopupHandler(final JPanel parent, Action action, Position position, boolean refreshOnClose, RefreshHandler refreshHandler)
   {
      this.parentPanel = parent;
      this.position = position;
      this.refreshOnClose = refreshOnClose;
      this.refreshHandler = refreshHandler;
      button = new StreamflowToggleButton( action );
      button.addItemListener( new ItemListener()
      {

         public void itemStateChanged(ItemEvent itemEvent)
         {
            int state = itemEvent.getStateChange();
            if (state == ItemEvent.SELECTED)
            {
               
               cleanToggleButtonSelection();
               optionsPanel = new JPanel();
               JToggleButton button = (JToggleButton) itemEvent.getSource();
               showPopup( button );
            } else if (state == ItemEvent.DESELECTED)
            {
               kill();
            }
         }
      } );
   }

   public StreamflowToggleButton getButton()
   {
      return button;
   }

   public void setPanelContent(JPanel filterPanel)
   {
      optionsPanel.add( filterPanel );
   }

   private void showPopup(final Component button)
   {
      SwingUtilities.invokeLater( new Runnable()
      {

         public void run()
         {
            // Make it impossible to have several popups open at the same time
            if (popup != null)
            {
               popup.dispose();
               popup = null;
            }
            final JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass( JFrame.class, parentPanel );
            popup = new JDialog( frame );
            popup.getRootPane().registerKeyboardAction( new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  System.out.println("Enter");
                  kill();
                  cleanToggleButtonSelection();
               }
            }, KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), JComponent.WHEN_IN_FOCUSED_WINDOW );
            popup.setUndecorated( true );
            popup.setModal( false );
            popup.setLayout( new BorderLayout() );

            popup.add( optionsPanel, BorderLayout.CENTER );
            Point location = button.getLocationOnScreen();        
            
            int diff = 0;
            if (position == Position.right) {
               diff = (int) optionsPanel.getPreferredSize().getWidth() - button.getWidth();
            }
            popup.setBounds( (int) location.getX() -diff, (int) location.getY() + button.getHeight(),
                  optionsPanel.getWidth(), optionsPanel.getHeight() );
            popup.pack();
            popup.setVisible( true );
            frame.addComponentListener( new ComponentAdapter()
            {
               @Override
               public void componentMoved(ComponentEvent e)
               {
                  if (popup != null)
                  {
                     System.out.println("Moved");
                     kill();
                     ((JToggleButton) button).setSelected( false );
                     frame.removeComponentListener( this );
                  }
               }
            } );
         }
      } );
   }

   public void kill()
   {
      if (popup != null)
      {
         popup.setVisible( false );
         popup.dispose();
         popup = null;
      }
      if (refreshOnClose) {
         refreshHandler.refresh();
      }
   }

   private void cleanToggleButtonSelection()
   {
      for (Component component : Iterables.flatten( Iterables.iterable( parentPanel.getComponents() ),
            Iterables.iterable( parentPanel.getComponents() ) ))
      {
         if (!(component instanceof JToggleButton))
            continue;
         if (((JToggleButton) component).isSelected() && component != button)
         {
            ((JToggleButton) component).setSelected( false );
         }
      }
   }

}
