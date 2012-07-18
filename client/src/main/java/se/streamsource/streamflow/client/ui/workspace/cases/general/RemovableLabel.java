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
package se.streamsource.streamflow.client.ui.workspace.cases.general;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.util.LinkedLabel;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RemovableLabel extends JPanel
      implements FocusListener
{
   private JButton button;
   private LinkValue removeLink;
   private LinkValue clickLink;
   private LinkedLabel label;

   public RemovableLabel()
   {
      initComponents();
   }

   public RemovableLabel(LinkValue removeLink, LinkValue clickLink)
   {
      this.removeLink = removeLink;
      this.clickLink = clickLink;
      initComponents();
   }

   private void initComponents()
   {
      this.setLayout( new BorderLayout(2,1) );
      setFocusable(true);
      this.setRequestFocusEnabled(true);

      if (removeLink != null)
      {
         label = new LinkedLabel( );
         label.setLink(clickLink, removeLink.text().get());
      } else
      {
         label = new LinkedLabel();
      }

      button = new JButton( i18n.icon( Icons.drop, 12 ) );
      button.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
      button.setBorderPainted( false );
      button.setContentAreaFilled( false );
      button.setFocusable( false );
      button.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            RemovableLabel.this.requestFocus();
         }
      } );

      this.add( label, BorderLayout.CENTER );
      this.add(button, BorderLayout.EAST);

      addFocusListener( this );
      addKeyListener( new KeyAdapter()
      {
         @Override
         public void keyPressed(KeyEvent e)
         {
            if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE
                  || e.getKeyChar() == KeyEvent.VK_DELETE)
            {
               button.doClick();
            }
         }
      });
      addMouseListener(new MouseAdapter()
      {
         @Override
         public void mouseClicked(MouseEvent e)
         {
            requestFocusInWindow();
         }
      });

      this.setVisible(removeLink != null);
   }

   @Override
   public void setEnabled( boolean enabled )
   {
      button.setVisible( enabled );
      label.setEnabled( enabled );
      super.setEnabled( enabled );
   }

   public LinkValue getRemoveLink()
   {
      return removeLink;
   }

   public JButton getButton()
   {
      return button;
   }

   public LinkedLabel getLabel()
   {
      return label;
   }

   public void focusGained( FocusEvent e )
   {
      setBorder( BorderFactory.createLineBorder(Color.GRAY, 1) );
      repaint();
   }

   public void focusLost( FocusEvent e )
   {
      setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
      repaint();
   }

   public void setText( String text )
   {
      label.setText( text );
      setToolTipText( text );
      this.setVisible( text != null );
   }

   public void setRemoveLink(LinkValue link)
   {
      this.removeLink = link;
      if (link != null)
      {
         label.setLink(clickLink, this.removeLink.text().get() );
      }
      this.setVisible( link != null );
   }

   public void setClickLink(LinkValue link)
   {
      this.clickLink = link;
      if (link != null)
      {
         label.setLink(clickLink, this.removeLink.text().get() );
      }
   }
   
   public void setLinks(LinkValue removeLink, LinkValue clickLink)
   {
      this.clickLink = clickLink;
      setRemoveLink(removeLink);
   }
}
