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

package se.streamsource.streamflow.client.ui.caze;

import org.jdesktop.swingx.JXLabel;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.util.Strings;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class RemovableLabel extends JPanel
      implements FocusListener, KeyListener, MouseListener
{
   JButton button;
   private LinkValue link;
   private JXLabel label;
   private boolean useBorder = false;

   /**
    * Button orientation left
    */
   public static final int LEFT = 0;
   /**
    * Button orientation right
    */
   public static final int RIGHT = 1;

   public RemovableLabel()
   {
      initComponents( RemovableLabel.RIGHT );
   }

   public RemovableLabel( LinkValue link )
   {
      this( link, new FlowLayout( FlowLayout.LEFT, 2, 1 ), RIGHT );

   }

   public RemovableLabel( LinkValue link, boolean useBorder )
   {
      this( link, new FlowLayout( FlowLayout.LEFT, 2, 1 ), RIGHT );
      this.useBorder = useBorder;
   }

   public RemovableLabel( LinkValue link, FlowLayout layout, int buttonOrientation )
   {
      super( layout );
      this.link = link;
      initComponents( buttonOrientation );
   }

   public RemovableLabel( LinkValue link, FlowLayout layout, int buttonOrientation, boolean useBorder )
   {
      super( layout );
      this.link = link;
      this.useBorder = useBorder;
      initComponents( buttonOrientation );
   }


   private void initComponents( int buttonOrientation )
   {
      setFocusable( true );
      this.setRequestFocusEnabled( true );

      if (link != null)
      {
         label = new JXLabel( link.text().get() );
      } else
      {
         label = new JXLabel();
      }

      button = new JButton( i18n.icon( Icons.drop, 12 ) );
      button.setBorder( BorderFactory.createEmptyBorder( 0,0,0,0 ) );
      button.setFocusable( false );

      switch (buttonOrientation)
      {

         case RIGHT:
            this.add( label );
            this.add( button );
            break;

         case LEFT:
            this.add( button );
            this.add( label );
            break;
      }
      if (useBorder)
      {
         setBorder( BorderFactory.createEtchedBorder() );
      }

      addFocusListener( this );
      addKeyListener( this );
      addMouseListener( this );

      if (!Strings.notEmpty( label.getText() ))
      {
         this.setVisible( false );
      }
   }

   @Override
   public void setEnabled( boolean enabled )
   {
      button.setEnabled( enabled );
      label.setEnabled( enabled );
      super.setEnabled( enabled );
   }

   public LinkValue link()
   {
      return link;
   }

   public void addActionListener( ActionListener listener )
   {
      button.addActionListener( listener );
   }

   public void focusGained( FocusEvent e )
   {
      if (useBorder)
      {
         setBorder( BorderFactory.createEtchedBorder( Color.LIGHT_GRAY, Color.BLUE ) );
      } else
      {
         setBorder( BorderFactory.createLineBorder( Color.GRAY, 1 ) );
      }
      repaint();
   }

   public void focusLost( FocusEvent e )
   {
      if (useBorder)
      {
         setBorder( BorderFactory.createEtchedBorder() );
      } else
      {
         setBorder( BorderFactory.createEmptyBorder( 0,0,0,0 ) );
      }
      repaint();
   }

   public void keyTyped( KeyEvent e )
   {
   }

   public void keyPressed( KeyEvent e )
   {
      if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE
            || e.getKeyChar() == KeyEvent.VK_DELETE)
      {
         button.doClick();
      }
   }

   public void keyReleased( KeyEvent e )
   {
   }

   public void mouseClicked( MouseEvent e )
   {
      this.requestFocusInWindow();
   }

   public void mousePressed( MouseEvent e )
   {
   }

   public void mouseReleased( MouseEvent e )
   {
   }

   public void mouseEntered( MouseEvent e )
   {
   }

   public void mouseExited( MouseEvent e )
   {
   }

   public void setText( String text )
   {
      label.setText( text );
      if (text != null)
      {
         this.setVisible( true );
      }
   }

   public void setLinkValue( LinkValue link )
   {
      this.link = link;
      if (link != null)
      {
         label.setText( this.link.text().get() );
         this.setVisible( true );
      }
   }
}
