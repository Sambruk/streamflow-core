package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.swingx.JXLabel;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

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

public class PossibleFormView extends JPanel
      implements FocusListener, KeyListener, MouseListener
{
   ListItemValue itemValue;
   JButton button;

   public PossibleFormView( ListItemValue itemValue )
   {
      super( new FlowLayout( FlowLayout.LEFT, 2, 1 ) );
      this.itemValue = itemValue;

      setFocusable( true );
      this.setRequestFocusEnabled( true );

      JXLabel label = new JXLabel( itemValue.description().get() );
      button = new JButton( i18n.icon( Icons.formSubmit, 16 ) );
      button.setBorder( new EmptyBorder( new Insets( 0, 0, 0, 0 ) ) );
      button.setFocusable( false );

      this.add( label );
      this.add( button );
      setBorder( BorderFactory.createEtchedBorder() );

      addFocusListener( this );
      addKeyListener( this );
      addMouseListener( this );
   }

   public ListItemValue form()
   {
      return itemValue;
   }

   public void addActionListener( ActionListener listener )
   {
      button.addActionListener( listener );
   }

   public void focusGained( FocusEvent e )
   {
      setBorder( BorderFactory.createEtchedBorder( Color.LIGHT_GRAY, Color.BLUE ) );
      repaint();
   }

   public void focusLost( FocusEvent e )
   {
      setBorder( BorderFactory.createEtchedBorder() );
      repaint();
   }

   public void keyTyped( KeyEvent e )
   {
   }

   public void keyPressed( KeyEvent e )
   {
      if (e.getKeyChar() == KeyEvent.VK_SPACE
            || e.getKeyChar() == KeyEvent.VK_ENTER)
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
}
