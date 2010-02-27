package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.swingx.JXLabel;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
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

public class RemovableLabel extends JPanel
      implements FocusListener, KeyListener, MouseListener
{
   ListItemValue itemValue;
   JButton button;
   private LinkValue link;

   /** Button orientation left*/
   public static final int LEFT = 0;
   /** Button orientation right*/
   public static final int RIGHT = 1;


   public RemovableLabel( ListItemValue itemValue )
   {
      super( new FlowLayout( FlowLayout.LEFT, 2, 1 ) );
      this.itemValue = itemValue;

      initComponents(RemovableLabel.RIGHT);
   }

   public RemovableLabel( LinkValue link)
   {
      this( link, new FlowLayout( FlowLayout.LEFT, 2, 1 ), RIGHT );

   }

   public RemovableLabel( LinkValue link, FlowLayout layout , int buttonOrientation)
   {
      super( layout );
      this.link = link;
      initComponents(buttonOrientation);
   }


   private void initComponents( int buttonOrientation)
   {
      setFocusable( true );
      this.setRequestFocusEnabled( true );

      JXLabel label = new JXLabel(  itemValue != null ? itemValue.description().get() : link.text().get() );
      button = new JButton( i18n.icon( Icons.deleteLabel, 16 ) );
      button.setBorder( new EmptyBorder( new Insets( 0, 0, 0, 0 ) ) );
      button.setFocusable( false );

      switch (buttonOrientation) {

         case RIGHT:
            this.add( label );
            this.add( button );
            break;

         case LEFT:
            this.add( button );
            this.add( label );
            break;
      }
      setBorder( BorderFactory.createEtchedBorder() );

      addFocusListener( this );
      addKeyListener( this );
      addMouseListener( this );
   }

   @Override
   public void setEnabled( boolean enabled )
   {
      button.setEnabled( enabled );
      super.setEnabled( enabled );
   }

   public ListItemValue item()
   {
      return itemValue;
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
}
