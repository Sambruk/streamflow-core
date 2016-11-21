/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.util;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import se.streamsource.streamflow.client.util.StateBinder.Binding;

public class SuggestTextField<T> extends JPanel
{
   private static final long serialVersionUID = -2427927984739983590L;

   private int MAX_LIST_LENGTH = 10;
   private int MIN_LENGTH_FOR_SEARCH = 3;
   
   private JList list;
   private JPopupMenu popup = new JPopupMenu();
   private DefaultListModel listModel;
   private JTextField textField;
   
   private SuggestModel<T> model;

   public SuggestTextField(SuggestModel<T> model)
   {
      this.model = model;

      initComponents();
   }

   public SuggestTextField(SuggestModel<T> model, int maxListLength, int minLengthForSearch)
   {
      this( model );
      MAX_LIST_LENGTH = maxListLength;
      MIN_LENGTH_FOR_SEARCH = minLengthForSearch;
   }

   public JTextField getTextField()
   {
      return textField;
   }
   
   private void initComponents()
   {
      setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
      textField = new JTextField();
      add( textField );

      listModel = new DefaultListModel();
      list = new JList(listModel);
      JScrollPane scroll = new JScrollPane( list );

      list.setFocusable( false );
      scroll.getVerticalScrollBar().setFocusable( false );
      scroll.getHorizontalScrollBar().setFocusable( false );

      popup.add( scroll );

      textField.registerKeyboardAction( new ShowPopupAction(), KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 ),
            JComponent.WHEN_FOCUSED );
      textField.registerKeyboardAction( new UpAction(), KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 ),
            JComponent.WHEN_FOCUSED );
      textField.registerKeyboardAction( new HidePopupAction(), KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ),
            JComponent.WHEN_FOCUSED );

      textField.addKeyListener( new KeyListener()
      {
         public void keyTyped(KeyEvent e){}
         public void keyPressed(KeyEvent e) {}
         
         public void keyReleased(KeyEvent e)
         {
            if (e.getKeyCode() != KeyEvent.VK_DOWN && e.getKeyCode() != KeyEvent.VK_UP
                  && e.getKeyCode() != KeyEvent.VK_ESCAPE && e.getKeyCode() != KeyEvent.VK_ENTER)
            {
               if (textField.getText().length() >= MIN_LENGTH_FOR_SEARCH)
                  showPopup();
               else
                  popup.setVisible( false );
            }
         }
      });
      
      textField.addFocusListener( new FocusListener()
      {
         public void focusLost(FocusEvent e)
         {
            if (!e.isTemporary())
            {
               popup.setVisible( false );
               handleSaveAction(textField.getText());
            }
         }
         
         public void focusGained(FocusEvent e)
         {
            textField.setSelectionStart( textField.getCaretPosition() );
         }
      });
      
      popup.addPopupMenuListener( new PopupMenuListener()
      {
         public void popupMenuWillBecomeVisible(PopupMenuEvent e){}

         public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
         {
            textField.unregisterKeyboardAction( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ) );
         }

         public void popupMenuCanceled(PopupMenuEvent e){}
      } );
      
      list.setRequestFocusEnabled( false );
   }

   public void setBinding( final Binding binding )
   {
      textField.setInputVerifier( new InputVerifier()
      {
         
         @Override
         public boolean verify(JComponent input)
         {
            if (!popup.isVisible())
            {
               binding.updateProperty( textField.getText() );
               return true;
            }
            
            return false;
         }
      });
   }
   
   @SuppressWarnings("serial")
   private class ShowPopupAction extends AbstractAction
   {
      public void actionPerformed(ActionEvent e)
      {
         if (textField.isEnabled())
         {
            if (popup.isVisible())
               selectNextValue();
            else
               showPopup();
         }
      }
   }

   @SuppressWarnings("serial")
   private class AcceptAction extends AbstractAction
   {
      public void actionPerformed(ActionEvent e)
      {
         handleAcceptAction( model.valueAt( list.getSelectedIndex() ) );
         popup.setVisible( false );
      }
   }

   @SuppressWarnings("serial")
   private class HidePopupAction extends AbstractAction
   {
      public void actionPerformed(ActionEvent e)
      {
         popup.setVisible( false );
      }
   }

   @SuppressWarnings("serial")
   private class UpAction extends AbstractAction
   {
      public void actionPerformed(ActionEvent e)
      {
         if (textField.isEnabled())
         {
            if (popup.isVisible())
               selectPreviousValue();
         }
      }
   };

   private void showPopup()
   {
      listModel.clear();
      for (T item : model.options( textField.getText() ))
      {
         listModel.addElement( model.displayValue( item ) );
      }
      
      if (textField.isEnabled() && list.getModel().getSize() != 0)
      {
         textField.registerKeyboardAction( new AcceptAction(), KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ),
               JComponent.WHEN_FOCUSED );
         int size = list.getModel().getSize();
         list.setVisibleRowCount( size < MAX_LIST_LENGTH ? size : MAX_LIST_LENGTH );

         popup.pack();
         if (!popup.isVisible())
         {
            popup.show( textField, 2, textField.getHeight() );
         }
         
      } else
      {
         popup.setVisible( false );
      }
      textField.requestFocusInWindow();
   }

   public void handleAcceptAction(T selectedItem)
   {
      textField.setText( model.displayValue( selectedItem ));
   }

   public void handleSaveAction(String text){
      
   };

   private void selectNextValue()
   {
      int index = list.getSelectedIndex();

      if (index < list.getModel().getSize() - 1)
      {
         list.setSelectedIndex( index + 1 );
         list.ensureIndexIsVisible( index + 1 );
      }
   }

   private void selectPreviousValue()
   {
      int index = list.getSelectedIndex();

      if (index > 0)
      {
         list.setSelectedIndex( index - 1 );
         list.ensureIndexIsVisible( index - 1 );
      }
   }

}
